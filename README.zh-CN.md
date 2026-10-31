# Ab's Mod

动作预设不可用时，第一人称自定义渲染会退出。界面打开或正在挖掘时忽略迟到的本地动作确认，远端玩家表现保持独立。

可替换的 `slashblade_baseline` 预设已在 26.2 接入真实第一人称武器变换及第三人称身体/手臂姿态，读取移植的 SlashBlade VMD 采样，不复制其伤害回调。保留九阶原图及服务端战斗。双加载器构建与 Fabric 12 项 GameTest 通过；已交付实际 NeoForge 连续截图，完整多人/输入/姿态验收尚未完成。见[实现与边界](docs/IMPLEMENTATION_SLASHBLADE_BASELINE.md)、[游戏截图](art/animation/slashblade_baseline/game/preview.html)、[来源许可](art/animation/slashblade_baseline/NOTICE.md)。

剑灵免疫伤害。手持虚化剑攻击时，先自动收回对应已加载剑灵，再由原攻击流程出刀；持续按住无需松开重按。空挥也收回，挖方块不触发。停止攻击不会自动显现。

潜行右键主手喵刀显现剑灵，原槽位留下半透明虚化剑占位。手持对应虚化剑（或所选槽位为空）潜行右键自己的剑灵，恢复原剑及身份、耐久和组件，无需管理员命令。其他物品不会被覆盖。崩溃恢复仍未完成。

剑灵默认使用用户提供的**护士酒狐**原模型及贴图（196骨骼），沿用Bedrock渲染器。双加载器构建通过，NeoForge已进入世界检查四视图及资源重载，确认未回退。已绑定基础剑灵动作，尚不支持原包Gecko/Molang完整动画。查看[接入与验证](docs/IMPLEMENTATION_NURSE_WINE_FOX.md)、[游戏截图](art/blade_spirit/preview.html)和[资源来源](art/blade_spirit/nurse-wine-fox/NOTICE.md)。

[English](README.md) | 简体中文

包名：`com.abyssredemption.absmod`，模组 ID：`absmod`。

## 项目结构

| 目录 | 用途 |
| --- | --- |
| `common/src/main/java` | 26.2 两种加载器共用的物品行为、命令与常量 |
| `common/src/main/resources` | 共用语言、模型和物品标签 |
| `26.2NeoForge` | NeoForge 入口、注册与元数据 |
| `26.2Fabric` | Fabric 入口、注册与元数据 |

根项目统一构建两种加载器，分别输出 JAR，无需 Architectury 运行时。
目前支持 Minecraft Java 26.2；其他游戏版本尚未实现。扩展版本时添加对应子项目，
按该版本的 API 选择共享代码和资源，不能直接假定跨版本二进制兼容。

## 构建与运行

安装 JDK 25，在 IDE 中以根目录的 `settings.gradle` 导入项目，将 Gradle JVM 设置为 JDK 25。
首次构建需要联网下载 Gradle、Minecraft 和加载器依赖。

请将仓库根目录作为 Gradle 项目打开，而不是仅导入 `26.2NeoForge` 或 `26.2Fabric`。
若 IDE 报错 `Task 'wrapper' not found in project ':neoforge'`，请重新导入根目录的
`settings.gradle`。两个加载器子项目也提供转发到根任务的 `wrapper` 入口，
`:neoforge:wrapper` 和 `:fabric:wrapper` 均生成同一套根目录 Wrapper。
Wrapper 任务固定为 Gradle 9.5.1，并保留发行包校验值。
需要主动修改 Wrapper 参数时，请调用根项目的 `:wrapper`。

```powershell
./gradlew.bat build
./gradlew.bat :neoforge:runClient
./gradlew.bat :fabric:runClient
```

分别构建：`./gradlew.bat :neoforge:build` 或 `./gradlew.bat :fabric:build`。
Linux 或 macOS 下将 `./gradlew.bat` 换成 `./gradlew`。
产物位于 `26.2NeoForge/build/libs` 与 `26.2Fabric/build/libs`；安装不带 `-sources` 的 JAR，
并选择与加载器对应的文件。Fabric 端需要安装 Fabric API。

## 喵刀

