# Ab's Mod quest export / 任务导出

DRAFT: intended for Minecraft 1.21.1 NeoForge and FTB Quests 2101.x. Runtime acceptance is pending; do not install this into 26.2.

当前为待验证导出包，不代表1.21.1模组移植完成。26.2使用原版进度与观察手册。

Regenerate with `python tools/quests/export_quests.py`. The chapter contains 25 advancement tasks with stable IDs, no automatic rewards, and separate normal-blade, dark-blade and cat paths. Normal stage 9 is intentionally excluded until its boss gate exists. All tasks refer to real `absmod:progression/*` IDs.

Source schema: FTBTeam/FTB-Quests branch `1.21.1/main`, commit `dc94c46a6a9ae806fb5921159513448fcedc8816`, `AdvancementTask.java` and `Chapter.java`. Only schema names are used; no upstream Java or assets are copied.

After runtime validation, copy only `config/ftbquests/quests/chapters/absmod_progression.snbt` into the corresponding instance. Back up any existing chapter with this filename. Do not replace the entire quest directory or delete world/team progress. Keep generated IDs stable when editing descriptions or positions.

验证后仅合并本章节，备份同名文件，不覆盖整个任务目录，不清空玩家/队伍进度。首次导入后检查任务显示、依赖、已有进度补判、完成保存与重新载入。
