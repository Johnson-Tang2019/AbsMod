# Ab's Mod 开发规格（Source of Truth）

> 文档修订：`2026-09-12.repository-progress.1`。本轮仅更新文档；未修改游戏代码，未运行 Gradle 或 Minecraft。

> 最后更新：2026-09-12  
> 仓库：`Johnson-Tang2019/AbsMod`  
> 本文件面向后续 Codex、低能力代码模型和人工开发者，是玩法、代码边界、资源流程的主规格。
>
> 标记规则：
> - **[CONFIRMED]**：用户明确确认，不得擅自改动。
> - **[PROPOSED]**：为了工程落地给出的推荐默认方案，可以被后续指令覆盖。
> - **[TBD]**：尚未决定，AI 不得自行填成最终设定。

---

## 1. 项目事实与当前基线

> **[VERIFIED_SOURCE]** 2026-09-12 重新读取远端 main，固定核对 `2e420203619b5d9cf0f1af7fbc5135ff8858f34f`。相对 2026-09-10 文档包，compare 为 identical，新增提交 0 个、变化文件 0 个；本轮分支列表仅返回 main。
> 本文档包没有提交到仓库。固定提交的 `docs/` 本轮读取仍返回 404；本地 `D:\Ab'sMod` 和未推送改动不在核对范围。
> 逐项证据见 [REPOSITORY_AUDIT.md](REPOSITORY_AUDIT.md)。当前执行入口见 [CURRENT_TASK.md](CURRENT_TASK.md)，完成状态见 [STATUS.md](STATUS.md)。

### 1.1 标识与真实构建工具

| 项目 | 当前代码配置 |
| --- | --- |
| 显示名称 / Mod ID | `Ab's Mod` / `absmod` |
| 包名 | `com.abyssredemption.absmod` |
| mod_version | `0.1.0` |
| Minecraft / Java toolchain | `26.2` / `25` |
| NeoForge | `26.2.0.77` |
| NeoForge 构建插件 | **ModDevGradle `2.0.146`**，不是此前聊天建议的 NeoGradle |
| Fabric Loader / API | `0.19.5` / `0.159.0+26.2` |
| Fabric Loom | `1.17.20` |
| Gradle Wrapper | `9.5.1` |
| configuration cache | 当前 `false` |

以上来自当前 `build.gradle`、`settings.gradle`、`gradle.properties`，不因旧聊天中的工具选择建议修改现有工程。[证据入口](REPOSITORY_AUDIT.md)

### 1.2 当前目录和模块语义

```text
AbsMod/
├─ common/src/main/java/            同 Minecraft 版本跨加载器共享源码
├─ common/src/main/resources/       共享资源
├─ common/src/verification/java/    命令、奶昔与刀光的自动检查源码
├─ 26.2NeoForge/                    Gradle :neoforge
├─ 26.2Fabric/                      Gradle :fabric
├─ AGENTS.md / CONTRIBUTING.md
├─ README.md / README.zh-CN.md / CHANGELOG.md
├─ build.gradle / settings.gradle / gradle.properties
└─ gradlew / gradlew.bat / gradle/
```

`common` 当前是两个子工程注入的共享源码/资源目录，**不是独立的 `:common` Gradle 子工程**。只实现了 MC 26.2，不等于已经实现 1.21.1/26.1 多版本。
`docs/` 是本包待合并的文档目录，不是本次已查到的远端目录。不要覆盖已有工作树改动。

### 1.3 已有实现与验证边界

本轮重新读取阶段表与物品类；以下其他 SOURCE/REPOSITORY_REPORTED 内容沿用 2026-09-10 对同一固定提交的证据。没有把历史源码阅读、历史构建自述或旧模拟报告改写为本轮执行。

