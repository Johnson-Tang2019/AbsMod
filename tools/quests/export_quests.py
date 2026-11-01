"""Export stable FTB 1.21.1 advancement quests from the shipped advancement descriptions."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RESOURCES = ROOT / "common/src/main/resources"


def stable_id(name):
    return hashlib.sha256(("absmod/quests/v1/" + name).encode()).hexdigest()[:16].upper()


def generate():
    language = json.loads((RESOURCES / "assets/absmod/lang/zh_cn.json").read_text(encoding="utf-8"))
    source = RESOURCES / "data/absmod/advancement/progression"
    quests = []
    names = {p.stem for p in source.glob("*.json")} - {"normal_stage_9"}
    positions = {"root": (0, 0), "resonance": (2, 0), "manifest": (4, 0), "bond": (6, 0),
                 "field_guide": (0, 6), "cat_observe": (2, 6),
                 "cat_family": (4, 6), "cat_dilute": (6, 6), "dark_heavy": (4, 4)}
    for stage in range(1, 9):
        positions["normal_stage_" + str(stage)] = (stage * 2, -2)
        positions["dark_stage_" + str(stage)] = (stage * 2, 2)
    for index, name in enumerate(sorted(names)):
        data = json.loads((source / (name + ".json")).read_text(encoding="utf-8"))
        display = data["display"]
        title = language[display["title"]["translate"]]
        description = language[display["description"]["translate"]]
        parent = data.get("parent", "").split("/")[-1]
        quest = {
            "id": stable_id(name), "title": title,
            "description": [description, "操作与配方详见游戏内观察手册（书 + 生鳕鱼）。"],
            "x": float(positions.get(name, (index * 2, 8))[0]), "y": float(positions.get(name, (index * 2, 8))[1]),
            "tasks": [{"id": stable_id(name + "/task"), "type": "advancement",
                       "advancement": "absmod:progression/" + name, "criterion": ""}],
            "rewards": [],
        }
        if parent in names:
            quest["dependencies"] = [stable_id(parent)]
        if name == "normal_stage_8":
            quest["description"].append("九阶尚未开放：同魂心魔试炼未实现。不要继续寻找不存在的升阶配方。")
        quests.append(quest)
    chapter = {"id": stable_id("chapter"), "filename": "absmod_progression", "title": "喵刀与猫咪生活",
               "order_index": 0, "group": "", "default_quest_shape": "square", "quests": quests}
    destination = ROOT / "quest-packs/ftb-1.21.1/config/ftbquests/quests/chapters/absmod_progression.snbt"
    destination.parent.mkdir(parents=True, exist_ok=True)
    # JSON's quoted keys and primitive lists are valid SNBT; avoid version-specific item-stack syntax.
    destination.write_text(json.dumps(chapter, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    ids = [q["id"] for q in quests] + [t["id"] for q in quests for t in q["tasks"]] + [chapter["id"]]
    assert len(ids) == len(set(ids)), "Duplicate task identity"
    assert all(q["tasks"][0]["advancement"].split("/")[-1] in names for q in quests)
    print(f"Exported {len(quests)} quests; stable IDs and advancement references verified. Runtime not implied.")


if __name__ == "__main__":
    generate()