- 共九个独立阶段，显示为“喵刀·1阶”至“喵刀·9阶”，按阶段顺序加入战斗栏。
- 一阶段保留 ID `absmod:meow_blade`；二至九阶段为 `absmod:meow_blade_stage_2` 至 `absmod:meow_blade_stage_9`。
- 各阶段的独立伤害参数集中在 `common/src/main/java/com/abyssredemption/absmod/item/MeowBladeStage.java`。
  参数表示玩家默认基础属性下的总值。已采用 PROPOSED PvE 候选表：
  伤害 **7/8/9/10/12/14/16/18/22**，总攻速 **1.8/2/2.2/2.5/2.8/3.2/3.6/4/5**。
  这些是原型测试值，尚非最终平衡；连击加成及高速满威力命中尚未实现。
- 耐久、附魔能力和修理材料也沿用钻石剑，加入 `minecraft:swords` 标签。
- 可从创造模式战斗栏取得，或执行 `/give @s absmod:meow_blade`。
- 例如 `/give @s absmod:meow_blade_stage_9` 可取得九阶喵刀。
- 暂不实现升级、阶段切换或合成配方。

每阶段都有独立的客户端物品定义、模型 JSON 和已提供的贴图。
原文件 `meowsword1.png` 至 `meowsword9.png` 按编号对应一至九阶。
项目文件位于 `common/src/main/resources/assets/absmod/textures/item/`，命名为 `meow_blade.png`
及 `meow_blade_stage_2.png` 至 `meow_blade_stage_9.png`。
九张贴图均为 1254 × 1254 RGBA PNG，按字节原样复制，保留透明度和原画。
对应模型引用 `absmod:item/<item_id>`，两种加载器共用同一套贴图。
此分辨率下的游戏内显示、图集 mipmap 和性能仍需客户端验证。

## 奶昔

