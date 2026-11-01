# Ab's Mod 美术与生图执行指南

## 2026-11-01：猫毛色图集（CAT-02）

六个独立image_gen调用，以原版成年/幼猫UV为参考重新配色绘制；不宣称原版UV或形体为原创。保留生成源图，打包为64×32成年与32×32幼猫，共12图。来源、提示词与技术处理见art/cats/genetics/ASSETS.md。已在26.2与1.21.1实际显示并截图，1.21.1幼猫沿用成年UV缩小；生成图不是游戏截图的替代。普通九阶原图不变。

## 2026-11-01：第一人称位置、倾斜与遮挡（MOTION-04）

CONFIRMED：用户明确优先第一人称刀的位置、倾斜和遮挡。静止握柄移向外侧下方并稍远离镜头，加入18度透视偏转、降低刀尖并减小静止尺寸；挥刀保留原始采样，收招随权重平滑回到新握持，左手镜像。无新增伤害或第三人称动作修改。

实际启动NeoForge进入新世界，检查一阶右手、九阶左手静止、关闭刀光挥刀、开启刀光及资源重载样本；截图 art/animation/first-person-grip/，日志 build/first-person-grip-client.log（BUILD SUCCESSFUL）。双加载器build通过（build/first-person-grip-build.log）。仅FOV65/1280×900的本地自动驱动视觉样本；未重复多人测试，非用户美术验收。侧刃瞬间过薄及其他FOV仍待打磨。

改动：BladeGrip.java、MotionVisualTest.java、本文/STATUS/CURRENT_TASK、双语README和截图。猫遗传/任务保持上一批状态；1.21.1客户端API移植与FTB实际运行仍未完成，不安装未通过验证的移植包。

## 2026-11-01：持刀姿态首轮修正

用户否定当前持刀效果。第一人称静止握持缩小/下移，恢复装备升降；第三人称只映射手臂旋转、保持肩部固定点。已启动NeoForge检查静止及挥刀截图，见art/animation/grip-revision/。仍有侧视刀身较薄、完整动作美术认可待办，不宣称用户验收通过。原九阶图与伤害不变。

## 2026-10-31：成长与暗黑新图

新增共鸣之心和暗黑喵刀基础 PNG，来自当前内置 imagegen，具体模型名未暴露；原文件无损复制，来源及哈希见 ../art/progression/ASSETS.md。普通九阶原画未修改；暗黑八阶初版共用基础图，独立逐阶原画仍待制作。游戏实际显示截图见 ../art/progression/。


## 2026-10-31：MOTION-03 与成长方案

新增连续挥刀前 0.75 tick 的前姿态过渡，服务端节奏和伤害不变。已启动真实 NeoForge 游戏及两个独立客户端，第二客户端经 TCP 进入同一测试世界，记录 connected=2 并保存 54 张关闭刀光的观察者截图；详见 [实际测试记录](MOTION_MULTIPLAYER_VALIDATION.md)。本机双客户端不等于公网延迟或全部姿态验收。

新增[合成、成长与数值 v2](MEOW_BLADE_PROGRESSION_PROPOSAL.md)：九阶材料/好感/耐久、保留身份的升级事务、暗黑对照与重击候选。全部新增参数为 PROPOSED，尚未改运行时配方和数值。下一批优先完善动作边缘显示与完整操作回归，再按评审后的设计接升级。


## 2026-09-14：真实挥刀动作初版（MOTION-01 / AUTO-05）

CONFIRMED：以 SlashBlade: Resharped `6e2a0a092fb794d7ea56fd83452869674f3ab1c7` 为可替换参考，保留本地 `78ff086` 上全部未提交工作。独立 `slashblade_baseline` 预设已导入原始 VMD/PMD 并解码为明确的 absmod_motion_v1；A3 拆成两个单次授权片段，未复制伤害/眩晕/位移回调。来源和单独许可范围见 [素材记录](../art/animation/slashblade_baseline/NOTICE.md)。

源码状态：参考动作已导入；第一人称武器位置/旋转及握持已接入；第三人称身体/手臂姿态与原持物层已接入。使用 26.2 渲染状态，不引入旧 playerAnimator。原版 swingTime 重置已从确认入口移除；沿用 attackId 去重、服务端节奏与伤害，只扩展表现时序。九阶原图、剑灵与数值保持原状。