- **[VERIFIED_SOURCE]** 普通喵刀 1～9 阶及固定 ID：`absmod:meow_blade`、`absmod:meow_blade_stage_2` 至 `_9`；两加载器注册、创造栏和命令入口已有代码。
- **[VERIFIED_SOURCE]** `MeowBladeStage` 仍全部 `7.0F`；`MeowBladeItem` 的攻速构造修正仍 `-2.4F`，README 描述显示攻速 1.6。表中的九阶 22/5.0 尚未接入。
- **[REPOSITORY_REPORTED]** 九张 1254×1254 RGBA 原画资源、item definition、model、语言与剑标签已入库。此次未重新测量 PNG/渲染图像或检查构建 JAR。
- **[VERIFIED_SOURCE + REPOSITORY_REPORTED]** 四种奶昔、饮用行为、六个配方、测试命令属于已存在内容；游戏内饮用/传送/剩余物/配方书仍待验收。
- **[VERIFIED_SOURCE]** `MeowSlashEffects`、`SlashSwingTracker` 与两加载器客户端入口已有代码。粉白六轨迹刀光来自原版挥手观察，**不是主攻击调度或额外伤害**。
- **[VERIFIED_SOURCE]** `:fabric:verifyCommands` 接入 check，会调用命令、奶昔和刀光检查；此项为同一提交的历史源码核对；本轮未重新运行 Java 检查。
- **[REPOSITORY_REPORTED]** 最新提交说明称两加载器构建和自动检查通过，明确 GPU/游戏内视觉验收待进行。
- **[NOT_RUN_THIS_UPDATE]** 本轮没有重新编译、没有跑上述 Java 检查、没有游戏内测试；本轮检查限于文档结构、数据公式和归档完整性。

### 1.4 尚未实现 / 尚未验收

长按攻击输入与网络、服务端出刀节奏、取消原版蓄力、受伤窗口局部适配、普通连击增伤/提速、候选数值接入、暗黑 1～8、重击眩晕、器灵身份/实体/好感/转换、升级配方、心魔与九阶门槛，均不得因为文档或刀光存在标为已完成。

真正的三维刀模型、器灵模型与 GeckoLib 接入仍为候选；不能把客户端弧光当作实体模型或 UV 材质。

### 1.5 功能扩展与维护原则

按功能组织逻辑；平台注册、事件和客户端入口放在各加载器模块。未来其他游戏版本需要独立 API 适配，不承诺现有 common 跨版本二进制兼容。保留所有现有物品 ID、奶昔与已提交刀光实现。

---

# 2. 两条喵刀路线

## 2.1 普通喵刀

**[CONFIRMED]**

- 起源：钻石剑。
- 总阶段：9。
- 玩法定位：高攻速、连击、叠 Buff、持续攻击越打越强。
- 最终上限：高于暗黑路线。
- 普通 8 阶升 9 阶前必须挑战“心魔”。
- 心魔是暗黑喵刀化作的 Boss。
- 击败心魔后才允许完成普通九阶。

流程：

```text
Diamond Sword
    ↓
Meow Blade I
    ↓
II
    ↓
III
    ↓
IV
    ↓
V
    ↓
VI
    ↓
VII
    ↓
VIII
    ↓
Inner Demon Boss
    ↓ victory
Meow Blade IX
```

## 2.2 暗黑喵刀

**[CONFIRMED]**

- 起源：下界合金剑。
- 总阶段：8。
- 前期伤害更高。
- 升级更简单。
- 单次攻击伤害更高。
- 攻速更低。
- 有重击。
- 重击具有眩晕。
- 最终上限低于普通九阶。
- 主题文案：

> 你获得了更强的力量，但是代价呢？

流程：

```text
Netherite Sword
    ↓
Dark Meow Blade I
    ↓
II
    ↓
III
    ↓
IV
    ↓
V
    ↓
VI
    ↓
VII
    ↓
VIII (cap)
```

## 2.3 路线差异必须真实存在

| 项目 | 普通喵刀 | 暗黑喵刀 |
| --- | --- | --- |
| 起点 | 钻石剑 | 下界合金剑 |
| 阶段上限 | 9 | 8 |
| 升级难度 | 较高 | 较低 |
| 初中期单击 | 较低 | 较高 |
| 攻速 | 高 | 低 |
| 核心 | 连击叠层 | 重击爆发 |
| 控制 | 未确认 | 眩晕 |
| 后期持续输出 | 高 | 中高 |
| 最终上限 | 最高 | 较低 |
| 终局门槛 | 心魔 Boss | 8 阶封顶 |

禁止只用不同 tooltip 表现路线差异，最终必须反映到战斗曲线。

