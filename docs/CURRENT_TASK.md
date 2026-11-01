# 下一批开发任务单

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

CONFIRMED：用户授权实施成长方案，并明确本次加入暗黑八阶、独立重击和原版进度引导。数值仍为可迭代 PROPOSED 原型，非最终生存平衡。

SOURCE_PRESENT：普通/暗黑保留数据的初始配方，共鸣之心与菜单，二至八阶保留身份/附魔/使用比例的升阶事务；好感每日收入账本；普通连击属性与逐阶耐久、断刃保留；暗黑八阶、可改键 G 重击（12 tick前摇、160 tick冷却、1.8倍基础伤害、普通怪12 tick眩晕及后续100 tick免疫）；中英原版进度树。心魔未实现，普通八升九仍锁定。新增共鸣之心/暗黑基础图由内置图像生成工具制作，原九阶剑图未改，暗黑八阶暂共用基础图。

VALIDATION：两端构建与 Fabric 20 项 GameTest 最近一次通过；NeoForge真实世界共鸣开关/发包升级/重放拒绝/返还通过；暗黑重击只造成一次90伤害并正确拒绝重复/冷却请求；原版进度真实授予与显示已检查。截图 `art/progression/`，日志 `build/progression-client.log`、`build/progression-final-validation.log`。实际启动 Minecraft，不将测试夹具或编译等同于全生存/多人验收。

剩余：心魔资格与Boss、完整崩溃恢复、真实双玩家同时升级、Fabric图形客户端、长期平衡、独立运行时配置、暗黑逐阶原画及专属动作。保留模型原始参考目录未提交。实现文件、边界与完整证据见 [实施记录](IMPLEMENTATION_PROGRESSION.md)。


## 2026-10-31：MOTION-03 与成长方案

新增连续挥刀前 0.75 tick 的前姿态过渡，服务端节奏和伤害不变。已启动真实 NeoForge 游戏及两个独立客户端，第二客户端经 TCP 进入同一测试世界，记录 connected=2 并保存 54 张关闭刀光的观察者截图；详见 [实际测试记录](MOTION_MULTIPLAYER_VALIDATION.md)。本机双客户端不等于公网延迟或全部姿态验收。

新增[合成、成长与数值 v2](MEOW_BLADE_PROGRESSION_PROPOSAL.md)：九阶材料/好感/耐久、保留身份的升级事务、暗黑对照与重击候选。全部新增参数为 PROPOSED，尚未改运行时配方和数值。下一批优先完善动作边缘显示与完整操作回归，再按评审后的设计接升级。


## 2026-10-31：MOTION-02

已补动作资源失败时的第一人称退出，以及界面/挖掘期间迟到确认的拦截。离线双端构建与既有共享自动检查通过；下一步游戏实测资源失败/恢复与界面中断，之后继续原有动作和多人验收。本批未启动游戏。

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
