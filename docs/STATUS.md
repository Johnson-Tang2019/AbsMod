# Ab's Mod 当前状态、TODO 与下一批任务

## 2026-11-19 · 26.1.2 剑灵饰品与井字棋

基线 `main/61691d1`。PLAN-UI-01、PLAN-GUIDE-01 与 PLAN-PORT-01。结缘铃的剑灵详情新增饰品与棋局入口。`SpiritWardrobe` 按灵魂 UUID 保存可重复佩戴的樱花发夹和月牙发夹，`BladeSpiritEntity` 同步选择，Bedrock 头骨骼带动饰品渲染。`SpiritGameMenu` 管理每场独立的井字棋，`SpiritGameScreen` 显示棋盘、结果和重开按钮。注册、原创发夹模型、两款配方、原版进度与中英说明一并接入。见 [剑灵规格](SPIRIT_SYSTEM.md) 和 [26.1.2 指南](PORT_2612.md)。

实机：启动 26.1.2 Fabric 中文客户端并进入隔离世界。通过结缘铃佩戴樱花、切换月牙，核对饰品数量、召回后重新显现的外观和世界存档中的饰品选择。实点棋盘完成一局并重开，确认两项 FTB 任务完成。手册第 31–32 页同步操作，FTB 从 38 项增至 40 项，原 38 项定义逐项比对一致。见 `build/spirit-leisure-2612-client.log` 和 `26.1.2Fabric/run/screenshots/port2612-spirit-{wardrobe,board,adornment}.png`。两款发夹在测试中由夹具提供，生存实作配方、退出重登与多玩家客户端尚未实测。

构建与安装：26.1.2 发布包与 25 项服务端 GameTest 通过，含重复落子、结束后落子和饰品权限测试。26.2 双加载器、1.21.1 兼容构建通过，并核对发布包资源与相应版本边界。26.1.2 Fabric 0.19.2 本机实例已更新 JAR、FTB 章节和中文说明，三份均核对 SHA-256。旧版备份于该实例的 `absmod-backups/20261119-225023`。见 `build/spirit-leisure-2612-release.log`、`build/spirit-leisure-final-26.log`、`build/spirit-leisure-final-1211.log` 与 `build/spirit-leisure-2612-delivery.json`。


## 2026-11-19 · 26.1.2 樱绒梳

基线 `main/5678e2f`。PLAN-LIFE-01、PLAN-GUIDE-01 与 PLAN-PORT-01。新增可反复使用的樱绒梳、候选配方、原创猫爪模型和简中／英文说明。自己驯服的猫会转头并使用它自己的呼噜音色，剑灵低头、耳尾摆动并在超过 4 格时复位。剑灵梳理复用每日普通交流额度，已记录后仍可触发动作。详见 [猫咪玩法](CAT_GENETICS.md)。

主要改动：`GentleBrushItem`、`BladeSpiritEntity` 与客户端渲染状态、Bedrock 模型姿态、`CatGeneticsMixin`、`BladeAffection` 和注册。观察手册新增第 30 页，26.1.2 FTB 增加「梳梳毛，理理发」，共 38 项，原 37 项的编号、依赖和完成条件与原导出一致。

验证：在 26.1.2 Fabric 的隔离客户端为剑灵梳理，核对当日记录、动作同步、正常结束和移出范围后的复位。达到日上限后再次梳理有动作且好感不叠加。实测猫咪交互、遗传与坐姿指令、FTB 新任务完成和梳子材质。证据见 `build/gentle-brush-2612-client-final.log`。他人及超出距离的请求在服务端 GameTest 中被拒绝。多人客户端尚待实测。

构建与安装：26.1.2 发布构建和全部 24 项服务端 GameTest 通过。26.2 双加载器与 1.21.1 兼容构建通过。26.1.2 本机实例的 JAR、章节和中文说明已更新并校验 SHA-256。旧文件备份于 `absmod-backups/20261119-215620`，见 `build/gentle-brush-2612-delivery.json`。构建日志见 `build/gentle-brush-2612-release-final.log`、`build/gentle-brush-build-26-final.log` 和 `build/gentle-brush-build-1211.log`。

## 2026-11-19 · 26.1.2 剑灵关系与当日互动

基线 `main/1ee67cc`。PLAN-UI-01、PLAN-GUIDE-01 与 PLAN-PORT-01。结缘铃伙伴详情显示当前路线和阶段、好感、原剑耐久、下一阶好感门槛和差值、当日交流与生鳕鱼赠礼、活跃同行与战斗额度。刷新重新读取服务端的原账本和灵魂记录。日历按主世界时钟结算，时间倒退沿用当前账本。手册新增第 29 页，26.1.2 的 FTB 好感任务补操作指引。

源码：`CatJournalViews`、`CatLife`、`BladeAffection`、`BladeGrowthStore`、`FieldGuideItem`、双语资源和 FTB 导出。次阶好感直接读取共鸣门槛，普通八阶显示试炼提示，暗黑八阶显示路线封顶。奖励与现有数值沿用当前规则。

实机：26.1.2 Fabric 在隔离客户端打开已显现剑灵详情，验证原剑耐久 1500/1561。完成一次交流与一次赠礼后刷新，好感由 0 到 3，二阶差值由 10 到 7，两个当日记录更新。重复赠礼保留剩余鳕鱼，单独刷新未改写灵魂记录，FTB 好感任务完成。证据见 `build/spirit-activity-2612-final.log`。NeoForge 运行时与多玩家详情查看不在本轮实机验收范围。

验证与交付：26.1.2 发布构建和 24 项 Fabric GameTest 通过，含账本查询权限、重复赠礼和时间倒退。共享代码通过 26.2 双加载器构建及 1.21.1 资源回归构建。本机仅更新 26.1.2 Fabric 0.19.2 的 JAR 和 FTB 好感任务中文说明。原文件备份于 `absmod-backups/20261119-210410`，SHA-256 见 `build/spirit-activity-2612-delivery.json`。构建日志见 `build/spirit-activity-2612-release.log` 和 `build/spirit-activity-build-*.log`。

## 2026-11-19 · 猫咪收藏、家谱与安置