---

# 3. 普通喵刀战斗系统

## 3.1 连击 Buff

**[CONFIRMED]**

玩家用普通喵刀不断攻击时，会逐步叠加 Buff，伤害随着连续攻击提高。

**[CONFIRMED，2026-09-09 补充]** 连击也应使攻速小幅提高，普通满级目标为连续“刷刷刷”的斩击感。具体数值为本轮草案，见 BALANCE.md。

工程行为要求：

1. 只有有效近战命中能推进叠层。
2. 服务端权威。
3. 客户端只显示同步后的层数、粒子、HUD。
4. 叠层必须可查询。
5. 每阶段可拥有独立参数：
   - baseDamage
   - attackSpeed
   - maxComboStacks
   - damagePerStack
   - comboTimeout
   - decayRule
6. 所有平衡值集中定义，禁止散落常量。

### 3.2 参数状态：已有 v1 提案，尚未游戏实测

**[TBD]**

- 最大层数。
- 每层加固定伤害还是百分比。
- Buff 名字。
- Buff 是否跨目标保持。
- 多久未命中清空。
- 切武器是否清空。
- 空挥是否影响连击。
- 是否显示 HUD。
- 是否有满层特殊效果。

### 3.3 推荐数据结构

**[PROPOSED]**

```text
MeowBladeBalance
MeowComboState
MeowBladeCombat
BladeCombatContext
```

`MeowBladeItem` 本身只做物品入口，不应承担全部状态管理。

推荐职责：

```text
MeowBladeItem
    ↓ identifies route/stage
MeowBladeCombat
    ↓ validates hit
MeowComboState
    ↓ updates stacks
DamageCalculator
    ↓ calculates final damage
Client feedback
```

---

### 3.4 按住左键连斩与取消原版蓄力

**[CONFIRMED，2026-09-09]** 用户要求按住左键持续挥动，取消原版攻击冷却，获得连续连招手感。
**[PROPOSED]** 用局部攻击上下文去掉原版蓄力门槛和蓄力折扣，用武器自己的动作间隔调度每刀；不是移除一切间隔或全局无敌。
详见 [CONTINUOUS_COMBAT.md](CONTINUOUS_COMBAT.md)，其中包含输入分支、攻击去重、节奏、受伤窗口、现有刀光衔接与 AUTO-01 至 AUTO-06。
本次不新增伤害／攻速数值；两路线具体表仍见 BALANCE.md。器灵自动回手与暗黑重击触发依然待定。

# 4. 暗黑喵刀战斗系统

## 4.1 核心

**[CONFIRMED]**

- 攻速更低。
- 单次伤害更高。
- 重击造成眩晕。

## 4.2 重击必须解耦触发条件

用户尚未指定“重击”到底由什么输入触发，因此实现必须拆成：

```text
HeavyAttackTrigger
    ↓
isHeavyAttack(context)
    ↓
DarkMeowBladeCombat.applyHeavyHit(...)
    ↓
damage + stun + feedback
```

这样以后可以自由把触发改成：

- 满攻击冷却攻击
- 蓄力右键
- 冲刺攻击
- 特殊键
- 连招终结技

而无需重写眩晕系统。

## 4.3 重击触发

**[TBD]**

目前绝对不要擅自选择触发方式。

## 4.4 眩晕

**[CONFIRMED concept]**

重击可眩晕目标。

**[TBD]**

- 时长。
- 是否影响玩家。
- 是否禁移动。
- 是否禁攻击。
- 是否禁使用物品。
- Boss 是否有抗性。
- 连续眩晕是否递减。
- PvP 是否缩短。

**[PROPOSED]**

把 `Stun` 做成可复用独立状态，而不是只写在暗黑喵刀类中。

---

# 5. 阶段与 ID

## 5.1 普通路线

已存在：

```text
meow_blade
meow_blade_stage_2
...
meow_blade_stage_9
```

不得随意改。

## 5.2 暗黑路线

**[PROPOSED, 尚未注册]**

```text
dark_meow_blade
dark_meow_blade_stage_2
dark_meow_blade_stage_3
dark_meow_blade_stage_4
dark_meow_blade_stage_5
dark_meow_blade_stage_6
dark_meow_blade_stage_7
dark_meow_blade_stage_8
```

