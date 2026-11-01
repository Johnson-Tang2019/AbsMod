# Ab's Mod quest export / 任务导出

Validated on Minecraft 1.21.1 NeoForge 21.1.265 and FTB Quests 2101.1.37: 25 quests loaded, real advancement detection and 10 completed quests restored on a dedicated server. Do not install the FTB runtime jars into 26.2.

已完成1.21.1运行验证与安装。中文任务、自动完成和存档重载已实测；不代表多人全流程或最终平衡验收。26.2仍使用原版进度与观察手册。

Regenerate with `python tools/quests/export_quests.py`. The chapter contains 25 advancement tasks with stable IDs, no automatic rewards, and separate normal-blade, dark-blade and cat paths. Normal stage 9 is intentionally excluded until its boss gate exists. All tasks refer to real `absmod:progression/*` IDs.

Source schema: FTBTeam/FTB-Quests branch `1.21.1/main`, commit `dc94c46a6a9ae806fb5921159513448fcedc8816`, `AdvancementTask.java` and `Chapter.java`. Only schema names are used; no upstream Java or assets are copied.

Copy only `config/ftbquests/quests/chapters/absmod_progression.snbt` plus this pack’s translation keys from `lang/zh_cn.snbt` into the corresponding instance. Back up any existing chapter with this filename. Do not replace the entire quest directory or delete world/team progress. Keep generated IDs stable when editing descriptions or positions.

仅合并本章节和本包中文键，备份同名文件，不覆盖整个任务目录，不清空玩家/队伍进度。首次导入后检查任务显示、依赖、已有进度补判、完成保存与重新载入。
