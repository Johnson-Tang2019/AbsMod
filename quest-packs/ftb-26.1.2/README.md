# Ab's Mod 26.1.2 Fabric 任务包

将本目录的 `config/ftbquests/quests` 合并到 26.1.2 Fabric 实例。此任务包覆盖普通刀、暗黑刀、共鸣、剑灵、好感及猫繁育，共 43 项。陪伴路线包括收藏、家谱、安置、饰品及三种棋局，生活支线新增「奶昔茶会」。完成观察手册前置后，合成并放置奶昔茶盘，摆入任意奶昔后空手取回一杯即可完成。普通九阶的心魔试炼仍待开发。

FTB 依赖为 [Quests 26.1.2.10](https://www.curseforge.com/minecraft/mc-mods/ftb-quests-fabric/files/9456216)、[Library 26.1.2.12](https://www.curseforge.com/minecraft/mc-mods/ftb-library-fabric/files/9456007) 和 [Teams 26.1.2.5](https://www.curseforge.com/minecraft/mc-mods/ftb-teams-fabric/files/9405341)。本地验证一并加载了 [Architectury 20.1.18](https://www.curseforge.com/minecraft/mc-mods/architectury-api/files/9651461)。

运行 `python tools/quests/export_quests.py` 可同步导出 26.1.2 与 1.21.1。两套任务共用原有主线进度，樱绒梳、剑灵饰品、三种棋局和奶昔茶会属于 26.1.2 的任务内容。26.1.2 使用 JSON5、单独的中文文件和确定性正数任务 ID，1.21.1 保留已有 SNBT 和 ID。两个版本的 FTB 存档分别使用。

格式依据为 [FTB Quests 26.1.2 更新记录](https://github.com/FTBTeam/FTB-Quests/blob/main/CHANGELOG.md) 与 [任务文件解析](https://github.com/FTBTeam/FTB-Quests/blob/main/common/src/main/java/dev/ftb/mods/ftbquests/quest/BaseQuestFile.java)。