推荐类：

```text
DarkMeowBladeStage
DarkMeowBladeItem
DarkMeowBladeCombat
```

如果用户后续给出正式 ID，用户指令优先。

---

# 5A. 器灵与好感度系统

> 详细实现规范见 [`SPIRIT_SYSTEM.md`](SPIRIT_SYSTEM.md)。

## 5A.1 核心设定

**[CONFIRMED]**

- 普通和暗黑喵刀都具有猫娘器灵形态。
- 非战斗时，器灵以实体形态跟随玩家。
- 战斗时，器灵化回剑形态。
- 好感度是升级的必要条件。
- 相处、互动、共同战斗均可增加好感度。
- 器灵死亡后不是永久消失，而是变回剑。
- 器灵死亡会：
  - 降低好感度；
  - 降低剑耐久。
- 普通器灵视觉主题为粉白。
- 暗黑器灵视觉主题为粉紫。

## 5A.2 工程提案 [PROPOSED]

采用三层：服务端 `BladeSoulRecord` / `BladeSoulStore` 长期档案、实际剑 ItemStack、猫娘 `BladeSpiritEntity`。
同一器灵的 `soulId`（原草案称 bladeSoulId）贯穿变形与升级。
档案中的好感和关系是唯一权威值；剑/实体只是引用与同步视图。
剑形态的物品属性以实际 ItemStack 为准，器灵显现期间托管完整原剑快照。

```text
一个长期身份
+ 同一时刻一个有效玩法载体
+ 允许非权威缓存、只读引用和交接暂存
```

实现入口统一使用 SpiritTransformationService，形态为 SWORD、MATERIALIZING、SPIRIT、RECALLING、RECOVERY_PENDING。
通过修订号、操作编号、有效实体校验和恢复记录处理重发与中断；UUID 本身不保证防复制。
原剑恢复、死亡扣罚、满背包交付和异常回滚不得散落在多个回调中。

先身份存储，后占位实体手动收放，再做死亡恢复、好感接口，最后接自动战斗与正式动画。
不强制依赖车万女仆，不要求十七种实体或骨骼。详见 SPIRIT_SYSTEM 第 2、3、18～24 节。

同阶变形保留原剑组件；升阶用显式迁移策略，不能盲拷旧攻击属性。
好感门槛不代表消耗好感；好感降低不默认降阶；重建实体不默认回满生命。
以上均为工程约束与未决边界，不是新批准的数值玩法。

---

## 5A.3 未决规则

**[TBD]**

- 战斗触发定义。
- 脱战重新化灵的延迟。
- 好感度范围和各阶阈值。
- 各行为的好感收益。
- 器灵死亡具体扣值。
- 死亡后的剑出现位置。
- 器灵生命、环境免疫和主人死亡/跨维度行为。
- 是否允许多器灵同时跟随。
- 升级是否消耗好感、好感下降是否降阶、变形回血和零耐久结果。


# 6. 升级系统

## 6.1 原则

**[CONFIRMED]**

- 普通路线升级总体更难。
- 暗黑路线升级总体更简单。
- 暗黑前期更强。
- 普通终局更强。

## 6.2 推荐实现

**[PROPOSED]**

```java
BladeProgressionStep {
    BladeRoute route;
    int fromStage;
    int toStage;
    List<ProgressionRequirement> requirements;
}
```

未来 requirement 可扩展：

```text
ItemRequirement
ExperienceRequirement
AdvancementRequirement
BossDefeatRequirement
LocationRequirement
KillRequirement
```

不要把每阶升级写成大量 `if (stage == 4)`。

## 6.3 普通 8 → 9

必须包含：

```text
BossDefeatRequirement(INNER_DEMON)
AffectionRequirement(requiredAffection)
```

仅拥有材料不能绕过。

## 6.4 未决定

**[TBD]**

- 钻石剑 → 普通 1 阶配方。
- 下界合金剑 → 暗黑 1 阶配方。
- 每阶材料。
- 经验消耗。
- 是否使用工作台。
- 是否使用锻造台。
- 是否自定义仪式/UI。
- 升级是否保留附魔。
- 升级是否保留耐久。
- 升级是否保留自定义名字。