验证：双加载器最终 build 通过（build/baseline-motion-final-build.log）；实际五片段采样、收招连续性、快照隔离与协议检查通过；Fabric 12 项 GameTest 通过（build/baseline-motion-validation.log）。实际启动 NeoForge 进入新世界，交付关闭/开启刀光、第一/第三人称、左右手、重载恢复与观察其他玩家样本的[连续截图](../art/animation/slashblade_baseline/game/preview.html)。

游戏验收仍不完整：自动视觉样本不等于真实鼠标/联网测试；其他玩家样本是本地 RemotePlayer，尚无双客户端联机验证。Fabric 客户端、全移动姿态、完整动作/刀光对齐与逐阶握持仍待检查。已有连击层数尚未接入攻速加成，快速确认测试不冒充连击系统完成。详细改动和剩余项见 [实现记录](IMPLEMENTATION_SLASHBLADE_BASELINE.md)。未提交或推送。


## 2026-09-14：使用用户指定护士酒狐模型（ART-02）

CONFIRMED：用户指定直接使用 art/护士酒狐女仆包。以本地 78ff086 与已有未提交修改为准；之前粉白候选外形未获确认，暂停该造型修订。

当前默认为 absmod:models/entity/nurse_wine_fox.geo.json 与 absmod:textures/entity/blade_spirit/nurse_wine_fox.png。原模型196骨骼及默认256×256贴图逐字节复制，几何/旋转/层级/逐面UV完整保留；按原包0.6比例显示。沿用Bedrock渲染路径和基础行走/看向/尾摆绑定，没有引入GeckoLib或YSM。服务端规则、剑灵身份与九阶剑图未改。

实际检查：双端 build 通过（build/nurse-fox-final-build.log），包含实际资源加载、护士骨骼绑定、姿态隔离、失败回退与恢复检查。NeoForge实际进入新测试世界，正面/侧面/背面/斜面已截图检查；资源重载前后 fallback=false、bones=196（build/nurse-fox-client.log）。测试实体为无AI静态外观样本，未验收主人跟随/行走、多实体及Fabric客户端。基础动作自动检查不能代替原包动画验收。

原包依赖的Gecko/Molang专用动作未接入；互斥表情、狐狸形态、GUI背景保留在模型中但不同时显示。来源、哈希与未确认再分发授权见 art/blade_spirit/nurse-wine-fox/NOTICE.md。未提交/推送。改动及剩余项见 [接入记录](IMPLEMENTATION_NURSE_WINE_FOX.md)。


## 2026-09-14：粉白曲面剑灵与重绘贴图（ART-02）

CONFIRMED：用户要求重绘贴图，人物不要原版方正模型。以本地 main / 8a035c5 为基线，开工前工作树干净。

本批新增原创21骨骼/2314多边面模型 blade_spirit_ethereal.geo.json、AI重绘1254×1254图集 ethereal_atlas.png；脸部、长发、裙摆、袖腿和尾巴为真实多边面曲面。Bedrock适配模块新增有限 poly_mesh 子集（索引三角/四边面），仍采用26.2模型提交路径，不依赖GeckoLib/YSM。保留旧资产/占位失败回退及服务端行为。

双加载器 build 与实际资源/法线/UV/姿态隔离/错误索引及加载恢复检查通过，日志 build/ethereal-final-build.log。浏览器正/背/斜/侧面检查（开启背面剔除）已执行，预览现在对应当前新模型。未启动Minecraft，本轮不声称游戏内光照、动画和多人验收。未提交/推送。文件清单与剩余项见 [修订记录](IMPLEMENTATION_ETHEREAL_SPIRIT.md)。


## 最新模型路线（CONFIRMED，2026-09-13）

正式外观采用 BedrockModel 同类的独立骨骼模型加载/动画/绘制，不限制原版人形或128×64共享UV，不以GeckoLib/YSM作为替代主方案。下方旧的人形皮肤布局仅适用于失败回退与历史模型，旧GeckoLib工具链建议已被本次决定替代。

