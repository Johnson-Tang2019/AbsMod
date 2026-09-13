# 仓库核对记录

> 修订：`2026-09-12.repository-progress.1` · 核对日期：2026-09-12  
> 仓库：`Johnson-Tang2019/AbsMod` · 分支：`main` · 固定提交：`2e420203619b5d9cf0f1af7fbc5135ff8858f34f`

## 1. 结论

本轮通过已连接 GitHub 工具重新读取远端，main 仍是 2e42020。相对 2026-09-10 文档包，compare 返回 **identical，ahead_by=0，behind_by=0，total_commits=0，files=[]**。分支列表本轮只返回 main。没有新的远端功能可转入完成栏。

阶段表和物品类已重新读到全文：九项伤害仍统一 7.0F，阶段表没有独立攻速字段，物品类仍传 -2.4F。既有刀光是同一代码快照里的客户端视觉实现，不是长按输入、取消原版蓄力或连击伤害的实现证据。固定提交 docs/ 请求仍为 404。

当前已存在的工程、九阶物品、奶昔和刀光状态沿用同一提交的上轮源码审阅。长按、候选平衡接入、连击、暗黑、器灵、好感、升级和心魔仍不能标为完成。用户本地未提交或未推送的开发不在本轮可见范围。

## 2. 本次查询与历史证据分开

| 证据 | 本轮状态 | 边界 |
| --- | --- | --- |
| main 引用 | 重新查询，2e42020 | 不代表本地 HEAD |
| compare | 重新查询，主线无变化 | 不用日期变化制造开发进度 |
| branches | 重新查询，仅返回 main | 未取得本地工作树 |
| docs/ | 固定提交返回 404 | 不表示 Library 文件不存在 |
| MeowBladeStage / MeowBladeItem | 本轮重读全文 | 不等于已执行构建 |
| 其他源码/注册/渲染/检查入口 | 保留 2026-09-10 同一提交审阅 | 未冒充本轮全部重新读取 |
| 构建通过 | 不变提交说明仍是历史自述 | 本轮未复跑，不能写 VERIFIED_RUNTIME |
| Actions / combined-status | 本轮未重新查询 | 9/10 的 0 条/空结果只作历史记录 |
| 文档和候选公式 | 本轮离线检查，见 PACKAGE_VALIDATION | 不等于 Java、网络或游戏验证 |
| 用户电脑、GitHub 写入 | 没有执行 | ZIP 不会自行更新项目 |

本轮未重新尝试下载仓库完整 ZIP。上一版中提到的 DNS 失败只属于 2026-09-10 的环境记录，不复述为今天的新失败。

## 3. 当前开发边界与任务

继续沿用仓库实际 ModDevGradle / Loom 双加载器构建，不改成旧聊天提议的 NeoGradle；common 仍是注入的共享源码目录。

下一批：**BAL-01 候选数据语义 → AUTO-01 长按输入会话 → AUTO-02 服务端唯一节奏器**。这是建议任务顺序，不是声称任务已启动。完整任务和本地交接模板见 [CURRENT_TASK.md](CURRENT_TASK.md)。

AUTO-03/04 没接通前不能声称取消原版冷却或真实高频命中完成。AUTO-05 复用现有视觉底座，不能把六种轨迹当作六段伤害。器灵支线保留身份/持久化先行，未决定阈值、回剑地点、重击或 Boss 数值不定稿。

## 4. 本轮 API 证据

- N01：`https://api.github.com/repos/Johnson-Tang2019/AbsMod/git/ref/heads/main`，返回 SHA 2e420203619b5d9cf0f1af7fbc5135ff8858f34f。
- N02：`https://api.github.com/repos/Johnson-Tang2019/AbsMod/compare/2e420203619b5d9cf0f1af7fbc5135ff8858f34f...2e420203619b5d9cf0f1af7fbc5135ff8858f34f`，返回 identical / 0 / 空变化文件。
- N03：`https://api.github.com/repos/Johnson-Tang2019/AbsMod/branches?per_page=100`，返回 main。
- N04：`https://api.github.com/repos/Johnson-Tang2019/AbsMod/contents/docs?ref=2e420203619b5d9cf0f1af7fbc5135ff8858f34f`，返回 404。
- N05/N06：下表 R06/R07 为本轮重读全文，blob SHA 与上一快照一致。

## 5. 保留的固定提交源码证据

除 R06/R07 外，下表来源为 2026-09-10 的审阅；因 HEAD 未变化仍适用于同一代码，不表示它们在 2026-09-12 全部重读。完整读取范围、SHA 和日期见 [REPOSITORY_AUDIT.json](REPOSITORY_AUDIT.json)。