---

# 7. 心魔 Boss

## 7.1 固定剧情职责

**[CONFIRMED]**

- 心魔是普通路线 8 → 9 的门槛。
- 心魔是“暗黑喵刀化作的 Boss”。
- 玩家必须正面战胜它。

## 7.2 工程模块

至少需要：

```text
InnerDemonEntity
InnerDemonAI
InnerDemonRenderer
InnerDemonModel
InnerDemonFightController
InnerDemonProgress
```

以及：

- EntityType 注册。
- Boss bar。
- 生成/召唤。
- AI。
- 动画。
- 音效。
- 失败处理。
- 多人归属。
- 通关状态。
- 8→9 条件校验。

## 7.3 通关状态

**[PROPOSED]**

用服务端持久状态记录玩家是否击败心魔。

要求：

- 重连后保留。
- 服务端可验证。
- 客户端不能伪造。
- 开发命令能查询。
- 后续可接 advancement。

## 7.4 Boss 未定项

**[TBD]**

- 正式名称。
- 外形：人形/剑灵/巨剑/猫形/混合。
- 技能数量。
- 阶段数量。
- 召唤地点。
- 场地。
- 失败惩罚。
- 掉落。
- 多人通关归属。
- 暗黑路线与 Boss 的特殊交互。

---

# 8. 美术与资源

## 8.1 普通喵刀视觉方向

**[CONFIRMED]**

- 普通喵刀正式主题色：**粉白**。

关键词：

```text
粉白
可爱
猫猫元素
细长
梦幻
轻灵
水晶
半透明边缘
```

早期：

- 明显来自钻石剑。
- 青蓝比例高。
- 粉色只是逐步侵入。

中期：

- 青蓝和粉白过渡。
- 猫耳/猫爪/爱心逐渐明确。

后期：

- 细长，不做厚重大剑。
- 粉白为主。
- 小量青蓝作为冷色高光。
- 边缘和翼片允许半透明。
- 水晶、羽片、薄光刃感。
- 装饰可以丰富，但整体轮廓仍轻。

禁止：

- 过粗。
- 大量笨重机甲块。
- 满屏尖刺。
- 过度杂乱。
- 半透明元素变成不透明白色塑料片。

## 8.2 暗黑美术

**[CONFIRMED]** 暗黑喵刀主题色为粉紫，与普通粉白路线区分；与下界合金剑有关，战斗偏低速重击和眩晕。

**[PROPOSED]** 使用深紫阴影和少量冷灰结构表现力量感，但不因为重击就强制使用笨重机甲造型。

**[TBD]** 具体辅色比例、材质、发光/半透明程度、器灵衣装和各阶段细节。
“暗黑色板尚未确认”不再适用于主题色；不要擅自改成黑红。

---

## 8.3 当前资源现状

当前代码使用普通 Minecraft item definition/model 加完整 2D 贴图。

当前已提交九张普通喵刀贴图可以构建，但不等于最终的拔刀剑式 3D 资产。

## 8.4 未来拔刀剑式资源

必须区分：

```text
concept art
2D item sprite
3D model
UV map
UV texture
renderer
```

如果决定用自定义 3D：

```text
model first
    ↓
UV unwrap
    ↓
UV texture
    ↓
render config
    ↓
in-game validation
```

不能直接把斜着画的整把剑 PNG 当作 3D UV。

## 8.5 半透明

半透明指“武器自身部分 alpha < 255”，不只是透明背景。

需要实测：

- GUI
- 手持
- 掉落物
- mipmap
- atlas
- 光影/Iris
- 透明排序

---

# 9. 代码推荐目录

**[PROPOSED]**

```text
common/src/main/java/com/abyssredemption/absmod/
├─ item/
│  ├─ BladeRoute.java
│  ├─ MeowBladeItem.java
│  ├─ MeowBladeStage.java
│  ├─ DarkMeowBladeItem.java
│  └─ DarkMeowBladeStage.java
├─ combat/
│  ├─ BladeCombatContext.java
│  ├─ MeowBladeCombat.java
│  ├─ MeowComboState.java
│  ├─ DarkMeowBladeCombat.java
│  ├─ HeavyAttackTrigger.java
│  └─ StunController.java
├─ progression/
│  ├─ BladeProgression.java
│  ├─ BladeProgressionStep.java
│  └─ requirement/
└─ boss/
   ├─ InnerDemonProgress.java
   └─ InnerDemonRules.java
```