基线：\`main/b8f6fe4\`。任务：PLAN-CAT-01、PLAN-HOME-01／02、PLAN-GUIDE-01、PLAN-PORT-01。观察手册新增独立收藏界面。可按毛色、花纹、眼色组合筛选或只看自家猫，逐个打开已观察的亲本与后代。绒月猫窝、结缘铃、休息与呼唤寻路、伙伴详情及现有召回服务已接入。猫窝和铃的材料与交互为首版原型，详见 [猫咪玩法](CAT_GENETICS.md)。

源码与资源：两个 API 分支的 \`CatLifeStore\`、\`CatLife\`、\`CatHomeGoal\`、\`CatJournalMenu\` 和客户端界面，\`CatNestBlock\` 与 \`BondBellItem\`，各加载器注册、原创模型、配方、掉落、双语说明与进度。手册第 25–28 页和 1.21.1／26.1.2 FTB 同步增加四项目标。导出比对确认各 33 项既有任务 ID、触发条件与依赖一致，新编号为正；两版各 37 项。

实机：26.2 与 1.21.1 NeoForge 在隔离存档打开收藏，点击黑色滤项，沿家谱查看已记录亲属与未知亲本。实测放置、选猫、绑定、走进窝后坐下、铃铛步行呼唤、剑灵召回同一把原剑，重开后核对收藏、家谱、铃铛选择和窝的关联，拆窝后原猫仍在。1.21.1 原版与 FTB 四个新增目标均完成。26.1.2 Fabric 实际点击收藏滤项，检查 FTB 新收藏任务完成。证据见 \`build/cat-life-client-*-final.log\` 与 \`build/cat-life-ftb-2612-final.log\`。

构建：26.2 双加载器、26.1.2 Fabric、1.21.1 NeoForge。两版 Fabric 服务端各 24 项 GameTest，包括多猫分别绑定、拒绝旁人控制、重分配与拆除清理。证据见 \`build/cat-life-release-*.log\`。多人客户端、独立区块卸载顺序、长期生存平衡及个体头像待验收或实现。

本机交付：26.2 NeoForge、1.21.1 NeoForge 与 26.1.2 Fabric 已更新并核对 SHA-256。两个 FTB 实例同步章节与中文说明。旧文件备份位于各实例的 `absmod-backups/20261119-201917`，逐文件记录见 `build/cat-life-delivery.json`。

## 2026-11-19 · 共鸣升阶中断恢复

基线 `main/d8485ab`。对应 PLAN-SPIRIT-03、PLAN-DOC-01、PLAN-GUIDE-01 和 PLAN-PORT-01。两个共享源码分支的 `ResonanceEscrow`、`ResonancePlayerStorageMixin`、`ResonanceMenu` 和 `SpiritTransferJournal` 将原剑、材料、光标和待结算状态与背包一起保存。共鸣之心入口与两加载器的登录事件会核对中断。背包满时留在工坊，腾空后重开取回。同步双语 README、手册第 12 页和两版现有 FTB 共鸣任务。两版各 33 个任务的 ID 和条件保持一致。

实际客户端：26.2 与 1.21.1 NeoForge 分别在各自的隔离世界启动四次，依次测试确认前、原剑和结算凭据落盘后、灵魂升阶档案落盘后强制退出。普通与暗黑二阶恢复时核对原剑数量、余料、原版进度、名称、附魔、耐久损耗、主人、好感和版本。满背包保留光标物品，重复核对只返还一次。26.1.2 Fabric 在单独世界实际点击共鸣按钮，核对原剑身份、重复确认和 FTB 普通二阶完成。日志 `build/resonance-crash-*.log` 与 `build/resonance-recovery-2612-client.log`。

构建：26.2 NeoForge／Fabric、26.1.2 Fabric 与 1.21.1 NeoForge；两版 Fabric 各通过 23 项服务端 GameTest。见 `build/resonance-recovery-final-*.log`。测试夹具只在指定开发参数下启用。

本机安装：26.2 NeoForge、1.21.1 NeoForge 与 26.1.2 Fabric 均已替换 JAR 并校验 SHA-256。1.21.1 和 26.1.2 同步各自 FTB 任务与中文说明，26.2 继续用原版进度和手册。各实例旧文件保存在 `absmod-backups/20261119-190946`，逐文件记录见 `build/resonance-recovery-delivery.json`。

尚待实测：双玩家同时操作、玩家死亡途中恢复、整机断电或管理员回滚不同时点存档。共鸣进阶规则和数值继续沿用现行规格。下一项是猫咪收藏与安置，26.3 仍暂停。

## 2026-11-19 · 战斗反馈与共鸣缺项

基线 `main/42c8ae1`。本批推进 PLAN-COMBAT-02、PLAN-UI-01 的共鸣部分及配套 PLAN-DOC-01／PLAN-GUIDE-01／PLAN-PORT-01。用 [当前任务表](CURRENT_TASK.md) 记录所有路线的实现、验收和待定项。星喵工艺已定位作者发布页与项目仓库，作为现有路线完成后的参考。

战斗：两个 API 分支增加 `BladeCombatStatusPayload` 和 `BladeCombatHud`，只同步本玩家的实际连击、重击准备与冷却。客户端按手持刀和槽位显示并提供独立显隐键。NeoForge 通道版本为 5。Fabric 26.1.2 使用对应 HUD 注入点。原攻击节奏、数值、阈值及动作包保持原有行为。

共鸣：`ResonanceMenu` 的预览材料提示分别显示投入和缺量，`ResonanceScreen` 显示好感差值和当前升阶限制。手册第 3、9 页、双语 README 和两版 FTB 落实操作说明，两版各 33 个任务的 ID、依赖与触发条件已逐项核对。

客户端验证：26.2、1.21.1 NeoForge 在隔离测试世界确认暗黑就绪、前摇、冷却和普通有效主命中后显示 1 层。重击仍一次结算 90 点伤害。共鸣测试确认 8/10 好感及 4 铜锭缺量，再补足材料验证升级、重复确认和关闭返还。26.1.2 Fabric 实测共鸣、FTB 普通二阶进度和暗黑状态同步。证据见 `build/combat-hud-{26,1211,2612}-client-final.log` 及 [连击与冷却截图](../art/animation/combat-status/)。

发布构建：26.2 两加载器、1.21.1 NeoForge 和 26.1.2 Fabric 通过。两版 Fabric 各通过 23 项服务端 GameTest，26.2 的独立验证也通过状态包编解码和连击衰减边界。日志为 `build/combat-hud-release-26.log` 的 GameTest 段，以及 `build/combat-hud-release-{26-final,1211,2612}.log`。

已安装三个本机实例，并逐文件核对 SHA-256。两个有 FTB 的实例同步本模组任务和翻译。各实例备份位于 `absmod-backups/20261119-180225`，完整校验记录见 `build/combat-hud-delivery.json`。实机测试使用独立开发世界。

仍需完成：共鸣强制中断对账、多人联机压力、伙伴每日互动状态、猫咪收藏和安置、暗黑关键阶原画、同魂心魔试炼及九阶生存入口。显隐键和细分中断提示另有实机交互验收。26.3 暂停。

## 2026-11-19 · 粉色共鸣、战斗辨识度与 26.1.2

本批沿用本地 `main/1e89ffa`。CONFIRMED：共鸣改为粉色模组界面，现有功能移植 26.1.2，游玩线路同步 FTB。选择本机已有的 26.1.2 Fabric 实例。26.3 开发暂停。

源码已接入独立 `absmod:resonance` 菜单和屏幕，分别展示原剑、投入材料、预览、所需材料及好感。确认按钮使用标准容器按钮同步，服务端保留门槛校验、每次打开最多升一阶、只读预览及关闭返还。普通与暗黑阶段数值、九阶原图和剑灵交接行为沿用。PLAN-COMBAT-01 的暗黑普攻改为短紫弧，重击增加专属下劈、命中环和结果提示。详见 [成长实现](IMPLEMENTATION_PROGRESSION.md) 和 [战斗规范](CONTINUOUS_COMBAT.md)。

26.1.2 复用现有 Fabric 玩法及资源，独立适配第一人称持物、活动界面访问、HUD 和世界渲染事件。两版 FTB 各 33 项，加入粉色界面操作和暗黑重击说明。1.21.1 沿用原 SNBT 和 ID，26.1.2 使用对应 JSON5、中文翻译和确定性正数 ID。[26.1.2 安装与依赖](PORT_2612.md)

验证：四个目标构建通过。26.1.2 Fabric 和 26.2 Fabric 各通过 23 项实际 GameTest，涵盖战斗、升阶、剑灵及遗传。26.2／1.21.1 实际启动独立 NeoForge 测试世界，确认两个路线可升阶、重复确认不再扣除材料、退出返还；现有八阶暗黑重击一次造成 90 点主伤害。26.1.2 实际启动 Fabric 并点击粉色按钮，确认二阶、耐久、身份、材料与原剑返还，FTB Quests 26.1.2.10 加载 33 项和 67 条中文键，并自动完成普通二阶任务。1.21.1 FTB 加载 33 项，测试途中有 7 项完成。截图见 [共鸣前后](../art/progression/resonance-custom/) 和 [暗黑挥刀](../art/animation/combat-styles/)。

对应日志：`build/resonance-release-{26,1211}.log`、`build/resonance-client-{26,1211}.log`、`build/port-2612-client-confirmed.log` 和 `build/port-2612-gametests-final.log`。测试使用开发实例和一次性世界。26.1.2 开发运行使用 Fabric Loader 0.19.5，本机 PCL 实例为 0.19.2。PCL 整包启动、多玩家共鸣压力及升阶事务的强制退出恢复另待验收。

安装已更新 26.1.2 Fabric、26.2 NeoForge 和 1.21.1 NeoForge 三个本机实例，并验证已复制文件的 SHA-256。26.1.2 安装正式 FTB 依赖，同时备份旧 Fabric API。1.21.1 合并本模组的中文任务键。备份目录为各实例的 `absmod-backups/20261119-172636`，逐文件记录见 `build/resonance-delivery.json`。

## 2026-11-19 · 剑灵跨维度与中断交接

PLAN-SPIRIT-02、PLAN-SPIRIT-03（剑灵交接）、PLAN-GUIDE-01、PLAN-PORT-01。基线 `main/0c2cecd`。现有未跟踪美术参考保留。换维度和重登后可在安全地面等待剑灵。客户端用虚化剑查询最后位置或重试自己尚未结束的交接。放入原交接槽前会先核对占用与原剑副本。实现方案、旧记录兼容及范围见 [剑灵恢复](IMPLEMENTATION_SPIRIT_RECOVERY.md)。

源码：两个版本的 `SpiritCompanionTravel`、`SpiritTransferJournal`、`SpiritTransformationService`、`BladeSoulStore`、`SpiritCarrier`、`BladeSoulRecord` 和 `BladeSpiritEntity`。三目标分别注册共享服务器轮询，1.21.1 另适配集成主机库存保存。双语 README、手册第 10 页和 FTB 显现任务同步。33 个任务 ID、条件与依赖保持原样。

验证：26.2 Fabric／NeoForge 和 1.21.1 NeoForge 发布构建通过，23 项 Fabric GameTest 通过。发布 JAR 检查排除夹具、包含双语定位与恢复文案。日志为 `build/spirit-transfer-release-26.log` 和 `build/spirit-transfer-release-1211.log`。

实机：用两个 NeoForge 客户端分别启动独立简体中文测试世界。先验证目的地受阻会保留旧载体，开放地面后跨维度跟上，再构造五个中断点并强制以 86 退出。重开同一世界验证另一维度剑灵重新加载后跟上、更新的交接日志覆盖旧 SavedData、显现与跨维度中断恢复、召回前后断点、占槽保留及腾槽后恢复。六把测试剑逐一核对名字、附魔、耐久、主人和好感，每个魂仅检出一把可用剑。重复调用未增加物品。1.21.1 两阶段实际载入 33 项 FTB。日志 `build/spirit-transfer-{26,1211}-{prepare,recover}-final.log`。准备阶段故意以非零退出，判据为 `SPIRIT_TRANSFER_HALT_READY`，重开阶段为 `SPIRIT_TRANSFER_RESTART_PASS` 和构建成功。

未验收真实双玩家联机、整机断电、硬盘损坏、管理员或外部模组改写存档后的冲突。共鸣升阶事务的强制中断恢复属于后续范围，普通八升九继续等待同魂心魔。测试从开发客户端启动，安装目录的玩家世界未用于注入中断。

安装：两个 PCL 实例已备份并更新，1.21.1 同步对应 FTB 章节及中文键。备份目录为各实例的 `absmod-backups/20261119-133308`。发布和已安装 JAR 的 SHA-256 一致：

- `26.2-NeoForge_26.2.0.87_miao` · `f71f4fe2ce264e599603634fc57cdbccb8033197a989a1b15eddaa9e31711988`
- `1.21.1-NeoForge_21.1.265_miao` · `6fe26c75a7b3393f115167b3041a858692f4cb737014e72ce96198f90809cbf6`

## 2026-11-19 · 剑灵安全追赶阶段记录

| 模块 | 已接入 | 下一步 |
| --- | --- | --- |
| 版本与引导 | 26.2 Fabric／NeoForge，1.21.1 NeoForge。24 页手册，1.21.1 可选 33 项 FTB 任务 | 按各版本验证受影响场景 |
| 剑灵 | 无敌、潜行显现、攻击与手动召回。已接入同维度安全追赶和最后位置查询 | 跨维度自动行为、重登自动对账、中断交接 |
| 普通／暗黑 | 普通一阶总攻速 1.6，连击属性已接入，普通生存可至八阶。暗黑八阶封顶并有独立重击 | 仍待心魔与同魂资格，普通八升九锁定。分路线动作及特效待做 |
| 猫咪 | 2048 个普通组合和 256 个粉色组合，遗传、家谱数据、观察与预测已接入 | 收藏管理、猫窝、结缘铃 |

本批 PLAN-SPIRIT-01、PLAN-SPIRIT-02 部分、PLAN-DOC-01、PLAN-GUIDE-01、PLAN-PORT-01。基线 `main/b6bf86b`，两处未跟踪美术原件保留。修改双版本 BladeSpiritEntity、SpiritCarrier、SpiritTransformationService、SpiritInteraction、SpectralBladeItem，及双语翻译、手册、原有 FTB 显现任务说明、双语 README 和独立测试夹具。

当前实现与实际游戏日志见 [安全跟随与失联找回](IMPLEMENTATION_SPIRIT_RECOVERY.md)。已启动两个 NeoForge 客户端，并验证安全追赶、区块卸载／重载、原剑完整交还与重复拒绝。Fabric GameTest 23 项通过。自动跨维度、重登对账、强制关服、真实双玩家尚未验收。以下按日期保留历史记录，其中早期未实现列表反映当时状态。

发布构建：`build/spirit-recovery-build-26.log` 和 `build/spirit-recovery-build-1211.log` 通过，含旧载体字段缺省和坐标写盘重读检查。发布 JAR 包含双语定位文案，排除测试夹具。核对 33 项 FTB 任务的章节、任务 ID 和依赖，没有变化。

两个 PCL 实例均已更新，旧 JAR 及 1.21.1 同名任务和语言文件备份于各实例下 `absmod-backups/20261119-125724`。复制后的 JAR SHA-256 为：

- `26.2-NeoForge_26.2.0.87_miao` · `1e27410c208651d76e0e110c3ec6ca17540ba317ec85c9310a398afee21261b2`
- `1.21.1-NeoForge_21.1.265_miao` · `aa5cad52998e9244eddb68cb3b96748c3aaaf3864d70a9827f89dc1574bd8c65`

游戏实测使用开发客户端的独立新世界。更新安装时只替换本模组 JAR、既有 FTB 章节与其语言键。

## 历史实现与验收记录

## 2026-11-01：组合毛色与浅奶粉色（CAT-03 / GUIDE-04）

CONFIRMED：用户要求类似热带鱼的丰富组合、极罕见粉色，获取方式由开发者决定；追加指定粉色参考原版粉色美西螈，改为浅奶粉色。基线main/c2224e0，原始模型未提交目录保留。

SOURCE_PRESENT：CatCoat＋CatGenome可选字段兼容旧存档；8普通底色×8花纹×4白斑×4眼色×2重点色=2048普通组合，粉色另256。出生后保存组合和粉色状态，读取不重抽，旧猫不重画。D/d、B/b、C/cs照常遗传；额外外观特征按亲本选择并各有1/32随机机会，是明确的游戏近似。粉色是幻想变异，PROPOSED初版概率：无粉亲本1/16384、一粉1/1024、两粉1/128；无染料获取、无保底、无隐藏粉色携带。

美术：内置image_gen按美西螈奶粉色调整源图，再由C#材质烘焙器组合花纹/白斑/眼色。不是2304次单独AI绘制。4608个成年/幼猫PNG，成年2304个不同哈希；幼猫2232种不同图，部分双色白斑覆盖小尺寸斑点，成年后才区分。原六套与九阶剑图未改。资源/提示词见art/cats/diversity/ASSETS.md，样本页preview.html。

GUIDANCE：24页中英手册，粉色与组合概率可观察；4项新进度，FTB33任务。猫路线不阻挡喵刀主线。1.21.1注册2304额外变体；26.2数据注册，不把新数据文件打进旧版。

VALIDATION：26.2双端与1.21.1发布build通过；Fabric23项GameTest通过（26万余次普通配对粉色统计、概率分档、2304ID注册/唯一性、外观CODEC、三位点和实体NBT）。两版本实际进入世界检查8组成年/幼猫样本，粉色由专用夹具明确指定，不冒充自然稀有出生验收；资源重载、21–24页中文、8项毛色进度通过。1.21.1 FTB33任务、13项完成；独立服务器重新载入恢复13项，遗传/外观/家谱NBT与剑灵回归通过。发布包检查JSON、4608图、Java21字节码、排除夹具；检查成年图哈希不重复。未逐只目视验收2304组合，未做多人繁育、PCL按钮启动或长期概率/性能平衡。

记录：build/cat-diversity-{tests,client-26,client-1211,server,release-26,release-1211}.log；CAT_DIVERSITY_DELIVERY.json；art/cats/diversity/{26.2,1.21.1}/。26.2仍有本机OSHI计数器警告，1.21.1皮肤服务曾超时，不影响上述结果。

安装：两个指定_miao实例发布JAR已备份更新，SHA256核对相同；1.21.1合并33任务及本包语言键，其他章节/世界未覆盖。备份absmod-backups/20261101-174032。修改包括双版本CatCoat/CatGenome/FieldGuideItem、变体注册/资源生成、翻译/进度/FTB、验证夹具、文档和双语README。提交推送已获持续授权；此记录写入时待执行，最终见Git历史与交付回复。

## 2026-11-01：三位点猫遗传与六种新毛色（CAT-02 / GUIDE-03）

CONFIRMED：继续完善遗传、生成贴图、补齐FTB，验证后安装两个版本并提交推送。基线main/322197b；保留未提交原始模型目录。

SOURCE_PRESENT：26.2 common与1.21.1适配分支同步D/d、B/b、C/cs独立双亲遗传、完整基因型/组合概率、旧记录隐藏位点迁移。六种外观为巧克力、淡紫及海豹/蓝/巧克力/淡紫重点色；只覆盖纯黑、蓝灰、暹罗来源，其他图案保持原图并说明限制。旧猫加载不重画。保留喵刀数值、原九阶图、剑灵规则。12张PNG由内置image_gen生成后按UV打包，源图和说明在art/cats/genetics/。

GUIDANCE：中文/英文20页手册、新增4项原版进度；FTB29任务稳定ID导出，新增任务有配对方法与概率，三重隐性依赖淡紫和重点色。26.2继续使用原版进度/手册；FTB仅对1.21.1交付，不宣称26.2兼容。

VALIDATION：26.2 Fabric/NeoForge及1.21.1 NeoForge发布build全部通过；Fabric22项GameTest通过，含64000次固定种子独立位点抽样、旧格式迁移、六种真实Cat后代变体及NBT恢复。两版本实际启动客户端创建独立测试世界，显示成年/幼猫六种外观、资源重载、四新进度、17–20页手册；1.21.1 FTB29任务加载、9项自动完成。独立服务器重新载入该世界，恢复9项FTB完成记录，三重隐性幼猫基因/外观/家谱NBT测试及既有剑灵回归通过。检查所有发布包12图哈希一致、JSON可解析、20页翻译齐全、1.21.1 Java21字节码、无测试类。

证据：build/cat-gametests.log、cat-client-final-26.log、cat-client-final-1211.log、cat-server-1211.log、cat-release-26.log、cat-release-1211.log；可跟踪截图art/cats/genetics/{26.2,1.21.1}/；CAT_GENETICS_DELIVERY.json记录安装SHA256。26.2启动日志存在本机OSHI/Windows性能计数器错误，未阻止进世界与测试；不隐去此环境警告。首次构建的JDK/任务名及可选夹具API错误已修复，以最终日志为准。

安装：已备份并更新两个_miao实例的发布JAR，逐文件SHA256相符；1.21.1合并本章节和语言键，未覆盖其他章节或世界进度。备份absmod-backups/20261101-171427。未通过PCL按钮启动，游戏验收来自开发运行目录。

NOT_TESTED：Fabric图形客户端、双玩家猫繁殖、长期生存平衡、全部原版花纹新外观、温度/性连锁遗传。不存在“所有真实杂交已实现”的结论。无新增疾病或性别限制。创始携带率与图案选择是游戏近似。

改动范围：CatGenome/CatGeneticsMixin/FieldGuideItem双分支、旧版变体注册、资源/翻译/4进度、FTB导出与任务包、GameTest/可选客户端夹具、文档与双语README。提交/推送：用户已授权，本记录写入时待执行；最终状态以Git历史与交付回复为准。

## 2026-11-01：1.21.1 NeoForge 与 FTB 任务适配（PORT-01 / GUIDE-02）

CONFIRMED：继续适配1.21.1 NeoForge和FTB，验证后安装并提交推送。以本地3c07088及未提交移植代码为基线，原始模型参考目录保留。

SOURCE_PRESENT：独立1.21.1NeoForge（NeoForge21.1.265、Java21字节码）及common/src/minecraft1211版本适配源；保留ModDevGradle、九阶原图/ID、暗黑八阶、奶昔、成长/重击、剑灵切换与猫遗传。1.21.1使用旧版模型绘制API、单独客户端入口/第一三人称Mixin、透明虚化模型；Bedrock几何、逐面UV和196骨骼保留。17张虚化模型引用原图，未重画原始资产。

VALIDATION：独立build与26.2双端回归build通过；Java21 class版本、JSON、17个虚化模型和发布包排除测试类检查通过。实际启动1.21.1客户端进入世界，普通/暗黑升级、保留耐久比例与原剑身份、重击12tick前摇/单次90主目标伤害/重复及冷却拒绝、16页中文手册、剑灵显示/召回、第一人称与刀光、资源重载完成。模型fallback=false、bones=196。FTB25任务加载、中文界面、自动完成真实进度已验证；独立服务器重新载入同一测试世界，恢复10项任务完成记录，并断言猫繁殖/NBT家谱、断刃保留、显现/无敌/攻击召回、已加载合成配方。

记录：build/port-final-client.log、build/port-dedicated-server-retry.log、build/port-release-build.log、build/port-26.2-regression-build.log；截图art/compatibility/1.21.1/。修复实测发现的TieredItem覆盖阶段耐久、ServerPlayer仅允许原版书ID开页、猫繁殖Mixin描述符和进度背景资源路径。FTB开发模式实体图标缺失已补共鸣之心图标描述；未作为新角色原画。

安装：D:/Program Files/PCL2/.minecraft/versions/1.21.1-NeoForge_21.1.265_miao，原mods为空。已安装本模组、FTB Quests2101.1.37/Library2101.1.39/Teams2101.1.11/Architectury13.0.11，合并本章节和中文语言表，逐包SHA256一致。未覆盖存档或任务进度；详见PORT_1211_DELIVERY.json。

边界：这是客户端自动驱动＋独立服务器实际运行验证，尚未做1.21.1双玩家联机、全姿态/全奶昔效果/长期生存平衡或PCL按钮启动。1.21.1 Fabric未在本批范围；禁止跨版本混装JAR或倒退26.2存档。正式数值仍为PROPOSED原型。原模型再分发授权状态沿用现有NOTICE，不擅自改许可证。

## 2026-11-01：第一人称位置、倾斜与遮挡（MOTION-04）

CONFIRMED：用户明确优先第一人称刀的位置、倾斜和遮挡。静止握柄移向外侧下方并稍远离镜头，加入18度透视偏转、降低刀尖并减小静止尺寸；挥刀保留原始采样，收招随权重平滑回到新握持，左手镜像。无新增伤害或第三人称动作修改。

实际启动NeoForge进入新世界，检查一阶右手、九阶左手静止、关闭刀光挥刀、开启刀光及资源重载样本；截图 art/animation/first-person-grip/，日志 build/first-person-grip-client.log（BUILD SUCCESSFUL）。双加载器build通过（build/first-person-grip-build.log）。仅FOV65/1280×900的本地自动驱动视觉样本；未重复多人测试，非用户美术验收。侧刃瞬间过薄及其他FOV仍待打磨。

改动：BladeGrip.java、MotionVisualTest.java、本文/STATUS/CURRENT_TASK、双语README和截图。猫遗传/任务保持上一批状态；1.21.1客户端API移植与FTB实际运行仍未完成，不安装未通过验证的移植包。

## 2026-11-01：猫遗传、中文手册和任务引导（CAT-01 / GUIDE-01）

本地基线 main/29a11c8，保留未提交的原始模型参考。实现 D/d 稀释双亲遗传、父母UUID/代数持久化、黑色dd蓝灰外观、观察与配对概率。书+生鳕鱼合成16页中文手册，配套四项原版进度；FTB导出25个稳定ID任务，普通/暗黑/猫分线，不把未实现九阶Boss列为可完成任务。

验证：双加载器build及Fabric21项GameTest通过（build/cat-guide-final-validation.log），包含真实猫繁殖方法、实体NBT保存重载谱系与固定种子分布。NeoForge实际进入世界，修复新书页组件先同步后开书；中文客户端书页、进度、黑/蓝灰外观和50%预测已截图检查（build/cat-guide-chinese-client.log，art/cats/）。首次英文测试的书页未打开，不能当成功证据；修复后加入失败断言。

边界：只模拟稀释位点，图案选择是游戏近似，其他稀释纹理未新增；未验收Fabric图形客户端和猫的多人繁殖。FTB导出仍待1.21.1实际加载/完成/存档验证。1.21.1独立Java21/NeoForge21.1.265构建正在移植，不是可安装成品；禁止用26.2 JAR冒充。

用户追加要求中文与改善持刀。已切换开发客户端及两个本机实例的语言设置；保留英文翻译。第一人称缩小/下移静止握持并恢复装备过渡，第三人称保留肩部固定点、不再移植放大的MMD肩部位移；真实游戏截图在art/animation/grip-revision/，build/motion-grip-client.log通过。属于首轮修正，不代表用户已认可完整动作。

26.2实际安装实例已改名为26.2-NeoForge_26.2.0.87_miao，现有模组核对匹配。最终双端build及21项GameTest通过；已备份并安装26.2发布JAR，两端共享资源一致、测试夹具已排除，复制哈希相同。详见CAT_GUIDE_DELIVERY.json。1.21.1仍未安装。

## 2026-10-31：合成、成长、暗黑重击与原版进度（PROG-01 / PROG-02 / DARK-01 / ADV-01）

交付状态：本批已完成本地 Git 提交。HTTPS 推送连续出现 Connection was reset；当前已核对远端 main 仍为 fb84ffc，尚未确认推送成功。此前本地 75f74c3 也随本批等待推送。最终远端结果以交付回复为准。

CONFIRMED：用户授权实施成长方案，并明确本次加入暗黑八阶、独立重击和原版进度引导。数值仍为可迭代 PROPOSED 原型，非最终生存平衡。

SOURCE_PRESENT：普通/暗黑保留数据的初始配方，共鸣之心与菜单，二至八阶保留身份/附魔/使用比例的升阶事务；好感每日收入账本；普通连击属性与逐阶耐久、断刃保留；暗黑八阶、可改键 G 重击（12 tick前摇、160 tick冷却、1.8倍基础伤害、普通怪12 tick眩晕及后续100 tick免疫）；中英原版进度树。心魔未实现，普通八升九仍锁定。新增共鸣之心/暗黑基础图由内置图像生成工具制作，原九阶剑图未改，暗黑八阶暂共用基础图。

VALIDATION：两端构建与 Fabric 20 项 GameTest 最近一次通过；NeoForge真实世界共鸣开关/发包升级/重放拒绝/返还通过；暗黑重击只造成一次90伤害并正确拒绝重复/冷却请求；原版进度真实授予与显示已检查。截图 `art/progression/`，日志 `build/progression-client.log`、`build/progression-final-validation.log`。实际启动 Minecraft，不将测试夹具或编译等同于全生存/多人验收。

剩余：心魔资格与Boss、完整崩溃恢复、真实双玩家同时升级、Fabric图形客户端、长期平衡、独立运行时配置、暗黑逐阶原画及专属动作。保留模型原始参考目录未提交。实现文件、边界与完整证据见 [实施记录](IMPLEMENTATION_PROGRESSION.md)。


## 2026-10-31：MOTION-03 与成长方案

实际检查：双端 build 与共享验证通过，Fabric 12 项 GameTest 通过（build/motion-peers-validation.log），两端正式 JAR 均排除联机测试入口。Minecraft 主机和观察者已正常退出。

新增连续挥刀前 0.75 tick 的前姿态过渡，服务端节奏和伤害不变。已启动真实 NeoForge 游戏及两个独立客户端，第二客户端经 TCP 进入同一测试世界，记录 connected=2 并保存 54 张关闭刀光的观察者截图；详见 [实际测试记录](MOTION_MULTIPLAYER_VALIDATION.md)。本机双客户端不等于公网延迟或全部姿态验收。

新增[合成、成长与数值 v2](MEOW_BLADE_PROGRESSION_PROPOSAL.md)：九阶材料/好感/耐久、保留身份的升级事务、暗黑对照与重击候选。全部新增参数为 PROPOSED，尚未改运行时配方和数值。下一批优先完善动作边缘显示与完整操作回归，再按评审后的设计接升级。


## 2026-10-31：动作退出边界（MOTION-02）

修复 BladeMotionPlayer / BladeFirstPersonMixin：资源预设加载失败时不接管第一人称；打开界面、缺少交互控制器或挖掘期间拒绝本地迟到确认，并在渲染采样时再次检查。远端玩家不受本地界面限制。服务器攻击及伤害逻辑未变。

本次离线双端 build 与既有共享自动检查通过，日志 build/motion-boundary-build.log；未启动 Minecraft，不能视为游戏验收通过。保留原有动作、多人及逐阶握持验收待办。

## 2026-09-14：真实挥刀动作初版（MOTION-01 / AUTO-05）

CONFIRMED：以 SlashBlade: Resharped `6e2a0a092fb794d7ea56fd83452869674f3ab1c7` 为可替换参考，保留本地 `78ff086` 上全部未提交工作。独立 `slashblade_baseline` 预设已导入原始 VMD/PMD 并解码为明确的 absmod_motion_v1；A3 拆成两个单次授权片段，未复制伤害/眩晕/位移回调。来源和单独许可范围见 [素材记录](../art/animation/slashblade_baseline/NOTICE.md)。

源码状态：参考动作已导入；第一人称武器位置/旋转及握持已接入；第三人称身体/手臂姿态与原持物层已接入。使用 26.2 渲染状态，不引入旧 playerAnimator。原版 swingTime 重置已从确认入口移除；沿用 attackId 去重、服务端节奏与伤害，只扩展表现时序。九阶原图、剑灵与数值保持原状。

验证：双加载器最终 build 通过（build/baseline-motion-final-build.log）；实际五片段采样、收招连续性、快照隔离与协议检查通过；Fabric 12 项 GameTest 通过（build/baseline-motion-validation.log）。实际启动 NeoForge 进入新世界，交付关闭/开启刀光、第一/第三人称、左右手、重载恢复与观察其他玩家样本的[连续截图](../art/animation/slashblade_baseline/game/preview.html)。

游戏验收仍不完整：自动视觉样本不等于真实鼠标/联网测试；其他玩家样本是本地 RemotePlayer，尚无双客户端联机验证。Fabric 客户端、全移动姿态、完整动作/刀光对齐与逐阶握持仍待检查。已有连击层数尚未接入攻速加成，快速确认测试不冒充连击系统完成。详细改动和剩余项见 [实现记录](IMPLEMENTATION_SLASHBLADE_BASELINE.md)。未提交或推送。


本轮验证完成：双加载器最终build与持续输入接续CPU检查通过（build/spirit-auto-recall-final.log）；Fabric 12项GameTest通过；实际启动NeoForge进入世界，服务端攻击意图驱动自动召回并确认原剑恢复（build/spirit-auto-recall-client.log）。该测试未模拟物理鼠标/真实客户端网络延迟；Fabric客户端和多人尚未实测。

## 2026-09-14：剑灵无敌与攻击自动收回（SPIRIT-04）

CONFIRMED：用户要求剑灵无敌，攻击自动收回。当前剑灵拒绝hurtServer伤害，包括绕过原版无敌标记的伤害；此前死亡扣罚设计不在此版本触发，不擅自删除历史设定或实现死亡惩罚。

攻击触发采用主手对应虚化剑的现有攻击输入（包括空挥，不包括挖方块）。服务端先验证包序号、槽位、维度、玩家状态、灵魂主人和有效实体，再经同一交接服务恢复原剑，之后由原有唯一攻击节奏器处理攻击。原版攻击包对虚化剑同样拦截，避免先打一次无武器伤害。虚化剑到同身份真剑的同步替换保留按住输入，其他换物/槽位仍要求松键。停止攻击不自动重新显现。

只召回同维度当前已加载的匹配剑灵；实体未加载、身份缺失或不属于玩家则不发剑、不强制加载区块。行政直接移除、跨维度、崩溃恢复和完整变形动画不属于此次伤害免疫实现。


验证完成：双加载器 build 通过（build/spirit-switch-final-build.log）；Fabric GameTest 全部11项通过（build/spirit-switch-gametest.log），包括普通右键不初始化、潜行显现/占位/交互召回、原剑数据、占槽/非主人/重复召回拒绝。NeoForge世界自动驱动切换与显示检查通过；真实鼠标网络、多玩家与Fabric客户端仍未实测。

## 2026-09-14：手动形态切换与虚化剑（SPIRIT-02）

CONFIRMED：潜行右键主手喵刀显现；潜行右键自己的剑灵召回；显现后在原槽位保留虚化剑。当前以潜行使用入口显式初始化新剑身份，已有身份缺档拒绝重新初始化。召回目标为所选主手槽位，仅接受空槽或对应身份的虚化剑，不覆盖其他物品。

SOURCE_PRESENT：共用SpiritTransformationService交接原剑，保留完整ItemStack和身份；九阶独立spectral占位物品不继承MeowBladeItem，无剑属性或连击能力。对应原剑纹理在渲染时施加40%不透明度淡粉着色，原始九张PNG不变。召回清除主人背包内同身份虚化占位并交还原剑。遗留在外部容器/掉落物中的占位仍无武器或发剑能力，也不计为实体剑副本。

保留护士酒狐模型与全部此前未提交修改。未实现自动战斗切换、渐变变身动画、跨区块远程召回或崩溃恢复闭环；不补写死亡/好感扣罚规则。本地基线78ff086；未提交或推送。详细验证见 IMPLEMENTATION_SPIRIT_SWITCH.md。


## 2026-09-14：使用用户指定护士酒狐模型（ART-02）

CONFIRMED：用户指定直接使用 art/护士酒狐女仆包。以本地 78ff086 与已有未提交修改为准；之前粉白候选外形未获确认，暂停该造型修订。

当前默认为 absmod:models/entity/nurse_wine_fox.geo.json 与 absmod:textures/entity/blade_spirit/nurse_wine_fox.png。原模型196骨骼及默认256×256贴图逐字节复制，几何/旋转/层级/逐面UV完整保留；按原包0.6比例显示。沿用Bedrock渲染路径和基础行走/看向/尾摆绑定，没有引入GeckoLib或YSM。服务端规则、剑灵身份与九阶剑图未改。

实际检查：双端 build 通过（build/nurse-fox-final-build.log），包含实际资源加载、护士骨骼绑定、姿态隔离、失败回退与恢复检查。NeoForge实际进入新测试世界，正面/侧面/背面/斜面已截图检查；资源重载前后 fallback=false、bones=196（build/nurse-fox-client.log）。测试实体为无AI静态外观样本，未验收主人跟随/行走、多实体及Fabric客户端。基础动作自动检查不能代替原包动画验收。

原包依赖的Gecko/Molang专用动作未接入；互斥表情、狐狸形态、GUI背景保留在模型中但不同时显示。来源、哈希与未确认再分发授权见 art/blade_spirit/nurse-wine-fox/NOTICE.md。未提交/推送。改动及剩余项见 [接入记录](IMPLEMENTATION_NURSE_WINE_FOX.md)。


## 2026-09-14：粉白曲面剑灵与重绘贴图（ART-02）

CONFIRMED：用户要求重绘贴图，人物不要原版方正模型。以本地 main / 8a035c5 为基线，开工前工作树干净。

本批新增原创21骨骼/2314多边面模型 blade_spirit_ethereal.geo.json、AI重绘1254×1254图集 ethereal_atlas.png；脸部、长发、裙摆、袖腿和尾巴为真实多边面曲面。Bedrock适配模块新增有限 poly_mesh 子集（索引三角/四边面），仍采用26.2模型提交路径，不依赖GeckoLib/YSM。保留旧资产/占位失败回退及服务端行为。

双加载器 build 与实际资源/法线/UV/姿态隔离/错误索引及加载恢复检查通过，日志 build/ethereal-final-build.log。浏览器正/背/斜/侧面检查（开启背面剔除）已执行，预览现在对应当前新模型。未启动Minecraft，本轮不声称游戏内光照、动画和多人验收。未提交/推送。文件清单与剩余项见 [修订记录](IMPLEMENTATION_ETHEREAL_SPIRIT.md)。


## Bedrock 渲染适配首批（2026-09-13，ART-02 / SPIRIT-CLIENT-01）

CONFIRMED：正式外观采用车万女仆 BedrockModel 同类的骨骼模型路线；不限制为原版人形＋128×64共享皮肤，不以 GeckoLib/YSM 替代。本批只改变客户端；absmod:blade_spirit、服务端身份/跟随/显现召回、伤害/好感及九阶原图保留。

SOURCE_PRESENT：独立 client-bedrock 源码适配模块、Bedrock 1.12.0 单 geometry 加载与任意父子骨骼、箱式/逐面 UV、26.2 模型提交接口；原创 10 骨骼测试模型和 128×128 PNG；行走/服务端视角驱动的转头/尾摆；缺失或损坏资源回退到旧占位模型，下次重载重新尝试。主路径不再继承 HumanoidModel。测试模型是结构验证资产，不是正式猫娘美术。

实际验证：NeoForge 首先编译通过，最终双端 build 和共享检查通过（build/bedrock-final-build.log）；CPU 检查覆盖 60 面法线/UV、层级、A/B/A 姿态交错、独立实例、错误模型/贴图回退和恢复。Fabric GameTest 服务器 10 项通过，类加载日志未见本模组 client 或 bedrock.client 类（build/bedrock-server-classload.log）。

客户端：实际启动 NeoForge，首次因项目路径单引号造成启动参数截断；已修复开发启动参数转义。重试日志确认资源加载完成及测试模型 10 骨骼成功加载（build/bedrock-client-retry.log）。只达到启动/加载检查，未进入世界观察剑灵，测试进程随后停止；不声明实体显示、真实动画、多实体或 F3+T 视觉验收通过。Fabric 客户端及 NeoForge 独立服务器本轮未运行。

main / 2e42020，保留已有未提交修改，未提交/推送。源码清单、许可证版本和剩余项见 [本批记录](IMPLEMENTATION_BEDROCK_RENDERER.md) 与 [源码依据](../client-bedrock/UPSTREAM.md)。旧预览页仍展示之前的橙模型，不是本批运行模型的证据。


## 临时切换为现成车万女仆模型（2026-09-13）

用户明确要求先使用车万女仆中的模型。当前客户端外观已切换为指定整合包内的橙（touhou_little_maid:chen），使用原配贴图，转换 29 个骨骼、304 个有效面；闭眼覆盖层暂不显示。修正 Bedrock 与当前模型的旋转方向差异，原生走路/转头继续工作；未接入车万女仆专用表情、尾巴或动作脚本。

自制粉白模型/贴图仍保留，显现/召回和存档逻辑不变。原整合包未改动。第三方来源与作者名单记录于 art/blade_spirit/BORROWED_MODEL_NOTICE.md，项目许可证未变；发布前仍需核对对应资源再分发条款。本轮未提交或推送。

检查：浏览器正背侧面预览、双加载器构建与共享检查（build/borrowed-spirit-final.log）；未启动 Minecraft，不声称客户端玩法或视觉验收完成。改动：borrowed_cat_spirit.json/png、BladeSpiritModel/Renderer、SpiritModelVerification、OBJ/预览导出脚本、双语 README 与状态记录。


## 侧视图修正（2026-09-13）

用户指出侧面诡异。本批 ART-02 调整头/躯干厚度、把悬空花饰放回太阳穴附近、后发后移，并用较平的正面五官区域避免眼睛绕到侧面。保持原图与图集。refine_spirit_model.py、模型 JSON、OBJ 和预览已更新。实际执行双加载器 build、共享几何/比例/动画检查及浏览器侧面截图检查通过；日志 build/spirit-side-validation.log，截图 build/spirit-preview-side.png。未启动 Minecraft，未提交/推送；当前仍为待打磨草模，未宣称正式美术完成。


## 最新修订：依据本地女仆模型校准比例（2026-09-13）

ART-02：按用户提供的整合包路径，实际读取 tlm_custom_pack 内橙、豪德寺三花、博丽灵梦的模型 JSON，测量基础骨骼。三者主体头高 8、头下 18、总高 26（不含帽子/猫耳/外层）；本模组此前总高 32、腿长 12，确有过于修长问题。

已将剑灵调至基础总高 26 / 头高 8（3.25 头身），腿长 9 / 腿宽 3，手臂长 8；头/身体原点 Y=6，髋 Y=15，脚底保持 Y=24。裙摆和头发同步缩短、定位。保留曲面造型与粉白原图/图集；未复制外部模型或改动整合包。具体来源路径、SHA-256、测量范围和验证见 [实测记录](SPIRIT_PROPORTION_REFERENCES.json)。这些是模型实现尺寸，不是角色年龄或最终游戏身高设定。

改动：refine_spirit_model.py、模型 JSON、OBJ/preview、export_spirit_model.py 的预览取景、SpiritModelVerification 的比例/动画后关节检查、双语 README 与本批记录。实际执行双加载器 build、几何/法线/UV/动画重置及比例检查、共享资源一致性、浏览器正背斜侧检查，均通过。未启动 Minecraft，真实光照/走动/多人仍待验收。main/2e42020，已有未提交工作保留；未提交或推送。


## 最新外观修订：减少方盒轮廓（2026-09-13）

ART-02：用户否定上一版方正外观，要求参考车万女仆修改。已将头部改为收下颌的环状曲面，头发改弯曲收尖发束，身体收腰，袖口/腿部圆化，裙摆改环绕钟形曲面，尾巴改连续弯管，花瓣圆化。1768 四边面；原图、图集和旧模型备份均保留。独立骨骼与动画继续使用共享渲染器。

参考范围：核对车万女仆公开仓库支持可替换模型的架构；本轮具体模型文件读取失败（API 403），未复制其模型或贴图，不声称精确复刻其拓扑。主要修正是本模组原创几何。

变更：BladeSpiritGeometry 新增曲面 quad 载入、blade_spirit.json、refine_spirit_model.py、export_spirit_model.py、OBJ/预览、SpiritModelVerification、双语 README 与规范补充。双加载器 build、几何/UV/法线/动画重置、共享资源一致性和浏览器正背斜侧视觉检查通过，见 [证据](SPIRIT_CURVED_VALIDATION.json)。未启动 Minecraft，光照与实际走动穿插仍待游戏内验收；仍 main/2e42020，未提交或推送。


## 最新美术实现：参考图建模（2026-09-13）

ART-02 普通路线外观切片：按用户提供的粉白猫娘参考图制作 33 个立体部件（198 四边面），包括分层长发、尖猫耳、花饰/水晶、外扩裙摆和五段弯尾；新图集经 AI 编辑重新排布，再用逐面 UV 接入。参考原图原样保存在 art/blade_spirit/reference.png，旧占位及九阶刀图保留。

模型源 JSON、OBJ/MTL 和可旋转预览已交付；[模型说明](../art/blade_spirit/README.md)、[验证记录](SPIRIT_MODEL_VALIDATION.json)。实际执行双加载器 build、共享检查、33 部件几何/UV/法线/动画重置检查，以及浏览器正面/背面/斜侧视图检查，均通过。未启动 Minecraft 客户端或服务器；游戏内光照、动作穿插和多人外观仍未验收。

当前仍 main / 2e42020，保留全部已有未提交工作。本次变更：BladeSpiritModel/Renderer、新增 BladeSpiritGeometry、模型 JSON/图集、SpiritModelVerification/CommandVerification、art/blade_spirit 交付物、tools/art/export_spirit_model.py、双语 README 和相关文档。未提交或推送。角色正式身高/年龄、暗黑模型和变形动画未擅自定稿。


## 最新本地补充：喵娘剑灵（2026-09-12）

SPIRIT-01 / SPIRIT-02 **部分实现**：粉白猫耳尾巴占位实体、主人/灵魂保存、节流跟随、管理员手动显现/指定空槽召回、原剑完整副本、形态/revision/operationId 记录及失败拒绝。详见[实现记录与变更文件](IMPLEMENTATION_SPIRIT_PROTOTYPE.md)、[美术需求](SPIRIT_ART_REQUIREMENTS.md)。

本地仍为 main/2e42020，有大量已有未提交工作，均保留。双加载器构建、共享检查、Schema 1/2 文件保存重读、Fabric 9 项模组测试 + 1 项框架测试通过，证据见 [SPIRIT_VALIDATION.json](SPIRIT_VALIDATION.json)。本次只启动 Fabric GameTest 服务器，未启动客户端或 NeoForge 服务器；视觉、真实跟随与中断恢复未验收。未提交/推送。

SPIRIT-03/04 死亡结算与自动召回未实现；中断/旧档恢复尚未闭环，不能称完整器灵系统。正式美术可稍后提供，现有喵刀原图无需重画。BAL-02 层数状态已完成但增伤/攻速加成仍待接入；下文对应记录保留。


## 当前本地工作树（2026-09-12，BAL-02 状态切片）

本地 main/HEAD 为 2e42020，源码未提交。新导入文档包只核对远端，不能覆盖本地实现事实；下方远端快照原文保留。

- BAL-01：九阶候选属性已实现。AUTO-01～05：输入会话、唯一节奏器、局部满强度/受伤窗口、服务端刀光确认已接线。CREATIVE-01 专属标签、SOUL-01/02 身份存储原型保留。
- BAL-02（部分实现）：新增有效主命中结果、服务端层数、5～20 阶段上限、重复/旧动作拒绝、65 tick 首次衰减、同刀松键保层、换持物/身份/槽位/世界及死亡/断线清理；inspect 与日志可查。
- BAL-02 尚未接入增伤/攻速修饰或客户端 HUD。本次不将状态计数称作完整连击加成。
- 本批变更：BladeComboState、BladeAttackScope、BladeAttackExecutor、BladeCombatServer、LivingDamageMixin、MeowBladeStage、CombatVerification、BladeCombatGameTests、中英文 README 与进度文档。
- 实际验证：双加载器构建、共享逻辑测试与 Fabric 测试世界通过；新增生命/吸收命中确认测试，8 项模组测试加 1 项框架测试，共 9 项通过。状态测试覆盖去重、上限、衰减、切换。详细结果见 [本批记录](IMPLEMENTATION_COMBO_STATE.md)。
- 本次启动了 Fabric GameTest 服务器并正常结束；未启动客户端或 NeoForge 服务器，真实输入/网络、盾牌、多人及嵌套反伤兼容仍待完整验收。现有贴图足够，本批不需要新图。

下一步：按旧层数结算接入局部增伤及唯一临时攻速加成，补换刀清理/外部修饰器/真实玩家集成测试。未提交或推送。

## 以下为导入包的远端快照


> 文档修订：`2026-09-12.repository-progress.1`  
> 核对日期：2026-09-12 · main：`2e420203619b5d9cf0f1af7fbc5135ff8858f34f`  
> **结论：与 2026-09-10 文档包相比，远端 main 新增提交 0 个、变化文件 0 个。** 本轮重新核对远端引用、比较结果、分支列表、阶段表与物品类，并刷新任务交接记录；没有编写游戏代码。  
> 本地未推送代码不可见，不能据本文件判断用户电脑没有新进展。

## 1. 状态词典

`SOURCE_PRESENT` = 已存在实现源码；`REPORTED_PASS` = 仓库描述过去通过，非本轮复跑；`NOT_IMPLEMENTED` = 在核对的远端入口/源码未见该功能；`NOT_TESTED` = 未取得对应运行验证；`PROPOSED` = 设计提案；`TBD` = 用户规则未决定。
下文已完成勾选仅用于实现/文档工作本身。“需求已确认”用单独文字记录，不拿它冒充代码完成。

## 2. 当前基线

MC 26.2 / Java25 / mod0.1.0；NeoForge26.2.0.77 + ModDevGradle2.0.146；Fabric Loader0.19.5 / API0.159.0+26.2 / Loom1.17.20；Gradle9.5.1。实际只有`:neoforge`和`:fabric`两个加载器子工程，common是共享源码目录。

最新提交：`2e42020`，粉白刀光与六种视觉轨迹；其父提交为`80cc5c2` wrapper修复。更早已有四种奶昔和普通九阶资源。
证据：[REPOSITORY_AUDIT.md](REPOSITORY_AUDIT.md)。本轮 branches 查询仅返回 main；固定提交的 docs/ 请求仍返回 404。本包没有推送到 GitHub，也没有访问 D:\Ab'sMod；此处仅报告远端进度。

本轮重读 MeowBladeStage.java：九阶仍全部为 7.0F，尚无阶段攻速字段；MeowBladeItem.java 仍传入 -2.4F。其他已存在功能沿用 2026-09-10 对同一固定提交的源码核对，不冒充本轮逐文件重读。

## 3. 功能状态矩阵

| 模块 | 已存在内容 | 未完成/未验收 | 证据 |
| --- | --- | --- | --- |
| 构建工程 | 两加载器入口、统一root wrapper、共享源码/资源配置 | 本轮未跑构建，尚无多MC版本实现 | R01-R03/R14 |
| 普通九阶 | Item/Stage、固定ID、两端注册、创造栏、资源和命令 | 参数仍7.0/1.6；升级/连击未落地 | R05-R07/R12/R13 |
| 奶昔 | 四种饮品、六配方和测试命令，注册已核对 | 饮用、传送、剩余物和配方书实测待定 | R05/R12/R13/R15 |
| 刀光 | 原版挥手观察、六种轨迹、左右镜像、总量/距离和暂停清理、两端客户端钩子 | GPU/多人视觉；按真实attackId去重联动；不是新手臂动画 | R05/R08-R11/R16 |
| 长按连斩 | 用户需求与详细设计文档 | 输入包、服务端节奏、取消蓄力、局部受伤窗口和实测均未完成 | 当前入口无相应实现 |
| 数值/连击 | 普通9阶/暗黑8阶候选表与离线历史报告 | Java表接入、连击状态、提速、衰减与真实DPS | R06/R07 + BALANCE |
| 暗黑路线 | 粉紫/8阶/下界合金起源/重击眩晕设计 | 暗黑注册、资源、重击触发与实现 | 已核对注册列表无暗黑 |
| 器灵与好感 | 粉白/粉紫猫娘、双形态与升级好感需求及档案提案 | 身份存储、实体、跟随、转换、死亡回剑、好感 | 无对应已核对注册/实现 |
| 成长/心魔 | 普通8升9需好感与心魔；暗黑8封顶 | 配方、升级、Boss、通关归属及存档 | 文档需求，不是实现 |
| 正式模型 | 已有九张2D剑图与程序化刀光 | 真正3D刀/UV、猫娘模型/动画和动画库选型 | R05 + ART_GUIDE |

### 3.1 已存在代码的维护清单

- [x] 保留普通一阶`absmod:meow_blade`及二至九阶ID。
- [x] 两加载器喵刀/奶昔注册和各自客户端刀光入口均已有源码。
- [x] `MeowSlashEffects`与`SlashSwingTracker`已实现视觉检测、序列与清理。
- [x] 自动检查入口已存在并接入Fabric check任务。
- [ ] 本轮复跑两加载器构建与Java自动检查。
- [ ] 两端单机/独立服务器/多人实际运行验收。
- [ ] 把本包文档合并到仓库，并按需合并AGENTS片段。

## 4. 推荐下一批：长按输入与服务端出刀节奏

**任务状态：READY_PROPOSED，尚无证据表明开发已经开始。** 此排序依据用户最近明确提出的长按需求，不表示强制暂停器灵支线。
范围与步骤详见[CURRENT_TASK.md](CURRENT_TASK.md)。先确认本地分支/未提交改动，再选最小切片。

| ID | 当前状态 | 下一步与完成条件 |
| --- | --- | --- |
| BAL-01 | NOT_IMPLEMENTED | 阶段数据加入明确的总攻速语义；候选伤害/攻速可用于原型；同步命令和tooltip，不重加玩家基础值 |
| AUTO-01 | NOT_IMPLEMENTED | 现有攻击绑定、按住/松开会话、失焦/界面/使用物品/换手等停止、超时/序号校验 |
| AUTO-02 | NOT_IMPLEMENTED | 单击与长按共用服务端动作预算，唯一attackId，小数进度、重按不送首刀、低TPS不补刀 |
| AUTO-03 | NOT_IMPLEMENTED | 只有授权喵刀攻击局部替换原版蓄力/折扣，不能全局满蓄力或只隐藏HUD |
| AUTO-04 | NOT_IMPLEMENTED | 局部受伤窗口/归属/护盾/取消事件/多人兼容验证，不使用直接setHealth |
| AUTO-05 | PARTIAL_VISUAL_ONLY | 复用现有六轨迹刀光；将新授权/预测/旧swing事件去重；当前只有视觉底座 |
| AUTO-06 | NOT_TESTED | 两加载器单人/多人、高低TPS与FPS、范围和输入边界、真实命中统计 |
| BAL-02 | NOT_IMPLEMENTED | 有效主命中叠层、旧层数算本刀、超时衰减、切换/断线清临时值 |
| BAL-03 | NOT_TESTED | 5→6目标节奏与真实命中分开验证；不是面板/刀光计数 |
| BAL-04 | PARTIAL_VISUAL_ONLY | 已要求长按与既有刀光集成，对应AUTO系列；不再重复开发一套 |
| BAL-05 | NOT_TESTED | 对小怪/Boss、盔甲、重击未来收益和耐久成本调平衡 |

第一批可以只交付AUTO-01/02的测试骨架和BAL-01前置，但未接伤害前不能声称“取消冷却已完成”。不把所有器灵/Boss/暗黑/网络/渲染一次写完。

## 5. 器灵支线保留，第一批仍是身份与存储

- [ ] SOUL-01 `BladeSoulRecord/Store`、服务端UUID初始化、字段权威来源、读写与缺失档案处理。
- [ ] SOUL-02 ItemStack引用、升阶迁移接口、不覆盖新阶属性、重复身份检测。
- [ ] SPIRIT-01 占位猫娘实体、主人关系、跟随、保存与加载。
- [ ] SPIRIT-02 唯一转换服务、operationId/revision、空槽/满背包保护、恢复状态。
- [ ] SPIRIT-03 死亡只结算一次、好感/耐久入口、回剑交付；卸载不是死亡。
- [ ] AFF-01 长期好感服务/来源/调试查询，与临时连击分离。
- [ ] AFF-02 相处/互动/共同战斗收益和升级门槛接入，未定数值不默认为0。
- [ ] SPIRIT-04 自动战斗召回/脱战恢复及正式动画，须先决定触发规则。

自动攻击原型不应为此创建第二套永久器灵UUID。当前没有soulId时只做保守的短期攻击会话，持物变动不明确就清会话，不把相同item ID视为同一把武器。

## 6. 后续功能队列

- [ ] DARK-01 暗黑1～8注册、独立参数/名称/资源，不改变普通ID。
- [ ] DARK-02 重击输入、倍率、眩晕对象/时长/递减规则定案与实现。
- [ ] PROG-01 两路线初始与分阶升级、数据迁移、材料/好感门槛。
- [ ] BOSS-01 心魔形态/技能/场地/多人归属定案。
- [ ] BOSS-02 心魔实体与普通8→9服务端解锁，不能只靠客户端标记。
- [ ] ART-01 九张当前原画与游戏显示验收，最终2D/3D/UV方案。
- [ ] ART-02 普通粉白/暗黑粉紫器灵模型、共享骨骼/阶段层与动画。
- [ ] RELEASE-01 版本、依赖、许可/第三方来源、双端运行回归；不默认改许可证。

旧女仆双人床是早期功能方向，未形成当前工程实现规格，保留在后续想法池，不插队替换当前喵刀任务。

## 7. 尚需用户决定的规则

好感范围/各阶门槛/收益/死亡扣值；器灵生命/免疫、回剑位置、零耐久处理、主人死亡/跨维度、多器灵、自动回手触发；暗黑重击触发/倍率/眩晕；升级材料与存档迁移策略；Boss与PvP。
各阶基础数值已有可测试草案，不再写“完全没有设计值”，但不能伪称已逐项确认或实测平衡。

## 8. 验证与证据日志

| 项目 | 状态 |
| --- | --- |
| 远端 main、compare、分支列表、docs/ | 2026-09-12 重新查询；无新增主线提交 |
| 阶段表 / 物品类 | 本轮重新读取，仍是占位属性 |
| 其他源码与 Java 检查源码 | 沿用 2026-09-10 同一提交证据，本轮未重新执行 |
| 双端构建 | 最新提交/README称历史通过；本轮未跑 |
| Actions 运行 | 2026-09-10 查询为 0 条；本轮未重查，不是当前 CI 结论 |
| combined-status | 2026-09-10 查询为空；本轮未重查，不当作 CI 通过 |
| 新的文档/JSON/公式检查 | 见PACKAGE_VALIDATION.json |
| 连斩输入参考模型旧报告 | 历史保留，本轮未复跑 |
| Minecraft/专用服务器/GPU/真实DPS | 本轮均未运行 |

## 9. 本轮修订记录

### 2026-09-12

- 重新确认 main = 2e42020；compare 返回 identical / ahead_by=0 / files=[]。
- 本轮分支列表只返回 main；未取得任何本地未提交进度。
- 重读阶段和物品类，没有把 BALANCE_PROFILE 的设计数值误认为游戏已接入。
- 长按、冷却替换、实际连击、暗黑、器灵与升级等 TODO 状态不变；下一批仍为 BAL-01 + AUTO-01/AUTO-02。
- CURRENT_TASK 增加可填写的交接记录模板，将“方案完成、源码完成、构建通过、运行验收”分开。
- 核对资料来源与日期，重做当前包的文件、链接和数值一致性检查；历史模拟报告保持原字节。
- 无新需求，不改变九阶/八阶数值、粉白/粉紫、器灵和心魔设定；不提交代码或文档到远端。

### 历史：2026-09-10

2026-09-10：main仍为2e42020；读取实际插件、模块、两端注册/客户端入口和自动检查代码。合并CONTINUOUS_COMBAT与COMBAT_INPUT；将战斗下一批和器灵支线分开；旧设计/故事/数值保留，测试状态按证据细分；没有把文档完成当成功能完成。
历史长按/器灵/平衡细节保留在各专项，文件改名映射见COMBAT_INPUT.md，包级更新见DOCUMENT_CHANGELOG.md。