本批交付原创 Bedrock 1.12.0 测试 JSON 和128×128 PNG；宽高由模型声明，可独立改变，骨骼和逐面UV可扩展。它是结构测试模型，正式粉白造型、美术细节与游戏视觉仍未验收。当前不需要用户新增贴图。准确格式与剩余支持范围见 [适配模块说明](../client-bedrock/README.md)，验证见 [本批记录](IMPLEMENTATION_BEDROCK_RENDERER.md)。


## 侧视图修正（2026-09-13）

用户指出侧面诡异。本批 ART-02 调整头/躯干厚度、把悬空花饰放回太阳穴附近、后发后移，并用较平的正面五官区域避免眼睛绕到侧面。保持原图与图集。refine_spirit_model.py、模型 JSON、OBJ 和预览已更新。实际执行双加载器 build、共享几何/比例/动画检查及浏览器侧面截图检查通过；日志 build/spirit-side-validation.log，截图 build/spirit-preview-side.png。未启动 Minecraft，未提交/推送；当前仍为待打磨草模，未宣称正式美术完成。


## 本地模型实测校准（2026-09-13）

用户指出旧比例不自然并提供 PCL2 整合包。已实际读到车万女仆模型：基础头高 8、总高 26、腿长 9。当前剑灵按这些量测校准成 3.25 头身，同时保留自有曲面、衣装及贴图；帽子/耳朵不计入基础头身比。原来的 4 头身与细长腿已替换。来源及检查见 SPIRIT_PROPORTION_REFERENCES.json，正式年龄/游戏身高仍不由这些工程尺寸自动决定。


## 2026-09-13 第二次外观修订

CONFIRMED：减少方盒感。现使用收下颌头部、弯曲尖端发束、收腰身体、圆袖/腿、环绕裙摆和连续弯尾。1768 四边面使用既有图集，未新增图像编辑。详细验证见 SPIRIT_CURVED_VALIDATION.json；不能用浏览器预览替代 Minecraft 验收。


## 2026-09-13 普通剑灵参考图已接入

用户已授权按所供粉白猫娘图建模。参考图原样保留，新 atlas 为 AI 辅助重排/适配，并非逐像素裁切。当前 33 部件模型使用逐面归一化 UV，图集为 1254×1254 不透明 RGB；饰品通过实体几何和不透明色区表现，未使用图集中棋盘背景。旧 128×64 占位 UV 要求不再适用于新模型。模型源、OBJ、纹理和预览见 art/blade_spirit/README.md。身高比例是当前模型实现，不代表确定角色年龄或所有阶段造型。


## 2026-09-12 本地美术接入补充

用户已允许剑灵先使用占位贴图（CONFIRMED）。已加入原创 128×64 粉白 UV 色块、人形猫耳/尾巴模型；正式人设与动画仍待交付，不改已有九阶刀图。请按[剑灵贴图与模型要求](SPIRIT_ART_REQUIREMENTS.md)提供资源。当前编译与服务器测试不代表客户端视觉验收。


> 文档修订：`2026-09-10.repository-progress.1`。本轮仅更新文档；未修改游戏代码，未运行 Gradle 或 Minecraft。

> 面向较低能力的生图 AI、像素美术 AI、模型 AI。  
> 目标：保证后续生成不会再次偏离“喵刀”的核心视觉。

---

# 1. 总原则

普通喵刀最终不是：

```text
厚重大剑
赛博巨刃
重甲机甲剑
尖刺堆砌
```

普通喵刀最终应该是：

```text
细长
梦幻
轻灵
粉白
可爱
猫猫
水晶
半透明
羽片
微光
```

---

# 2. 普通喵刀固定视觉元素

普通喵刀正式主题色：**粉白**。

必须至少保留部分：

- 猫耳。
- 猫爪。
- 爱心晶体。
- 粉白。
- 青蓝冷色高光。
- 轻盈刀刃。
- 后期半透明边缘。

不要每阶都把所有元素堆满。

---

# 3. 九阶视觉演进

## Stage 1

来源：钻石剑。

生成要求：

- 明显接近钻石剑；“约 70% 的视觉关联”仅为美术提案，不是用户确认的量化门槛。
- 青蓝主色。
- 棕色握柄可以保留。
- 粉色只是点缀。
- 猫爪只能作为小细节。
- 不要提前出现终极梦幻羽翼。

关键词：

```text
minecraft diamond sword ancestry
cyan crystal
tiny pink accents
small paw
simple
early evolution
```