平台模块：

```text
26.2NeoForge/
├─ registry
├─ event
└─ client

26.2Fabric/
├─ registry
├─ event
└─ client
```

Boss entity 注册和事件入口依加载器 API 分离。
器灵新增的 spirit/、affection/、entity/ 职责目录以 SPIRIT_SYSTEM 第 18 节为准。
这些是拟新增目录，不是已存在文件清单。

---

# 10. 服务端与客户端

必须服务端决定：

- 最终伤害。
- 连击层数。
- 重击成立。
- 眩晕。
- 升级资格。
- Boss 击败。
- 九阶解锁。
- 器灵身份、好感、形态交接、原剑交付与死亡惩罚。

客户端负责：

- HUD。
- tooltip。
- 动画。
- 粒子。
- 音效展示。
- 模型。
- 已同步状态。

禁止客户端直接决定可升级、可眩晕、最终伤害。

---

# 11. 平衡数据管理

## 本轮候选值 [PROPOSED]

普通九阶：基础22伤害/5.0攻速；二十层后35.2/6.0。暗黑八阶：基础50伤害/1.6攻速，不继承普通连击。完整1～9/1～8表及技术依赖以 BALANCE.md 为单一数值定义，JSON可供代码AI读取。

这些是设计值，代码基线仍使用7.0/-2.4占位；本轮没有修改 Java。


所有数值集中，例如：

```text
MeowBladeBalanceTable
DarkMeowBladeBalanceTable
```

当前精确参数已有 v1 平衡草案，见 [`BALANCE.md`](BALANCE.md) 与 `BALANCE_PROFILE.json`。草案允许原型验证，不等于用户逐项确认或游戏实测通过；未覆盖的参数仍不自行填最终值。

普通阶段至少：

```text
baseDamage
attackSpeed
maxComboStacks
damagePerStack
comboTimeout
durability
```

暗黑阶段至少：

```text
baseDamage
attackSpeed
heavyHitMultiplier
stunDuration
heavyHitCooldown
durability
```

区分当前代码中的旧 placeholder、本文已设计的 PROPOSED 候选参数、以及真正 TBD 的未决参数。不得把尚未决定的重击/好感阈值写成零值掩盖缺项。

---

# 12. 测试要求

最低：

```powershell
./gradlew.bat build
git diff --check
```

普通路线：

- 有效命中才叠层。
- 无双倍伤害。
- 不同玩家状态独立。
- 服务器重连状态按最终规则。
- 满层伤害正确。
- 切换目标行为正确。

暗黑：

- 攻速确实更低。
- 单击明显更高。
- 重击只结算一次。
- 眩晕不能永久锁。
- PvP/Boss 规则正确。

Boss：

- 未击败不能升 9。
- 击败后可升 9。
- 重连保持。
- 多人不误发资格。
- 失败不软锁。

资源：

- GUI 不糊。
- 手持方向正确。
- 半透明边缘无黑边。
- 掉落物正确。
- 两加载器一致。

---

# 13. 低能力 AI 强制规则

1. 开工前阅读本文件、`LORE.md`、`STATUS.md`、`SPIRIT_SYSTEM.md`、`ART_GUIDE.md`、`BALANCE.md` 和 `CONTINUOUS_COMBAT.md`。
2. 用户最新明确指令优先。
3. 未经设计授权不自行发明数值；本轮获授权设计的普通/暗黑伤害攻速与连击候选参数见 BALANCE.md，状态仍为 PROPOSED。材料、眩晕秒数与 Boss 技能等未决项不擅自定稿。
4. 原型可采用明确标注的 PROPOSED 参数；最终发布前进行验证。未设计的参数仍保持 TBD，不能用 0 假装确认。
5. 不更改已存在普通喵刀 ID。
6. 两加载器必须同时可构建。
7. 平台 API 不得放错模块。
8. 不把整个功能塞进一个类。
9. 不把客户端效果当服务端判定。
10. 生图必须遵守后期“细长、梦幻、轻灵、半透明边缘”。
11. 暗黑正式主题色已确认为粉紫；除非用户确认，不得擅自改成传统黑红主题。
12. 每次开发后更新 `STATUS.md`。
13. 用户补充设定后同时更新 `DEVELOPMENT.md` 和 `LORE.md`。