迁移自作者的 [MilkShakeMod](https://github.com/Johnson-Tang2019/MilkShakeMod/tree/b9a1ac762b18c29973301048f6984987a4601fe2)（`master`，提交 `b9a1ac7`）。
两种加载器均包含原项目已实现的四种饮品及原始 16 × 16 RGBA 贴图。
原仓库的绿色贴图没有对应已注册物品或配方，因此不额外创作绿色奶昔玩法。

| 物品 ID | 显示名称 | 效果 |
| --- | --- | --- |
| `absmod:milkshake` | 奶昔 | 生命提升 II，120 秒 |
| `absmod:chorus_milkshake` | 紫颂果奶昔 | 生命提升 II，120 秒；随机传送 |
| `absmod:gold_milkshake` | 金奶昔 | 生命提升 II、伤害吸收 IV，120 秒；抗火 II、抗性提升 II，300 秒；生命恢复 II，20 秒 |
| `absmod:pink_milkshake` | 最好的奶昔 | 生命提升 II，120 秒；速度 IV，300 秒 |

所有奶昔恢复 6 点饥饿值，饱和度系数为 1.0（12 点饱和度），满饥饿也可饮用，
饮用耗时 3 秒，使用蜂蜜饮用音效，最多堆叠 16 个，稀有度为 Uncommon。
奶昔作为合成材料时返回桶，与原项目 `craftRemainder` 行为一致。
饮用不额外新增空容器返还，也不会清除已有状态效果。
紫颂果奶昔保留原实现：在起点附近约 8 格范围内最多尝试随机传送 15 次，
包含传送门粒子以及成功/失败音效，传送仅在服务端执行。

六个配方均为无序合成：

| 材料 | 产物 |
| --- | --- |
| 牛奶桶 + 糖 | 2 杯奶昔 |
| 牛奶桶 + 糖 + 紫颂果 | 2 杯紫颂果奶昔 |
| 奶昔 + 紫颂果 | 1 杯紫颂果奶昔 |
| 牛奶桶 + 糖 + 金块 | 2 杯金奶昔 |
| 奶昔 + 金块 | 1 杯金奶昔 |
| 奶昔 + 樱花树苗 | 1 杯最好的奶昔 |

取得牛奶桶后解锁全部六个配方。旧版无效的配方条件、物品栏触发器和不完整的配方奖励引用
已修正为有效的 26.2 数据。
新物品使用 `absmod` 命名空间，不会自动转换旧存档中的 `armilkshake` 物品。
可在“食物与饮品”创造栏取得，或用 `/absmod milkshakes @s` 一次领取四种各一杯。
用 `/give @s absmod:gold_milkshake 16` 可测试单个品种的整组堆叠。
饮用、效果、传送、合成剩余物和配方书解锁仍需在两个加载器中游戏内验收。

## 命令

所有命令的权限均与原版 `/give` 相同，需要游戏管理员权限（通常为 OP 等级 2）。

| 命令 | 用途 |
| --- | --- |
| `/absmod` 或 `/absmod help` | 查看命令帮助 |
| `/absmod stages` | 列出九阶段、ID、基础伤害和攻速，并提示候选测试状态 |
| `/absmod give <targets> <stage>` | 给每个目标发放一把指定阶段喵刀 |
| `/absmod kit <targets>` | 给每个目标发放九阶段各一把的测试套装 |
| `/absmod milkshakes <targets>` | 给每个目标发放四种奶昔各一杯 |
| `/absmod inspect <targets>` | 查看每个目标主手喵刀的阶段及剩余/最大耐久 |

```mcfunction
/absmod give @s 1
/absmod give @a 9
/absmod give PlayerName 4
/absmod kit @s
/absmod stages
/absmod inspect @s
```

语法：`/absmod give <targets> <stage>`。
阶段必须是 1 至 9 的整数，每个被选中的在线玩家获得一把对应阶段的喵刀。
服务器控制台可使用玩家名或 `@a`；背包满时，物品掉落在目标玩家身边并指定该玩家拾取。
命令发放新物品，不改变现有物品的阶段。两种加载器共用命令逻辑，反馈提供中英文翻译。

## 喵刀刀光

九个阶段的喵刀挥动时都会产生粉白弧形光带，空挥也能触发。
客户端读取自己及其他已同步玩家的原版挥手动画，不增加伤害、实体或额外网络包；挖掘挥手也会产生视觉效果。
效果按实际挥动的手判断，左利手玩家和副手挥动会镜像显示。
每次挥刀立即切换下一种刀光动作：两种相反斜斩、横扫、上挑、大倾角斜斩和反向横扫，共六种循环。
每位玩家独立计数，不增加出刀延迟，也不因停顿重置。此处改变刀光轨迹，原版手臂动画不变；
中途加入或开始跟踪玩家的客户端，视觉序列起点可能不同。

原创程序化网格组合了珍珠白亮边、半透明粉色薄纱、流动细丝和飘散微光。
樱花粉主体采用透明混合，在明亮背景下保留粉色，再叠加独立发光层。
前半段保持高亮，后半段平滑淡出；两层均保留深度测试，关闭深度写入和背面剔除。
放大的弧光包含 12 层光带、扩散光晕和 32 枚旋转的花瓣状微光。
每道刀光持续 18 个游戏刻（20 TPS 时为 0.9 秒），最多保留 32 格内的 32 道刀光。
视觉范围扩大不会改变攻击距离或伤害。
断开连接或切换世界时清理，游戏暂停时冻结；高阶段的视觉尺寸略微增加。
这是对概念图的游戏化实现，并非逐像素复现。不打包拔刀剑 OBJ/PNG 素材或概念图背景。

使用 `/absmod kit @s` 领取喵刀，选中后挥动即可预览。
请检查一阶和九阶、第一/第三人称、左右利手、连续挥刀、上下视角、墙体遮挡、暂停恢复和切换世界。
多人环境下检查第二个客户端每次观察到挥手只显示一道刀光；钻石剑和空手不应触发。
自动几何和挥手检查包含在 `:fabric:verifyCommands` 中，不验证 GPU 输出，双加载器仍需游戏内视觉验收。

## 开发约定

技术命名使用含义明确的英文，不使用拼音。代码注释和文档字符串使用英文。
`README.md` 与 `README.zh-CN.md` 内容保持同步，具体规则见 [CONTRIBUTING.md](CONTRIBUTING.md) 和 [AGENTS.md](AGENTS.md)。

## 游戏内验收

分别运行两种加载器的客户端，确认模组加载、战斗栏物品、中文/英文名称和手持模型；
用原版钻石剑对照喵刀的属性提示、攻击冷却和攻击行为，检查附魔与钻石修理。
命令需检查一阶和九阶、拒绝 0 和 10、OP 与普通玩家权限、多个目标、控制台及满背包场景。
构建成功只能验证编译和打包，不能代替游戏内验收。

`build` 会通过 `:fabric:verifyCommands` 测试共享命令逻辑，在不启动游戏服务器的情况下验证合法语法、
非法阶段数值、缺失参数、权限限制和只读命令执行，并核对迁移奶昔的效果时长、等级和数量。两个加载器均执行编译；NeoForge 运行行为、物品入包及主手检查仍需游戏内验收。

2026-09-08 验证结果：JDK 25 / Gradle 9.5.1 下两个加载器均构建成功；
已对照 Minecraft 26.2 原版 `Items` 中的钻石剑配置，并检查两个 JAR 的共享资源、
物品类、版本元数据及加载器隔离。尚未进行游戏内实测。

## 构建依据

- [NeoForge 26.2 MDK](https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle)
- [Fabric 26.2 示例](https://github.com/FabricMC/fabric-example-mod/tree/26.2)

依赖版本在根目录 `gradle.properties` 和 `build.gradle` 中固定。

## 器灵身份与持久化原型

管理员可对目标玩家主手的喵刀进行测试：

```text
/absmod soul init <玩家>
/absmod soul inspect <玩家>
/absmod soul affection set <玩家> <数值>
/absmod soul affection add <玩家> <数值>
```

初始化必须明确执行，并为本次测试将目标玩家记录为主人，不代表已实现自动绑定玩法。刀上只保存器灵 UUID；服务端档案保存主人、路线、阶段、可选好感、形态、格式版本和修订号。好感初始未设置，测试命令接受有限数值，未擅自确定正式初始值或范围。查询不会初始化刀；档案缺失或无法读取时会拒绝操作，不会自动重建。

重复身份诊断覆盖在线背包、末影箱和已加载掉落物，阻止相关测试命令；尚不扫描离线玩家、未加载或嵌套容器，也没有全局战斗锁。阶段迁移当前只生成方案，保留身份和好感并采用目标物品默认属性，不实际升级。详见[当前实现及游戏内验收清单](docs/IMPLEMENTATION_BATCH_1.md)。

粉色刀光已扩大半透明光幕和外层光晕，降低外层不透明度并保留亮芯；游戏内视觉验收待完成。

## 喵刀连续攻击

主手持喵刀，按住游戏中绑定的攻击键，即可沿当前准星方向连续攻击。服务端按实际攻速控制出刀；单击和长按共享一个预算。松键、界面、失焦、瞄准方块/挖掘、使用物品、死亡、换物品/世界都会停止会话。中断后需松开并重新按下。

授权主攻击采用满攻击强度，并局部绕过目标受伤窗口，保留正常近战伤害流程。其他武器、额外伤害和原版受伤冷却历史保持原有行为。服务端确认后播放原有粉色刀光，并按 attackId 去重。主手持喵刀且连斩通道可用时，隐藏原版蓄力指示。

`/absmod combat probe @s true` 开启日志，`false` 关闭日志，`/absmod combat inspect @s` 查看会话状态。这些命令不切换自动伤害，也不重置出刀恢复。日志统计的是授权动作，不是成功命中。

双加载器构建与独立逻辑检查通过。游戏内伤害、真实按键、盾牌/护甲交互和多人刀光尚未实测。详见[实现与验收清单](docs/IMPLEMENTATION_AUTOMATIC_DAMAGE.md)。

## 创造模式物品栏

打开 **Ab's Mod** 专属标签页（喵刀图标），即可按顺序找到九阶喵刀和四种奶昔。物品也保留在原版战斗、食物与饮品分类中。

## 战斗运行时测试

使用 JDK 25 执行 `./gradlew.bat --gradle-user-home .gradle-user-home -PgameTests :fabric:runGameTest`。`-PgameTests build` 会同时运行这些测试和双加载器构建。测试模组独立，不进入发布 JAR。

八项 AbsMod 服务器世界测试覆盖连续满强度伤害、200 游戏 tick 内 60 次实际命中、原版蓄力、护甲、无敌保护、普通伤害冷却历史、主命中确认（含吸收值）和创造标签注册/图标。受控测试使用显式属性的模拟玩家；不代表真实按键、选目标/网络、盾牌、多人视觉、NeoForge 运行时或最终平衡已验收。详见[运行时验证](docs/RUNTIME_VALIDATION.md)。

## 连击状态原型（BAL-02）

服务端授权的主攻击实际扣除目标生命或吸收值后，增加一层短期连击。上限采用 PROPOSED 平衡表（5～20 层）；空挥和免疫命中不刷新计时。松键保留层数，最后有效命中后第 65 游戏 tick 首次减层，之后每 5 tick 减一层。换刀、改变器灵引用/槽位/世界、死亡或断线会清空短期状态。

使用 `/absmod combat inspect @s` 查看 `comboStacks`；`/absmod combat probe @s true` 记录攻击前后层数及有效主命中结果。**尚未应用连击增伤和攻速加成。** 本批先完成命中确认和状态清理，再接属性增益。目前不需要新贴图。详见[实现记录](docs/IMPLEMENTATION_COMBO_STATE.md)。

## 喵娘剑灵原型

第一版手动剑灵已使用粉白占位皮肤，带猫耳、轻摆尾巴、人形行走与主人跟随。显现期间保留原剑完整数据。主手拿喵刀，由管理员执行：

```text
/absmod soul init @s
/absmod spirit manifest @s
/absmod spirit recall @s @e[type=absmod:blade_spirit,sort=nearest,limit=1] 7
```

槽位 7 是第八个快捷栏槽，必须为空；允许槽位 0～35。多只剑灵测试时，请用显现返回的实体 UUID 替换选择器。尚未实现自动召回、升级、好感增长或死亡扣罚。请在测试世界使用：中断存档恢复和全局重复检查尚未完成。生命、移动和跟随距离为测试数值，原型设为无敌。

新增第九项 AbsMod 服务器世界测试，覆盖显现/召回、原剑数据保留及拒绝操作；含一项框架测试共十项通过。双加载器构建和 SavedData 文件重读检查通过；未启动客户端或 NeoForge 服务器，渲染与实际跟随仍需游戏内验收。详见[实现记录](docs/IMPLEMENTATION_SPIRIT_PROTOTYPE.md)和[贴图/模型需求](docs/SPIRIT_ART_REQUIREMENTS.md)。

## 花饰长发剑灵模型

普通路线剑灵现已按提供的粉白猫娘原画制作模型：分层长发、尖猫耳、花饰/水晶、外扩裙摆及五段弯尾。33 个立体部件通过逐面 UV 使用新排布图集。原画与旧占位资源均保留；图集是 AI 辅助的美术适配，并非逐像素裁切重排。

可[旋转查看模型](art/blade_spirit/preview.html)，或查看[可编辑几何与 OBJ 导出](art/blade_spirit/README.md)。双加载器构建、几何/UV/动画重置检查和浏览器视觉检查已通过。本次美术改动未启动 Minecraft，光照和运动穿插仍需客户端验收。详见[验证记录](docs/SPIRIT_MODEL_VALIDATION.json)。

## 剑灵柔和轮廓修订

最新版把方盒头、直板头发和整块裙子改为收下颌脸型、弯曲收尖发束、收腰身体、圆袖/腿、钟形裙摆及连续弯尾，共 1768 四边面。现有图集不变。双加载器构建与几何/UV/动画检查通过，已检查浏览器正面、背面和斜侧视图。Minecraft 光照与走动效果仍待游戏内验收。[验证记录](docs/SPIRIT_CURVED_VALIDATION.json)。

## 按整合包女仆模型实测比例

最新版已读取指定本地整合包内橙、豪德寺三花和博丽灵梦模型作尺寸参考：基础总高 26、头高 8（3.25 头身）、腿长 9、腿宽 3、手臂长 8。头发、裙摆和关节同步调整，原图与图集不变。双加载器构建、几何/比例/动画检查及浏览器视图检查通过；未启动 Minecraft。[测量与验证记录](docs/SPIRIT_PROPORTION_REFERENCES.json)。

侧视修订补足头和躯干厚度，将花饰连接到太阳穴附近，并压平正面五官区域，避免眼睛绕到侧面。双加载器构建与模型检查通过（`build/spirit-side-validation.log`）；目前仍是草模，未作 Minecraft 视觉验收。

## 临时使用现成女仆模型

当前剑灵改用指定车万女仆包中的橙及原配贴图，自制粉白资源保留。已适配旋转方向，闭眼覆盖层在表情动画接入前暂不显示；保留人形走路/转头与显现/召回。详见[第三方来源](art/blade_spirit/BORROWED_MODEL_NOTICE.md)。双加载器构建与模型检查通过，Minecraft 客户端验收仍待完成。