| 编号 | 文件 | 支持的事实 |
| --- | --- | --- |
| R01 | [build.gradle](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/build.gradle) | ModDevGradle 2.0.146 / Loom 1.17.20 / Java 25; shared source injection; root build delegates both loaders. |
| R02 | [settings.gradle](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/settings.gradle) | Only :neoforge and :fabric are included; common is a source directory. |
| R03 | [gradle.properties](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/gradle.properties) | MC26.2 and pinned loaders, mod 0.1.0, configuration cache false. |
| R04 | [AGENTS.md](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/AGENTS.md) | English technical names/comments, bilingual READMEs, preserve textures and do not invent progression. |
| R05 | [README.zh-CN.md](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/README.zh-CN.md) | Nine swords/four milkshakes/six recipes; texture and slash parameters; runtime acceptance pending. Metadata and quoted sections read, not asset remeasurement. |
| R06 | [common/src/main/java/com/abyssredemption/absmod/item/MeowBladeStage.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/common/src/main/java/com/abyssredemption/absmod/item/MeowBladeStage.java) | Nine entries all attackDamage 7.0F; no stage speed field. |
| R07 | [common/src/main/java/com/abyssredemption/absmod/item/MeowBladeItem.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/common/src/main/java/com/abyssredemption/absmod/item/MeowBladeItem.java) | Reread on 2026-09-12; damage total conversion and -2.4F speed modifier; no spirit/combo logic. |
| R08 | [common/src/main/java/com/abyssredemption/absmod/client/MeowSlashEffects.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/common/src/main/java/com/abyssredemption/absmod/client/MeowSlashEffects.java) | Observes swinging/swingTime; 32 effect and 32 block caps; per-player visual sequence and world/pause cleanup. |
| R09 | [common/src/main/java/com/abyssredemption/absmod/client/SlashSwingTracker.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/common/src/main/java/com/abyssredemption/absmod/client/SlashSwingTracker.java) | Visual swing restart detection and motion cycling, not attack cadence or damage. |
| R10 | [26.2Fabric/src/main/java/com/abyssredemption/absmod/fabric/AbsModFabricClient.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/26.2Fabric/src/main/java/com/abyssredemption/absmod/fabric/AbsModFabricClient.java) | Registers client tick/extraction/submission callbacks. |
| R11 | [26.2NeoForge/src/main/java/com/abyssredemption/absmod/neoforge/AbsModNeoForgeClient.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/26.2NeoForge/src/main/java/com/abyssredemption/absmod/neoforge/AbsModNeoForgeClient.java) | Client-dist Mod with tick/extraction/geometry callbacks. |
| R12 | [26.2Fabric/src/main/java/com/abyssredemption/absmod/fabric/AbsModFabric.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/26.2Fabric/src/main/java/com/abyssredemption/absmod/fabric/AbsModFabric.java) | Registers sword and milkshake variants, creative tabs and shared commands. |
| R13 | [26.2NeoForge/src/main/java/com/abyssredemption/absmod/registry/ModItems.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/26.2NeoForge/src/main/java/com/abyssredemption/absmod/registry/ModItems.java) | Deferred sword and milkshake registration only. |
| R14 | [26.2Fabric/build.gradle](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/26.2Fabric/build.gradle) | check depends on verifyCommands; verification source set and JavaExec. |
| R15 | [common/src/verification/java/com/abyssredemption/absmod/command/CommandVerification.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/common/src/verification/java/com/abyssredemption/absmod/command/CommandVerification.java) | Calls MilkshakeVerification and SlashVerification; command grammar/permissions; not an actual game server. |
| R16 | [common/src/verification/java/com/abyssredemption/absmod/client/SlashVerification.java](https://github.com/Johnson-Tang2019/AbsMod/blob/2e420203619b5d9cf0f1af7fbc5135ff8858f34f/common/src/verification/java/com/abyssredemption/absmod/client/SlashVerification.java) | Six visual motions, idle persistence, independent players, mirror/dedup/restart, geometry budget, fading. Not GPU acceptance. |

## 6. 后续更新程序

先核对实际目标分支/HEAD，变化时再读差异涉及的源码与测试。记录源码存在、独立测试、仓库自述三个层级；无新提交时更新复核时间和交接信息，不随意勾选 TODO。若本地已领先，先合并本地证据，不能用本包旧进度覆盖。只有工具写入回执成功才能说已保存到 Library，只有真实 commit 才能说已同步 GitHub。
