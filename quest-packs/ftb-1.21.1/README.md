# Ab's Mod quest export / 任务导出

0.3.0 adds three quests for the first successful compass trail, anklet pickup and bottle of rain. Pages 42–44 of the guide explain the blade, game and daily gift requirements. The 48 previously shipped quest and task IDs are preserved.

新增「循须寻伴」「落地拾穗」「接一瓶月露」。取得信物后按任务说明使用一次即可完成对应目标。

Validated on Minecraft 1.21.1 NeoForge 21.1.265 and FTB Quests 2101.1.37: the 0.2.0 chapter loads 48 quests (37 existing IDs, six companion activities and five workshop objectives). The four new cat tasks use positive quest and task IDs. Earlier runs verified that a dedicated server retained 13 completed quests. Do not install the FTB runtime jars into 26.2.

已完成1.21.1运行验证与安装。中文任务、自动完成和存档重载已实测；不代表多人全流程或最终平衡验收。26.2仍使用原版进度与观察手册。

Regenerate with `python tools/quests/export_quests.py`. The chapter contains 51 advancement tasks with stable IDs, no automatic rewards, and separate normal-blade, dark-blade and cat paths. The cat path includes four collection, family tree, nest, and bell goals. Normal stage 9 is intentionally excluded until its boss gate exists. All tasks refer to real `absmod:progression/*` IDs.

Source schema: FTBTeam/FTB-Quests branch `1.21.1/main`, commit `dc94c46a6a9ae806fb5921159513448fcedc8816`, `AdvancementTask.java` and `Chapter.java`. Only schema names are used; no upstream Java or assets are copied.

Copy only `config/ftbquests/quests/chapters/absmod_progression.snbt` plus this pack’s translation keys from `lang/zh_cn.snbt` into the corresponding instance. Back up any existing chapter with this filename. Do not replace the entire quest directory or delete world/team progress. Keep generated IDs stable when editing descriptions or positions.

仅合并本章节和本包中文键，备份同名文件，不覆盖整个任务目录，不清空玩家/队伍进度。首次导入后检查任务显示、依赖、已有进度补判、完成保存与重新载入。