---

# 14. 用户决策待办

**[TBD]**

优先询问/等待用户确定：

1. 普通九阶伤害/攻速 v1 已提出，待原型验证、体验反馈和定稿（BALANCE.md）。
2. 暗黑八阶伤害/攻速 v1 已提出，重击单独设计，待定稿（BALANCE.md）。
3. 普通连击 v1：每层伤害 +3%、基础攻速 +1%，逐阶层数上限见 BALANCE.md；待验证。
4. 连击清空/衰减 v1 已提出（60 tick 宽限、之后每5tick减一层）；待体验验证。
5. 暗黑重击触发方式。
6. 眩晕时间和适用对象。
7. 暗黑粉紫主题的具体辅色、材质与明暗比例。
8. 两路线升级材料。
9. 心魔 Boss 外形。
10. 心魔 Boss 技能。
11. Boss 召唤方式。
12. 最终采用 2D 还是自定义 3D 拔刀剑式模型。

---

# 15. 变更记录

## 2026-09-10：按实际 main 复核

- 固定远端基线 2e42020；与前版无新增 main 提交，不虚构进度。
- 校正 ModDevGradle 与共享源码目录的真实语义；读取两加载器客户端和自动检查入口。
- 分离用户目标、候选参数、源码存在、历史构建报告和本轮实际检查。
- 合并 COMBAT_INPUT / CONTINUOUS_COMBAT 两套命名，以后者为主；新增当前任务与证据清单。
- 玩法、数值与故事主线未擅自改动。


## 2026-09-09：连续攻击需求

- 新增长按／取消原版蓄力专项规格，数值保持 v1 草案。
- 复核 main 已前进至 2e42020，补记已实现的纯视觉刀光；没有将刀光当成连斩伤害。
- 本次仅文档和离线参考模型测试，未编译或运行游戏。

## 2026-09-09：战斗平衡v1

- 记录满级高速连斩与连击小幅提速目标。
- 新增 BALANCE.md、设计JSON、公式验证报告，补入17阶段数值草案；游戏实现仍未完成。
- 区分设计提案与仓库占位值；未决定的重击/好感/死亡规则仍为TBD。

## 2026-09-09

- 写入上一轮器灵实现方案，所有新增架构保持 PROPOSED。
- 更新档案/物品/实体的权威分工、交接恢复和第一批任务入口。
- 修正“暗黑主题色尚未确认”和“docs 已在仓库”等过时或模糊表述。
- 复核远端 main 未变化，未把设计标为已实现，未运行构建。

## 2026-09-08

- 建立长期开发规格。
- 记录当前 26.2 双加载器状态。
- 固化普通 9 阶、钻石剑起源。
- 新增暗黑 8 阶、下界合金剑起源。
- 固化暗黑“前期更强、升级更简单、上限更低”。
- 固化暗黑低攻速、高单击、重击眩晕。
- 固化普通高攻速、连续攻击叠 Buff。
- 固化普通 8→9 必须挑战心魔。
- 固化心魔为暗黑喵刀化作的 Boss。
- 固化普通后期视觉：细长、梦幻、轻灵、半透明边缘。

- 固化普通主题色为粉白，暗黑主题色为粉紫。

- 固化器灵平时为猫娘跟随、战斗化剑；加入好感度升级门槛和死亡回剑惩罚。


## 2026-09-12 进度复核补记

本轮仅刷新远端证据和文档任务交接。主线代码未变化，所以设计与实现差距仍按 STATUS.md 保留；没有调整数值、依赖或已确认设定。下一批入口为 CURRENT_TASK.md，推荐先接入候选阶段表及普通主手长按输入/节奏骨架。不得将此文档更新误解为游戏功能完成或已写入用户本地项目。
