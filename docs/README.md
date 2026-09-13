# Ab's Mod 文档入口

当前客户端模型路线及本批验证以 [Bedrock 适配记录](IMPLEMENTATION_BEDROCK_RENDERER.md) 为准；旧预览和旧格式说明保留为历史。


## 普通剑灵参考图模型（2026-09-13）

已按用户参考图接入模型及新 UV 图集：[可编辑资源说明](../art/blade_spirit/README.md)、[可旋转预览](../art/blade_spirit/preview.html)、[本次验证](SPIRIT_MODEL_VALIDATION.json)。旧的 128×64 占位贴图要求只适用于旧模型。


## 最新本地实现索引（2026-09-12）

- [喵娘剑灵手动原型与剩余边界](IMPLEMENTATION_SPIRIT_PROTOTYPE.md)
- [具体贴图、UV、模型与动画需求](SPIRIT_ART_REQUIREMENTS.md)
- [本批执行证据](SPIRIT_VALIDATION.json)

本地源码进度优先看 STATUS 的顶部补充；导入包的远端历史快照不代表当前工作树。


> 本地补充（2026-09-12）：输入/自动伤害、创造标签、身份存档及 Fabric 运行时检查已存在；本批继续 BAL-02 有效主命中与层数状态。属性加成尚未接入。以 [STATUS](STATUS.md) 和 [本批记录](IMPLEMENTATION_COMBO_STATE.md) 为准；下文“未实现”仅指远端快照。


> 文档包修订：`2026-09-12.repository-progress.1`  
> 代码快照：`2e420203619b5d9cf0f1af7fbc5135ff8858f34f` · 核对日期：2026-09-12

## 先看结论

2026-09-12 复核：远端 main 相对 2026-09-10 包没有新增提交，compare 返回 identical。九阶物品、奶昔和客户端刀光仍按同一代码快照保留；长按/蓄力替换/连击/器灵仍待实现。本次没有新增玩法或修改数值，没有推送、访问本地工作树或运行游戏。

本轮更新进度核对日期、证据来源、任务交接模板与包级检查。故事、器灵、美术、完整连续战斗规格及平衡 JSON 保留上一版内容；专项文件的原修订日期不强行改为今天，以免制造内容变化。包级最新修订以 DOCUMENT_MANIFEST.json 为准。

## 阅读顺序

[STATUS](STATUS.md) → [CURRENT_TASK](CURRENT_TASK.md) → 对应专项。出现“已完成”争议时检查[REPOSITORY_AUDIT](REPOSITORY_AUDIT.md)和实际本地代码。

| 文件 | 用途 |
| --- | --- |
| [DEVELOPMENT.md](DEVELOPMENT.md) | 完整开发主规格、真实工程基线与不变ID |
| [LORE.md](LORE.md) | 故事设定、两路线、心魔与器灵关系 |
| [SPIRIT_SYSTEM.md](SPIRIT_SYSTEM.md) | 身份、好感、实体、转换、死亡与恢复 |
| [ART_GUIDE.md](ART_GUIDE.md) | 粉白/粉紫、细长轻灵、资产与动作规范 |
| [BALANCE.md](BALANCE.md) | 普通9阶/暗黑8阶与连击候选数值、技术依赖 |
| [BALANCE_PROFILE.json](BALANCE_PROFILE.json) | 机器可读设计表，不是已加载的运行时配置 |
| [CONTINUOUS_COMBAT.md](CONTINUOUS_COMBAT.md) | 长按、取消原版蓄力、服务端节奏和兼容主规格 |
| [COMBAT_INPUT.md](COMBAT_INPUT.md) | 旧名称跳转及INPUT/AUTO任务编号映射 |
| [STATUS.md](STATUS.md) / [CURRENT_TASK.md](CURRENT_TASK.md) | 源码事实、TODO和推荐下一批任务 |
| [REPOSITORY_AUDIT.md](REPOSITORY_AUDIT.md) / [JSON](REPOSITORY_AUDIT.json) | 固定commit证据、文件blob SHA、核对范围 |
| [DOCUMENT_CHANGELOG.md](DOCUMENT_CHANGELOG.md) | 本轮变更及来源合并说明 |
| [BALANCE_VALIDATION.json](BALANCE_VALIDATION.json) | 历史数值检查报告，未冒充新游戏实测 |
| [CONTINUOUS_COMBAT_VALIDATION.json](CONTINUOUS_COMBAT_VALIDATION.json) | 历史输入参考模拟报告，本轮未复跑 |
| [PACKAGE_VALIDATION.json](PACKAGE_VALIDATION.json) | 本轮文档/公式检查实际结果 |
| [DOCUMENT_MANIFEST.json](DOCUMENT_MANIFEST.json) | 包内文件相对路径、大小和SHA-256 |

## 真相与提案的优先级

最新明确用户决定控制“要做什么”；源码控制“已经做了什么”；测试结果控制“验证到哪一步”。不能让任一层代替另两层。
CONFIRMED是用户设定；PROPOSED/DRAFT是实现或文案建议；TBD需等决定；VERIFIED_SOURCE是源码证据而不是玩法批准。
本包保留前轮长文，不把简化索引替代完整规格。普通九阶22/5.0→35.2/6.0仍是候选，远端不是该数值。

## 安全导入

先阅读包根[APPLY_TO_REPO.md](../APPLY_TO_REPO.md)，按差异合并，不覆盖本地领先的STATUS或其他未提交内容。
[AGENTS_APPEND.md](../AGENTS_APPEND.md)是待合并片段，不是要替换现有AGENTS。本包不包含实际游戏源码、贴图、模型或JAR。