## Stage 2

- 仍明显钻石剑。
- 增加粉色边线。
- 护手开始有猫耳暗示。
- 仍简洁。

## Stage 3

- 青蓝仍占主导。
- 粉色开始侵入刀身。
- 猫耳/心形晶体更清楚。
- 不要太复杂。

## Stage 4

- 青蓝与粉白进入过渡。
- 握柄可逐渐从棕色改为粉白。
- 开始出现较优雅轮廓。

## Stage 5

- 中期身份成立。
- 粉白开始成为主色。
- 青蓝作为冷色内芯或高光。
- 结构仍清晰。

## Stage 6

这是美术风格转折点。

必须：

- 变细。
- 变长。
- 变轻。
- 加半透明水晶翼片。
- 爱心晶体更精致。
- 猫元素变成装饰语言，不是贴图贴纸。

## Stage 7

- 比 6 更梦幻。
- 半透明面积略增加。
- 刀刃更修长。
- 可有小型漂浮/连接晶片，但不要满屏粒子。
- 仍然是游戏内能读清的武器。

## Stage 8

- 接近终极。
- 水晶羽片更完整。
- 粉白/淡紫/淡蓝虹彩。
- 保持轻薄。
- 为心魔 Boss 前的“未完成终极形”。
- 不要比九阶更夸张。

## Stage 9

最终普通喵刀。

必须：

```text
very slender
elegant long blade
dreamy
ethereal
pink white
iridescent pale cyan
semi-transparent crystal edges
cat paw motif
heart crystal
light feather-like fins
clean silhouette
```

禁止：

```text
thick greatsword
heavy mecha
huge dark outline
dense spikes
oversized guard
visual clutter
```

---

# 4. 半透明怎么画

“透明背景”不等于“武器半透明”。

武器边缘建议：

- 核心刃：alpha 220～255。
- 外层水晶：alpha 120～210。
- 羽片最外缘：alpha 60～160。
- 高光线：alpha 180～255。
- 不要整个武器都低透明。

要让人感觉：

> 实体核心 + 光质水晶外壳。

---

# 5. 颜色建议

普通后期推荐：

```text
base white:       #FFF5FB
soft pink:        #F7BEDD
main pink:        #EE72B7
highlight pink:   #FF9DD7
lavender:         #DCCAF9
ice cyan:         #A7E8FF
deep accent:      #8D4B79
warm gold small:  #D8AD7A
```

只是方向，不是强制色值。

金色只可少量使用。

---

# 6. 暗黑喵刀

**正式主题色已经确定为：粉紫。**

**[CONFIRMED]** 粉紫主题已确定。
**[PROPOSED / TBD]** 具体辅色比例、材质、服装及逐阶饰品仍是概念提案，不得写成主色未定。
概念图与可直接入包的 UV 材质应分别验收。


暗黑喵刀的主视觉必须以粉紫为核心，而不是传统黑红。允许用少量深紫、冷黑灰或下界合金灰作为结构/阴影色，但这些只能服务于粉紫主题。

推荐方向：

```text
main pink-purple
soft violet
deep violet shadow
small netherite gray
small cold cyan or magenta highlight
```

禁止把主视觉改成：

```text
mostly black-red
blood red
orange lava as dominant color
generic demon sword palette
```

已确认的视觉情绪：

```text
heavier
slower
stronger single hit
netherite origin
tempting power
cost
pressure
```

不要默认：

```text
black-red is final
blood theme
demonic horns
player corruption
```

除非用户后续确认。

---

# 6A. 器灵猫娘美术规范

## 普通器灵

固定主题：

```text
pink + white
catgirl weapon spirit
dreamy
light
crystal
small ice-cyan accents
```

建议让器灵与普通喵刀共享视觉符号：

- 爱心晶体；
- 猫爪纹样；
- 猫耳轮廓；
- 粉白水晶；
- 后期半透明羽片/晶片。

器灵阶段提升时可以通过：

- 衣装局部；
- 发饰；
- 晶体饰品；
- 光效；
- 武器核心纹样；

逐级升级，而不是强制每阶重做完全不同的人体模型。

## 暗黑器灵

固定主题：

```text
pink + purple
deep violet shadow
small netherite-gray structure
catgirl weapon spirit
heavier emotional presence
```

