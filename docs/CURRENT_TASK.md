# 下一批开发任务单

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


## 最新用户要求：使用本地实际模型（2026-09-13）

已按指定整合包找到完整车万女仆模型，读取橙/三花/灵梦作比例参考，并完成 26:8 基础身高与头高、9 长度腿部的骨架修订。详见 STATUS 和 SPIRIT_PROPORTION_REFERENCES.json。下一步为游戏内动作与光照验收，不再把“没有模型文件”当作当前阻碍。


## 最新用户外观修正（2026-09-13）

CONFIRMED：上一版人物过于方正，要求参考车万女仆修改。曲面轮廓修订已接入并通过构建与浏览器检查，见 STATUS 顶部；游戏客户端验收仍待完成。


## 2026-09-13 用户指定的美术任务

CONFIRMED：按提供的粉白猫娘图制作模型并重新排布贴图。ART-02 普通模型切片已接入双端，见 STATUS 最新补充和 art/blade_spirit。待游戏内检查光照、跟随行走、穿插与多人显示；这一美术任务不改变器灵转换规则或未完成的中断恢复。


## 最新用户决定与本地执行

2026-09-12：继续开发喵娘剑灵，允许先用占位贴图，完成后提供具体美术需求，可参考车万女仆的渲染方式（CONFIRMED）。本批已完成 SPIRIT-01/02 手动闭环切片，见 [实现记录](IMPLEMENTATION_SPIRIT_PROTOTYPE.md)；下一步优先补中断恢复与客户端验收。BAL-02 的状态切片和未接入属性加成边界保持不变。


> 本地补充（2026-09-12）：输入/自动伤害、创造标签、身份存档及 Fabric 运行时检查已存在；本批继续 BAL-02 有效主命中与层数状态。属性加成尚未接入。以 [STATUS](STATUS.md) 和 [本批记录](IMPLEMENTATION_COMBO_STATE.md) 为准；下文“未实现”仅指远端快照。


> 修订：`2026-09-12.repository-progress.1` · 固定基线：`2e420203619b5d9cf0f1af7fbc5135ff8858f34f`  
> 任务状态：**READY_PROPOSED**，不是已启动/已完成的开发声明。

## 1. 本轮建议目标

先做普通喵刀“已在主手时按住攻击键连续出刀”的输入/节奏骨架；旧刀光继续复用。长按为用户明确目标，保留自定义动作间隔、具体网络协议和精确数值仍是提案。
2026-09-12 复核：远端仍为固定基线 2e42020，分支列表仅返回 main；当前没有远端新功能可以移入完成栏。本地未提交或未推送代码不可见，开工前必须核对工作树。若本地领先，应保留其实现并记录证据，不能照本表回退。

## 2. 必读与核对文件

依次读 `AGENTS.md`、`CONTRIBUTING.md`、本目录README、STATUS、CONTINUOUS_COMBAT、BALANCE和BALANCE_PROFILE。涉及长期身份时再读SPIRIT_SYSTEM。

现有入口：

```text
common/src/main/java/com/abyssredemption/absmod/item/MeowBladeStage.java
common/src/main/java/com/abyssredemption/absmod/item/MeowBladeItem.java
common/src/main/java/com/abyssredemption/absmod/command/ModCommands.java
common/src/main/java/com/abyssredemption/absmod/client/MeowSlashEffects.java
common/src/main/java/com/abyssredemption/absmod/client/SlashSwingTracker.java
26.2Fabric/src/main/java/com/abyssredemption/absmod/fabric/AbsModFabricClient.java
26.2NeoForge/src/main/java/com/abyssredemption/absmod/neoforge/AbsModNeoForgeClient.java
26.2Fabric/build.gradle
common/src/verification/java/com/abyssredemption/absmod/command/CommandVerification.java
```

实际用ModDevGradle，不重搭工程。候选22/5.0不是当前代码，当前仍7.0/-2.4。

## 3. 最小切片与检查点

### 切片A：BAL-01数据语义

设计表已存在。优先给阶段表加基础总攻速字段，并让武器/命令/说明读取同一来源。数值来自PROPOSED表，明确是测试平衡。保留ID和素材。
总伤害包含默认玩家基础值；总攻速不能直接当构造修正重复相加。`/absmod stages`若原来写死1.6须一起核对。
完成门槛：数值和tooltip/命令一致，两加载器编译；未跑游戏时保留实测TODO。

### 切片B：AUTO-01输入会话

