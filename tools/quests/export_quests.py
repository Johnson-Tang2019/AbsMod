"""Export stable FTB advancement quests from the shipped advancement descriptions."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RESOURCES = ROOT / "common/src/main/resources"


def stable_id(name):
    return hashlib.sha256(("absmod/quests/v1/" + name).encode()).hexdigest()[:16].upper()


def modern_id(legacy_id):
    # 26.1.2 FTB Quests treats nonpositive signed IDs as missing.
    return f"{int(legacy_id, 16) & ((1 << 63) - 1):016X}"


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
        if name == "manifest":
            quest["description"].extend([
                "前置：取得喵刀。主手持刀潜行右键显现，原槽留下虚化剑。找回剑灵无需额外材料。",
                "穿过传送门后先在安全地面等待剑灵。若未能跟上，主手持虚化剑对空气右键查看最后位置，再返回附近。",
                "靠近剑灵后潜行右键召回，持虚化剑攻击可召回同维度已加载的剑灵。重新登录会核对中断交接。若原槽被占，腾空后重登；手上仍有虚化剑时也可对空气右键重试。"
            ])
        if name == "resonance":
            quest["description"].extend([
                "主手持共鸣之心右键。粉色界面左上放召回后的原剑，右侧投入材料。",
                "悬停所需材料查看缺量。核对好感差值后点击「确认共鸣」，再取回原剑。每次只能升一阶，下次需重新打开。",
                "共鸣中断后重新登录：未确认的原剑和材料会返还，已保存的升阶继续结算一次。背包满时先腾出空间，再用共鸣之心打开工坊取回。"
            ])
        if name.startswith(("normal_stage_", "dark_stage_")) and name[-1] != "1":
            quest["description"].append("用共鸣之心打开粉色界面。原剑放左，材料放右。达到好感门槛后确认，再取回升级后的原剑。")
        if name == "normal_stage_1":
            quest["description"].append("主手持普通喵刀，按住攻击。有效主命中才叠连击；状态条显示当前层数、满层与衰减。")
        if name == "dark_heavy":
            quest["description"].extend([
                "前置：取得暗黑喵刀并装备在主手，无需消耗材料。对准可攻击的生物，按下默认 G 键，前摇期间保持瞄准。",
                "暗黑普攻是短紫弧。重击状态条显示就绪、前摇和冷却；命中有紫色光环。换剑或打开界面会中断前摇。"
            ])
        if name == "normal_stage_8":
            quest["description"].append("九阶尚未开放：同魂心魔试炼未实现。不要继续寻找不存在的升阶配方。")
        quests.append(quest)
    chapter = {"id": stable_id("chapter"), "filename": "absmod_progression", "title": "喵刀与猫咪生活",
               "order_index": 0, "group": "", "default_quest_shape": "square", "quests": quests}
    translations = {"chapter." + chapter["id"] + ".title": chapter["title"]}
    for quest in quests:
        translations["quest." + quest["id"] + ".title"] = quest["title"]
        translations["quest." + quest["id"] + ".quest_desc"] = quest["description"]
    legacy = ROOT / "quest-packs/ftb-1.21.1/config/ftbquests/quests"
    write_json(legacy / "chapters/absmod_progression.snbt", chapter)
    write_json(legacy / "lang/zh_cn.snbt", translations)

    modern = ROOT / "quest-packs/ftb-26.1.2/config/ftbquests/quests"
    modern_chapter = {key: value for key, value in chapter.items() if key not in {"title", "quests"}}
    modern_chapter["id"] = modern_id(chapter["id"])
    modern_chapter["quests"] = []
    for quest in quests:
        data = {key: value for key, value in quest.items() if key not in {"title", "description", "tasks"}}
        data["id"] = modern_id(quest["id"])
        if "dependencies" in data:
            data["dependencies"] = [modern_id(parent) for parent in data["dependencies"]]
        data["tasks"] = [dict(task, id=modern_id(task["id"]), type="ftbquests:" + task["type"]) for task in quest["tasks"]]
        modern_chapter["quests"].append(data)
    write_json(modern / "chapters/absmod_progression.json5", modern_chapter)
    write_json(modern / "lang/zh_cn/chapters/absmod_progression.json5",
               {f"quest.{modern_id(quest['id'])}.{key}": value
                for quest in quests for key, value in [("title", quest["title"]), ("quest_desc", quest["description"])]})
    write_json(modern / "lang/zh_cn/absmod_chapter.json5",
               {f"chapter.{modern_chapter['id']}.title": chapter["title"]})
    ids = [q["id"] for q in quests] + [t["id"] for q in quests for t in q["tasks"]] + [chapter["id"]]
    assert len(ids) == len(set(ids)), "Duplicate task identity"
    modern_ids = [modern_id(value) for value in ids]
    assert len(modern_ids) == len(set(modern_ids)) and all(int(value, 16) > 1 for value in modern_ids)
    assert all(q["tasks"][0]["advancement"].split("/")[-1] in names for q in quests)
    print(f"Exported {len(quests)} quests; stable IDs and advancement references verified. Runtime not implied.")


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    generate()
