"""Export stable FTB advancement quests from the shipped advancement descriptions."""
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RESOURCES = ROOT / "common/src/main/resources"
COMPANION_QUESTS = {"gentle_brush", "milkshake_tray", "spirit_adornment", "spirit_board_game", "spirit_gomoku", "spirit_reversi"}
POSITIVE_QUEST_IDS = {"cat_collection", "cat_pedigree", "cat_nest", "cat_bell"} | COMPANION_QUESTS | {"paw_crystal", "rose_charms", "rose_tools", "rose_armor", "rose_kitchen", "whisker_compass", "petal_anklet", "rainfish_locket"}


def stable_id(name):
    digest = hashlib.sha256(("absmod/quests/v1/" + name).encode()).hexdigest()[:16].upper()
    # Both runtimes need positive IDs for new entries. Keep shipped legacy IDs for progress.
    return modern_id(digest) if name.split("/", 1)[0] in POSITIVE_QUEST_IDS else digest


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
                 "cat_family": (4, 6), "cat_dilute": (6, 6), "dark_heavy": (4, 4),
                 "cat_collection": (2, 8), "cat_pedigree": (4, 10), "cat_nest": (2, 10), "cat_bell": (2, 12),
                 "gentle_brush": (0, 8), "milkshake_tray": (0, 10), "spirit_adornment": (4, -4), "spirit_board_game": (6, -4),
                 "spirit_gomoku": (8, -4), "spirit_reversi": (10, -4)}
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
        home_tips = {
            "cat_collection": ["前置：观察一只猫。拿手册右键猫记录，再潜行用手册对空气右键。点击毛色、花纹或眼睛切换筛选，也可只看自家猫。"],
            "cat_pedigree": ["前置：观察有谱系的幼猫。打开手册收藏并点击这只猫。观察过的亲本和后代可点击，未观察的亲本保留空位。"],
            "cat_nest": ["材料：线、空格、线／白色羊毛×3／任意木板×3，分三行合成。",
                         "先用结缘铃右键自家的驯服猫，再右键已放置的猫窝绑定。空手右键让猫回窝，潜行右键解除。猫应在16格内且道路可达。"],
            "cat_bell": ["材料：铜锭、粉红色染料、铜锭／铜锭、紫水晶碎片、铜锭／空格、线、空格。",
                         "用铃右键自家的驯服猫选中它。退几步，潜行对空气右键呼唤。只呼唤16格内已加载且有路可达的猫。",
                         "对空气右键查看伙伴和上次位置。在附近剑灵的详情中可召回原剑，原槽或虚化剑槽须可用。"]
        }
        quest["description"].extend(home_tips.get(name, ()))
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
        if name == "gentle_brush":
            quest["description"].extend([
                "工作台合成：粉红色羊毛、白色羊毛并排。白色羊毛下面两格依次放线、木棍。",
                "拿在主手，靠近并右键自家的驯服猫或已显现的剑灵。猫会转头和发出呼噜声，剑灵会低头回应。",
                "剑灵梳理与当天的普通交流共用一次好感奖励。当天已记录后仍可梳理。"
            ])
        if name == "spirit_adornment":
            quest["description"].extend([
                "金粒、线和一份专用材料无序合成。樱花发夹用粉红色羊毛，月纹发夹用紫水晶碎片。其余十款材料见观察手册第 32–33 页。",
                "把饰品带在背包里。先显现自己的剑灵，再持结缘铃对空气右键，进入附近剑灵的详情，点「剑灵饰品」。",
                "可翻页换款或收起。饰品可重复使用，外观随同一把剑保存。"
            ])
        if name == "milkshake_tray":
            quest["description"].extend([
                "前置：完成观察手册任务。准备五块樱花木板、两块粉红色羊毛、一枚紫水晶碎片和任意一杯已有奶昔。",
                "合成三行：樱花木板／空／樱花木板；粉红色羊毛／紫水晶碎片／粉红色羊毛；樱花木板×3。",
                "放置茶盘，主手持奶昔右键任意空杯垫，再用空主手右键同一个杯垫取回，完成任务。访客可取用，打破会掉出剩余奶昔。",
            ])
        board_rules = {
            "spirit_board_game": "选择「剑灵井字棋」。你先落 X，剑灵落 O，先在一条线上连三枚获胜。",
            "spirit_gomoku": "选择「五子棋」。在 9×9 的棋盘上横、竖或斜连出五子即可获胜。",
            "spirit_reversi": "选择「黑白棋」。你执黑先走。点带圆点的格子，夹住并翻转白子。自动跳过无合法落点的一方，终局棋子多者胜。",
        }
        if name in board_rules:
            quest["description"].extend([
                "前置：自己的剑灵已显现且在附近，无需额外材料。持结缘铃对空气右键，打开该剑灵的详情，点「陪伴游戏」。",
                board_rules[name],
                "下完一局即可完成，获胜、落败或平局都算。",
            ])
        quests.append(quest)
    chapter = {"id": stable_id("chapter"), "filename": "absmod_progression", "title": "喵刀与猫咪生活",
               "order_index": 0, "group": "", "default_quest_shape": "square", "quests": quests}
    translations = {"chapter." + chapter["id"] + ".title": chapter["title"]}
    bond_tip = "查看剑灵关系：先显现自家剑灵，持结缘铃对空气右键，再点击剑灵。面板显示好感、下阶门槛和今日记录。空手右键交流，手持生鳕鱼右键赠礼。完成后重开详情，或点击「刷新」。"
    legacy_quests = [dict(q, description=q["description"] + [bond_tip]) if q["id"] == stable_id("bond") else q for q in quests]
    active = ROOT / "1.21.1NeoForge/src/main"
    workshop_text = json.loads((active / "language/zh_cn.json").read_text(encoding="utf-8"))
    workshop_positions = {"paw_crystal": (2, -6), "rose_charms": (4, -6), "rose_tools": (4, -8),
                          "rose_armor": (4, -10), "rose_kitchen": (4, -12), "whisker_compass": (6, -6),
                          "petal_anklet": (6, -8), "rainfish_locket": (6, -10)}
    for name, (x, y) in workshop_positions.items():
        advancement = json.loads((active / "resources/data/absmod/advancement/progression" / (name + ".json")).read_text(encoding="utf-8"))
        desc = [workshop_text["advancement.absmod." + name + ".description"]]
        pages = {"paw_crystal": [], "rose_charms": [39, 40], "whisker_compass": [42],
                 "petal_anklet": [43], "rainfish_locket": [44]}.get(name, [41])
        desc.extend(workshop_text[f"guide.absmod.page.{page}"] for page in pages)
        legacy_quests.append({"id": stable_id(name), "title": workshop_text["advancement.absmod." + name + ".title"],
                              "description": desc, "x": x, "y": y,
                              "dependencies": [stable_id(advancement["parent"].split("/")[-1])],
                              "tasks": [{"id": stable_id(name + "/task"), "type": "advancement",
                                         "advancement": "absmod:progression/" + name, "criterion": ""}], "rewards": []})
    legacy_ids = [chapter["id"]] + [i for q in legacy_quests for i in (q["id"], q["tasks"][0]["id"])]
    assert len(legacy_ids) == len(set(legacy_ids))

    for quest in legacy_quests:
        translations["quest." + quest["id"] + ".title"] = quest["title"]
        translations["quest." + quest["id"] + ".quest_desc"] = quest["description"]
    legacy = ROOT / "quest-packs/ftb-1.21.1/config/ftbquests/quests"
    write_json(legacy / "chapters/absmod_progression.snbt", dict(chapter, quests=legacy_quests))
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
    modern_translations = {}
    for quest in quests:
        prefix = f"quest.{modern_id(quest['id'])}"
        modern_translations[prefix + ".title"] = quest["title"]
        modern_translations[prefix + ".quest_desc"] = (
            quest["description"] + [bond_tip] if quest["id"] == stable_id("bond") else quest["description"]
        )
    write_json(modern / "lang/zh_cn/chapters/absmod_progression.json5",
               modern_translations)
    write_json(modern / "lang/zh_cn/absmod_chapter.json5",
               {f"chapter.{modern_chapter['id']}.title": chapter["title"]})
    ids = [q["id"] for q in quests] + [t["id"] for q in quests for t in q["tasks"]] + [chapter["id"]]
    assert len(ids) == len(set(ids)), "Duplicate task identity"
    modern_ids = [modern_id(value) for value in ids]
    assert len(modern_ids) == len(set(modern_ids)) and all(int(value, 16) > 1 for value in modern_ids)
    assert all(q["tasks"][0]["advancement"].split("/")[-1] in names for q in quests)
    print(f"Exported {len(legacy_quests)} active quests and {len(quests)} archived quests; stable IDs and advancement references verified. Runtime not implied.")


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")


if __name__ == "__main__":
    generate()