她必须和暗黑喵刀属于同一个视觉家族。

禁止默认：

```text
black-red demon girl
blood theme
horned succubus
evil smile as mandatory
```

暗黑路线的主题是“更直接的力量与代价”，不是通用恶魔模板。

## 器灵与剑的对应

生图/建模必须让人能看出：

```text
this catgirl
⇅ transforms into
this exact sword family
```

最少共享两个以上标识，例如：

- 相同核心爱心晶体；
- 相同猫爪纹；
- 相同粉白/粉紫分区；
- 相同耳形护手和发饰；
- 相同水晶材料。

## 变身视觉

推荐方向：

```text
catgirl silhouette
→ crystal particles / ribbons
→ heart core remains
→ crystal blade extends
→ sword completes
```

第一版代码可无动画；正式美术不得反过来强迫逻辑依赖客户端动画。



## 6A.1 实现工具链候选 [PROPOSED]

推荐评估 Blockbench 建模与 GeckoLib 动画，第一轮可用简单占位 renderer。
这不是用户已选定的依赖，也不是已经接入项目的功能；加入前核实目标 Minecraft、加载器、Java 与实际构建兼容性。
不在文档中凭历史对话锁定 GeckoLib 版本，不因模型工具链选择自动更改武器 2D/3D 方案。

一套基础骨骼 + 普通/暗黑路线贴图 + 分阶段饰品层是成本控制建议。
不是要求所有角色只换色，也不是要求十七个完全独立骨骼。
保持同一位器灵跨阶段的辨识度，至少让剑与器灵共享两处视觉符号。

## 6A.2 第一批模型交付项 [PROPOSED]

- 可编辑模型源文件、明确的骨骼命名与父子关系。
- 绑定目标 UV 布局的贴图，路线为粉白/粉紫；原画不是 UV。
- 不透明主体、半透明晶片和可选发光部分可分别控制。
- 初始动作：idle、walk、look、ear/tail、interact、materialize、recall、collapse。
- 上述动作名是拟自定义名称，不是任何库已包含的猫娘资源。
- 标明用于哪个模型、骨骼版本及目标渲染方案；不随意更换骨骼名称破坏已有动画。

“暗黑攻击有重量”不代表必须画成重甲或巨型角色。
半透明装饰不意味着整个身体必须半透明，具体服装、年龄外观、身高与人格不得凭空写成既定设定。

## 6A.3 变形的逻辑边界 [PROPOSED]

不要求人体逐顶点变成剑。可用轮廓淡出、核心晶体保留、光线收束、剑出现完成视觉连续性。
动画仅表现服务端确认的转换，不能在客户端动画末帧发放真实 ItemStack。
渲染失败或客户端关闭特效时，服务器交接仍须正确完成。
实体模型动画选型与剑的手持模型渲染分开评估。

# 7. 心魔 Boss 美术关系

心魔 = 暗黑喵刀化作的 Boss。

因此 Boss 必须让人一眼联想到暗黑喵刀：

- 共用猫元素。
- 共用核心晶体/武器纹样。
- Boss 的关键攻击可以围绕暗黑刀身。
- 不能变成完全无关的普通魔王。

具体形态目前 TBD。

---

# 8. 2D Sprite 生图 Prompt 模板

```text
Create a transparent-background Minecraft mod item sprite.
Single sword only.
Diagonal from bottom-left to top-right.
No environment.
No hands.
No text.
Readable silhouette.
Pixel-friendly rendering.

Weapon: Meow Blade Stage X.
Evolution from diamond sword.
Cute pink-white cat theme.
Stage-specific color balance: [fill].
Keep cat paw, heart crystal and subtle cat-ear design.
Late stages must be slender, elegant, dreamy and ethereal.
Use semi-transparent crystal outer edges and feather-like fins.
Do not make it a thick greatsword or heavy mecha weapon.
```

---

# 9. 概念原画 Prompt 模板

```text
Fantasy weapon concept art, isolated transparent background.
Meow Blade Stage X of 9.
Long slender blade.
Dreamy pink-white crystal material.
Cute but elegant cat motifs.
Heart crystal near guard.
Small paw emblem integrated into design.
Iridescent cyan highlights.
Semi-transparent crystal wings at outer blade edges.
Light, airy, celestial silhouette.
No bulky greatsword proportions.
No dense spikes.
No black background.
```

