"""Generate the 1.21.1 finish pack from the pinned Kaleidoscope Cookery release."""
import hashlib
import json
from pathlib import Path
import subprocess
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[2]
MODULE = ROOT / "1.21.1NeoForge"
RES = MODULE / "src/main/resources"
PACK = RES / "resourcepacks/rose_cookery"
UP = "kaleidoscope_cookery"
BLOCKS = """stove shawarma_spit pot stockpot chopping_board bamboo_tray millstone steamer teapot
trash_can kitchenware_racks fruit_basket enamel_basin oil_pot empty_cup clay_pot_milk_tea recipe_block""".split()
TEAS = "barley_tea biluochun butter_tea flower_tea mystery_tea oolong sakura_fubuki tieguanyin".split()
TOOLS = """stockpot_lid kitchen_shovel iron_kitchen_knife gold_kitchen_knife diamond_kitchen_knife
netherite_kitchen_knife sickle transmutation_lunch_bag recipe_item""".split()
ITEMS = [name for name in BLOCKS if name != "recipe_block"] + TOOLS + TEAS
PIN = "e93784a3f17e546ebfa7c449572933bf67c6b694c01cb26b83ad120c40452b33"

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf8", newline="\n")

def build():
    runtime = next((MODULE/"run/mods").glob("kaleidoscopecookery-1.6.0-neoforge+mc1.21.1.jar"))
    if hashlib.sha256(runtime.read_bytes()).hexdigest() != PIN:
        raise ValueError("Unexpected Cookery release")
    model_paths = set()
    textures = set()
    with ZipFile(runtime) as jar:
        def read(path):
            return json.loads(jar.read("assets/" + UP + "/" + path + ".json"))

        def texture(value):
            if not value.startswith(UP + ":"):
                return value
            path = value.split(":", 1)[1]
            if path.split("/")[-1] in {"oil", "water", "fire", "flame"}:
                return value
            textures.add(path)
            return "absmod:cookery/" + path

        def model(value):
            if not value.startswith(UP + ":"):
                return value
            path = value.split(":", 1)[1]
            if path not in model_paths:
                model_paths.add(path)
                data = read("models/" + path)
                if path.startswith("block/") and "elements" in data and "textures" in data and "loader" not in data:
                    # Inherit the installed geometry; only substitute the original UV material names.
                    data = {"parent": value, "textures": {k: texture(v) for k, v in data["textures"].items()}}
                else:
                    data = remap(data)
                write(PACK / ("assets/absmod/models/cookery/" + path + ".json"), data)
            return "absmod:cookery/" + path

        def remap(data):
            if isinstance(data, list):
                return [remap(v) for v in data]
            if not isinstance(data, dict):
                return data
            out = {}
            for key, value in data.items():
                if key in ("model", "parent") and isinstance(value, str):
                    out[key] = model(value)
                elif key == "textures":
                    out[key] = {k: texture(v) for k,v in value.items()}
                else:
                    out[key] = remap(value)
            return out

        for name in BLOCKS + TEAS:
            data = read("blockstates/" + name)
            variants = {}
            for key, value in data["variants"].items():
                suffix = "," + key if key else ""
                variants["absmod_rose=false" + suffix] = value
                variants["absmod_rose=true" + suffix] = remap(value)
            write(PACK / f"assets/{UP}/blockstates/{name}.json", {"variants": variants})

        for name in ITEMS:
            data = read("models/item/" + name)
            variants = data.setdefault("overrides", [])
            originals = list(variants)
            for override in originals:
                pink = remap(override)
                pink["predicate"]["absmod:rose_finish"] = 1
                variants.append(pink)
            if not originals:
                variants.append({"predicate":{"absmod:rose_finish":1}, "model":model(f"{UP}:item/{name}")})
            write(PACK / f"assets/{UP}/models/item/{name}.json", data)
            write(RES / f"data/absmod/recipe/cookery_finish/{name}.json", {
                "neoforge:conditions":[{"type":"neoforge:mod_loaded","modid":UP}],
                "type":"absmod:rose_upgrade","base":UP+":"+name,"result":UP+":"+name})

        textures.update("block/"+name for name in ("teapot","millstone","trash_can"))
        staging = ROOT / "build/cookery-uv"
        for texture_path in sorted(textures):
            filename = "textures/" + texture_path + ".png"
            raw = jar.read("assets/" + UP + "/" + filename)
            target = staging / (texture_path + ".png")
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(raw)
            meta = "assets/" + UP + "/" + filename + ".mcmeta"
            if meta in jar.namelist():
                write(PACK/("assets/absmod/textures/cookery/"+texture_path+".png.mcmeta"),json.loads(jar.read(meta)))
        subprocess.run(["java", str(Path(__file__).with_name("CookeryPalette.java")), str(staging),
                        str(PACK/"assets/absmod/textures/cookery"), *sorted(textures)], check=True)
    write(PACK/"assets/minecraft/atlases/blocks.json", {"sources":[{"type":"minecraft:directory","source":"cookery","prefix":"cookery/"}]})
    write(PACK/"pack.mcmeta", {"pack":{"pack_format":34,"description":"Ab's Mod · Rose Cookery"}})
    print(f"{len(ITEMS)} item finishes, {len(BLOCKS)+len(TEAS)} placed forms, {len(textures)} UV sheets")

if __name__ == "__main__":
    build()
