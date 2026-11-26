# 樱花竞技场

Minecraft 1.21.1 NeoForge。CONFIRMED 范围为樱花门、专属虚空维度、单座受保护的竞技场、跌落救回与 Boss 地面战。以下尺寸、造门材料和场地布置为本版设计选择。

## 入场与离场

在平地竖起樱花原木框，外宽 4、高 5，内部留 2×3 空气。普通、去皮樱花原木与樱花木均可，四角可省。手持一朵香樱花，点击底梁中间任一块的顶面。穿过花瓣进入场内南侧出生台，朝北可看到心镜。自有普通八阶实体原剑仍按既有试炼资格开启挑战。

跌出浮台会在受到虚空伤害前回到场内出生台。南门根据每个入场者记录的维度和门位置寻找有支撑、能容身的落脚处。找不到时使用主世界出生点。多人共享同一座竞技场。樱花门和已有心镜的玩法见手册 75–76 页。

## 建筑与保护

0.12.0 的樱月试炼花庭是露天的圆形首领场地，台面直径约 111 格。中央半径 34 格为素面浅樱木，南门到心镜全程平地。六层看台、座椅、悬挑雨棚、观景厅和露台均已拆除。

| 方位 | 地标与用法 |
| --- | --- |
| 南 | 两束高低不同的樱枝在门上交叠，枝下垂着暖色提灯。离场时穿过枝下的粉色花门。 |
| 北 | 弦月雕塑从浅水池畔升起，小星垂在月缺里，花枝从另一侧探过来。月池用于辨认方向，试炼在场心偏北的原心镜开启。 |
| 西 | 弯曲的浅溪由原版水方块构成，两处踏石横过水面，岸边长有牡丹、粉色郁金香和落英，水面散着睡莲。 |
| 东 | 斜樱、双干樱和垂樱与战斗地面同高。 |
| 场缘 | 西北、东南各有一朵带凹口和浅色花蕊的立体五瓣樱花，另外两处布置低矮的卷瓣花苞，瓣间留出看向虚空的空隙。 |

竞技场固定镜以场心为试炼范围中心，自建心镜以镜为中心、半径 12 格。

[可编辑投影](../art/cherry_arena/cherry_arena.litematic) · [花庭全景](../art/cherry_arena/moon-court.png) · [入场](../art/cherry_arena/moon-entry.png) · [弦月花池](../art/cherry_arena/moon-altar.png) · [西侧花溪](../art/cherry_arena/moon-brook.png) · [五瓣樱花](../art/cherry_arena/moon-blossom.png) · [垂灯花门](../art/cherry_arena/moon-gate.png) · [心镜战斗](../art/cherry_arena/moon-fight.png)

### 《灾变》的场地参考

核对了 L_Ender 的 Burning Arena 八个模板，文件与指定提交的 Git 摘要逐件一致。按生成器拼接后包围盒为 85×85×82 格（X、Z、Y），上半部分抬高 48 格。火焰祭坛位于模板坐标 (42, 44, 42)，断面可见开敞的战斗空间。花庭借鉴「穿门入场、留出战斗空间、由祭坛定位」的关系，转译成垂灯樱门、开敞圆场和弦月花池。建筑方块均为本项目独立布置。[焰魔竞技场生成器](https://github.com/lender544/new1.20.1/blob/819937203e22b4fa72fdeccc6c56ad415d65eeef/src/main/java/com/github/L_Ender/cataclysm/structures/Burning_Arena_Structure.java) · [原模板目录](https://github.com/lender544/new1.20.1/tree/819937203e22b4fa72fdeccc6c56ad415d65eeef/src/main/resources/data/cataclysm/structures)

0.11.2 留档：[北侧观景厅](../art/cherry_arena/pavilion-north.png) · [旧投影](../art/cherry_arena/pavilion-0.11.2.litematic)。

0.11.1 实景留档：[旧版全景](../art/cherry_arena/gallery-court.png) · [南侧拱门](../art/cherry_arena/gallery-gate.png) · [西侧采光棚](../art/cherry_arena/gallery-west.png)。

0.10.0 曾借鉴《灾变》焰魔竞技场的层次及《暮色森林》娜迦庭院的分段围墙。旧版建筑投影用于计算原址更新时应清除的方块。[《灾变》原生成器](https://github.com/lender544/new1.20.1/blob/819937203e22b4fa72fdeccc6c56ad415d65eeef/src/main/java/com/github/L_Ender/cataclysm/structures/Burning_Arena_Structure.java) · [《暮色森林》原庭院结构](https://github.com/TeamTwilight/twilightforest/tree/2bfc2df7d12dae8d3cbe067beeb0c63d1bab1d5e/src/main/resources/data/twilightforest/structure/courtyard) · [上版投影](../art/cherry_arena/grand-court-0.10.litematic) · [旧版全景](../art/cherry_arena/grand-court.png)

维度使用无地层、无原版结构的平坦生成器，专用群系的生物与地物列表为空。服务器在每次开档时准备固定场地。场内的玩家拆建、爆炸破坏及通常通过世界或区块写入的方块变化受保护。此保护应用在竞技场维度，允许生物受伤及掉落战利品。

## 复查和修改建筑

以 `tools/arena/moon_garden.scene.json` 的混合场景方案确定门、月、溪流和樱树的位置，用 v16 的 compile_world_scene 检查南北视线。从 0.11.2 投影依次应用 `tools/arena/moon_garden.py` 生成的 garden_silhouette、garden_planting、garden_grove 三个补丁，分开检查剪影、植物水面和东侧树冠。导出器读取历代投影，将旧建筑多出的位置写为空气。游戏加载的是 1.21.1 原生结构模板（DataVersion 3955）。运行时放置花门与心镜并重算天空光照。

## 首领识别

场内按实体类型标签 absmod:arena_bosses、可选的 c:bosses 和 forge:bosses，以及实体类持有的服务端首领血条识别首领。凋灵、末影龙和心魔显式列入标签。竞技场保留其 AI 与伤害，用每个首领自身宽度约束它在中心场地上的高度与水平范围。普通玩家、宠物和飞行投射物保持各自的运动。使用自定义外置血条又未提供通用 Boss 标签的模组，可用数据包向该标签追加实体 ID。

首领能够以召唤、跨维度等方式入场。花门还支持自由活动的凋灵和未绑定原版末地战役的龙。原版末地战役维持自己的实体生命周期。