---

# 10. 3D 模型 AI 指令

如果后续决定做 3D：

```text
The sword must preserve a thin side profile.
Avoid thick rectangular blade volume.
Main blade thickness should visually read as light.
Transparent crystal fins should be separate mesh/material groups.
Keep opaque core and translucent outer layer separable.
Cat-ear guard should be thin, not chunky.
Heart crystal can be a separate gem mesh.
Prepare UV islands cleanly for 512 or 1024 texture.
Do not bake full concept illustration as one flat plane.
```

---

# 11. 游戏资源检查

每张最终图必须检查：

- [ ] RGBA PNG。
- [ ] 真透明背景。
- [ ] 无黑底残留。
- [ ] 无白色 halo。
- [ ] 缩小到 32/64/128 预览仍能认出。
- [ ] 刀尖没有被裁。
- [ ] 刀柄没有被裁。
- [ ] 方向一致。
- [ ] 九阶进化关系清楚。
- [ ] 后期没有越画越粗。
- [ ] 半透明是真 alpha。


# 12. 修订记录

## 2026-09-09

- 补入候选建模动画管线、交付清单和动画/交接边界，未生成或替换图片。
- 删除暗黑主色仍为 TBD 的旧说明，粉紫主题不变。
- 技术选型和具体服装仍为提案，不因本次写文档自动确认。


# 13. 连击速度的视听表现 [PROPOSED, 2026-09-09]

对应 BALANCE.md：普通九阶基础目标5次/秒，二十层上升到6次/秒。这是节奏草案，不是当前游戏性能结论。
每层轻度加速，用更紧凑的动作间隔表达；不要每次增加刀身粗细或扩大人物体型。
粉白路线使用细长短尾迹、交替横斩/轻斜斩与短促音效。声光以真实攻击编号去重，不能用多道特效伪造多次伤害。
建议附可关闭震动/粒子开关，避免连续白闪遮挡目标；暗黑粉紫路线保持低速重击身份，不继承普通提速。
动作/透明层/数值分开验收；角色外观、生命与好感不会因短期连击换成另一套永久状态。


# 14. 长按连续攻击与现有刀光的衔接

**[CONFIRMED]** 新操作目标是长按自动挥动、不等待原版蓄力；普通粉白、暗黑粉紫不变。
**[PROPOSED]** 用同一主动作编号驱动动作与刀光，一刀一组效果；多道光带不代表多次伤害。
当前 main 的 MeowSlashEffects 已有六种轨迹和半透明光带，不从零覆盖此表现。先连接真实主动作，检查旧 swing 监听是否重复生成。
18 tick 生命周期在持续 5～6 次/秒下会叠加多道效果，需要测试可读性和性能；是否缩短／缩小必须作为美术调优，不把本轮建议直接当用户否定现有风格。
空挥和挖掘的装饰效果不参与伤害／连击／好感。长按不新建剑贴图、不改变器灵角色轮廓。
技术说明见 CONTINUOUS_COMBAT.md 第 9 节。本次未生成图片、未测试 GPU。


# 15. 2026-09-10：实际已存在的刀光与待交付资产

**[VERIFIED_SOURCE]** 已核对 MeowSlashEffects / SlashSwingTracker 和 Fabric、NeoForge 客户端入口。当前刀光基于原版挥手，不改变手臂动画；六种轨迹是视觉循环，不是六层连击或六段伤害。
README 记录 18 tick 生命周期、12 层光带、32 个花瓣状微光、最多32道/32格范围。源码效果管理确有总量与距离限制。上述参数是当前实现，不是本轮重新批准的设计目标，GPU可读性与多人仍未验收。

**[PROPOSED]** 连斩接入优先复用现有几何/渲染，按同一主攻击编号去重。必须区分本地预测、服务端确认、旧 swing 观察器，避免同一刀画两次。高速下验证重叠、遮挡、帧率和音效，不未经用户同意把现有大弧光直接替换成另一种审美。
正式三维刀身/UV、粉白与粉紫器灵模型、实体动画和 GeckoLib 接入仍为待定或待制作。现有程序化刀光不代表这些资产已经存在。详见 REPOSITORY_AUDIT 和 CURRENT_TASK。