客户端读取游戏“攻击/破坏”绑定，不硬编码鼠标编号。开始/更新/停止消息含会话与递增序号，不携带可信伤害/好感/层数。
服务端按玩家、世界、持物和可操作状态校验；界面/失焦无法直接由服务器读取，需要停止消息及超时兜底。
普通挖方块/物品使用/其他武器不被劫持。断线、换世界、切物品、死亡、旁观和输入过期终止会话。
完成门槛：能看到合法会话变化，重复/旧包不能重开或刷首刀。

### 切片C：AUTO-02唯一节奏器

按服务端游戏tick推进，保留小数余量。单击与长按同一动作预算，一tick最多一个主动作，停止和空挥不积攒多刀，低TPS不墙钟补刀。
五到六刀/秒是候选基准；稳定六刀不能无条件取整成每4tick一刀。读取含连击的属性后不要再乘一次同样加速。
每次授权建立唯一attackId；此阶段可仅调试输出，不同时执行原版攻击和自定义伤害。
完成门槛：单击、快点、松开重按不比同攻速长按更快；重复包不重复授权；数据异常安全拒绝。

## 4. 当前明确排除

不同时实现猫娘模型、好感最终阈值、进化配方、暗黑八阶、心魔、新的自动瞄准或范围伤害；不擅自改变刀光审美和资产。
AUTO-03/04没做时，不声称取消原版蓄力/高速有效命中已经完成。不得用全局无限攻速、清全局受伤时间或直接setHealth代替。
器灵处于实体形态时不能临时生成另一把剑出刀；自动召回触发仍TBD。第一批只处理已经合法装备的普通喵刀。

## 5. 后续接线

AUTO-03选择单一近战伤害入口，局部替换蓄力门槛/缩放，保留护甲/护盾/暴击条件/归属/取消事件。
AUTO-04验证目标受伤窗口、嵌套/反伤/多人/箭矢/燃烧。AUTO-05复用已有Geometry/Renderer，旧swing观察器与新attackId不能双播。
只有有效主命中才进入BAL-02叠层；六种视觉轨迹不是六段伤害。未命中可空挥但不刷Buff或共同战斗好感。

## 6. 测试与交付模板

根目录执行（属于建议执行命令，本包没有运行它们）：

```powershell
.\gradlew.bat :fabric:verifyCommands
.\gradlew.bat :fabric:build :neoforge:build
git diff --check
```

随后按CONTINUOUS_COMBAT游戏矩阵检查双端客户端/独立服务端和多人。离线调度模拟、Java检查、构建、游戏测试分开报告。

交付应写明：修改文件、实现的任务ID、仍然TODO、命令及真实结果、游戏是否启动、截图/日志位置；同步STATUS及两份README，不自动提交或推送。

## 7. 可直接给代码AI的指令

```text
以当前本地工作树为准，先读取AGENTS、CONTRIBUTING和docs/README.md。
根据docs/CURRENT_TASK.md开始最小切片：BAL-01必要前置，以及AUTO-01/AUTO-02输入/节奏骨架。
先列计划和既有实现，避免覆盖未提交改动。
保留原物品ID、奶昔、两加载器与六轨迹刀光。只操作本Mod状态，不做全局无冷却。
沿用明确标为PROPOSED的候选表，不发明未定重击、好感和Boss规则。
未接伤害/受伤窗口时明确标为调试骨架，不声称玩法完成。
按实际执行情况更新STATUS，分别记录自动检查、构建、游戏实测。
只完成最小切片，不自动提交或推送。
```

器灵并行入口仍是SPIRIT_SYSTEM第23节的身份与持久化任务，不因本单排队而被删掉或默认完成。


## 8. 本地执行后的交接记录模板 [PROPOSED]

此模板供实际编写代码的会话填写，本次没有填造结果。一次只推进一个可验收切片；不因用户说“继续”就把整个模块标为完成。

```text
记录时间：
当前分支 / HEAD：
工作树状态（是否存在未提交改动）：
任务 ID：BAL-01 / AUTO-01 / AUTO-02 / 其他已列任务
改动文件：
实现范围：
仍未接入的接口：
自动检查：命令、结果、日志位置；未运行则写 NOT_RUN
两加载器构建：命令、结果、日志位置；未运行则写 NOT_RUN
游戏验收：加载器、单机/服务器、场景、结果；未运行则写 NOT_TESTED
更新的 TODO 项及对应证据：
下一步最小任务：
是否提交 / 推送：只记录实际结果
```

验收进阶规则：
- BAL-01 的纯数据/编译结果不能证明真实攻击频率已变化。
- AUTO-01/02 的会话与节奏日志不能证明伤害已接通。
- AUTO-03/04 的实现存在但未验证时，继续保留冷却兼容与多人测试项。
- 现有六轨迹刀光不能充当上述任何服务端验收的证据。
- 不覆盖本地已有的实际测试结果；冲突时合并记录并注明不同 commit/环境。
