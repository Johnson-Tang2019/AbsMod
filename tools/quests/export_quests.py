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
                 "cat_palette": (8, 12), "cat_patterns": (10, 12), "cat_odd_eyes": (10, 14), "cat_pink": (12, 14),
                 "field_guide": (0, 6), "cat_observe": (2, 6),
                 "cat_chocolate": (6, 8), "cat_lilac": (8, 8), "cat_point": (6, 10), "cat_lilac_point": (10, 10),
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
        breeding_tips = {
            "cat_palette": "2048普通组合+256粉色组合。按底色、花纹、白斑、眼色和重点色收集；旧猫不变，繁育新后代即可。",
            "cat_patterns": "特征各从一位亲本继承，另有1/32机会随机变化。用手册查看和规划配对；这是游戏近似。",
            "cat_odd_eyes": "异瞳可遗传，也可由眼色特征的随机变化获得。不影响健康，不新增性别限制。",
            "cat_pink": "普通双亲1/16384，一粉1/1024，两粉1/128；每只幼猫独立抽取，无保底。浅奶粉色幻想变异，染料无效，存档重载不重抽。可选收藏，不阻挡主线。",
            "cat_chocolate": "用手册寻找Bb携带者。两只Bb每只幼猫有25%概率为bb；选择黑/巧克力色系（肉桂、橘红及粉色不满足巧克力外观目标），并观察有谱系后代。",
            "cat_lilac": "目标bb dd。两只Dd Bb的后代为bb dd的基因概率是6.25%；同基因纯合双亲可稳定遗传。重点色后代也能满足此基因目标。",
            "cat_point": "寻找Ccs携带者或暹罗来源cscs。Ccs×Ccs为25%，cscs×cscs为100%；末端颜色还取决于B/b和D/d。",
            "cat_lilac_point": "目标dd bb cscs。两只Dd Bb Ccs为1/64（约1.56%），不是保底。先完成淡紫与重点色，再规划组合；毛色收集不阻挡喵刀主线。",
        }
        if name in breeding_tips:
            quest["description"].append(breeding_tips[name])
        if name == "cat_lilac_point":
            quest["dependencies"].append(stable_id("cat_lilac"))
        if name == "normal_stage_8":
            quest["description"].append("九阶尚未开放：同魂心魔试炼未实现。不要继续寻找不存在的升阶配方。")
        quests.append(quest)
    chapter = {"id": stable_id("chapter"), "filename": "absmod_progression", "title": "喵刀与猫咪生活",
               "order_index": 0, "group": "", "default_quest_shape": "square", "quests": quests}
    destination = ROOT / "quest-packs/ftb-1.21.1/config/ftbquests/quests/chapters/absmod_progression.snbt"
    destination.parent.mkdir(parents=True, exist_ok=True)
    # JSON's quoted keys and primitive lists are valid SNBT; avoid version-specific item-stack syntax.
    destination.write_text(json.dumps(chapter, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    # Modern FTB Quests stores localized text separately; keep inline fallback for older packs.
    translations = {"chapter." + chapter["id"] + ".title": chapter["title"]}
    for quest in quests:
        translations["quest." + quest["id"] + ".title"] = quest["title"]
        translations["quest." + quest["id"] + ".quest_desc"] = quest["description"]
    lang = destination.parent.parent / "lang"
    lang.mkdir(exist_ok=True)
    (lang / "zh_cn.snbt").write_text(json.dumps(translations, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    ids = [q["id"] for q in quests] + [t["id"] for q in quests for t in q["tasks"]] + [chapter["id"]]
    assert len(ids) == len(set(ids)), "Duplicate task identity"
    assert all(q["tasks"][0]["advancement"].split("/")[-1] in names for q in quests)
    print(f"Exported {len(quests)} quests; stable IDs and advancement references verified. Runtime not implied.")


if __name__ == "__main__":
    generate()
