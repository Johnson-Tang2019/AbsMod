# 樱花竞技场

Minecraft 1.21.1 NeoForge。CONFIRMED 范围为樱花门、专属虚空维度、单座受保护的竞技场、跌落救回与 Boss 地面战。以下尺寸、造门材料和场地布置为本版设计选择。

## 入场与离场

在平地竖起樱花原木框，外宽 4、高 5，内部留 2×3 空气。普通、去皮樱花原木与樱花木均可，四角可省。手持一朵香樱花，点击底梁中间任一块的顶面。穿过花瓣进入场内南侧出生台，朝北可看到心镜。自有普通八阶实体原剑仍按既有试炼资格开启挑战。

跌出浮台会在受到虚空伤害前回到场内出生台。南门根据每个入场者记录的维度和门位置寻找有支撑、能容身的落脚处。找不到时使用主世界出生点。多人共享同一座竞技场。樱花门和已有心镜的玩法见手册 75–76 页。

## 建筑与保护

圆形浮台直径约 111 格。中央半径 34 格为素面浅樱木地板，周围的六层看台通过阶梯连接环形步道。棚面交替使用浅暖色陶面、去皮樱木和石英收边，白色玻璃天窗间留少量粉玻璃。0.11.2 的四个方向各有地标。

| 方位 | 从战斗场能看到的地标 |
| --- | --- |
| 南 | 三道由低到高的拱门迎向入场者，朝北沿中轴前进即可找到心镜。 |
| 东 | 三株樱树从北向南依次为向赛场侧展的斜樱、高处分叉的双干樱，以及枝梢垂落的垂樱。 |
| 北 | 双层观景厅顺着看台弧线展开。由东端楼梯登上露台，左侧是内退的玻璃雅座，右侧是木格遮阳架。 |
| 西 | 三段锯齿形采光棚以竖向磨砂色玻璃采光，棚下沿用连续步道。 |

竞技场固定镜以场心为试炼范围中心，自建心镜以镜为中心、半径 12 格。

[可编辑投影](../art/cherry_arena/cherry_arena.litematic) · [游戏内全景](../art/cherry_arena/pavilion-court.png) · [俯视屋面](../art/cherry_arena/pavilion-plan.png) · [东侧樱庭](../art/cherry_arena/pavilion-grove.png) · [近看垂樱](../art/cherry_arena/pavilion-boughs.png) · [北看台近景](../art/cherry_arena/pavilion-north.png) · [露台花槽与玻璃栏杆](../art/cherry_arena/pavilion-terrace.png) · [南门入场视角](../art/cherry_arena/pavilion-entry.png)

0.11.1 实景留档：[旧版全景](../art/cherry_arena/gallery-court.png) · [南侧拱门](../art/cherry_arena/gallery-gate.png) · [西侧采光棚](../art/cherry_arena/gallery-west.png)。

0.10.0 曾借鉴《灾变》焰魔竞技场的层次及《暮色森林》娜迦庭院的分段围墙。旧版建筑投影用于计算原址更新时应清除的方块。[《灾变》原生成器](https://github.com/lender544/new1.20.1/blob/819937203e22b4fa72fdeccc6c56ad415d65eeef/src/main/java/com/github/L_Ender/cataclysm/structures/Burning_Arena_Structure.java) · [《暮色森林》原庭院结构](https://github.com/TeamTwilight/twilightforest/tree/2bfc2df7d12dae8d3cbe067beeb0c63d1bab1d5e/src/main/resources/data/twilightforest/structure/courtyard) · [上版投影](../art/cherry_arena/grand-court-0.10.litematic) · [旧版全景](../art/cherry_arena/grand-court.png)

维度使用无地层、无原版结构的平坦生成器，专用群系的生物与地物列表为空。服务器在每次开档时准备固定场地。场内的玩家拆建、爆炸破坏及通常通过世界或区块写入的方块变化受保护。此保护应用在竞技场维度，允许生物受伤及掉落战利品。

## 复查和修改建筑

设计源为 `tools/arena/design.py`。依次生成 frame、envelope、roof、ornament，在 v16 中分别检查六视图和方块差异。用 seat_banks 修整分段椅背及内外棚沿厚度，再用 rose_finish 给整个场馆做粉色饰面。0.11.1 在这一基础上用 `tools/arena/gallery_details.py` 分 muted_materials 和 four_galleries 两步改变饰面、打开雨棚并塑造四个方向。0.11.2 用 `tools/arena/pavilion_garden.py` 分三次局部修整。第一步布置平台和无叶树骨，第二步安装窗带、顶棚及座椅，第三步沿各自的枝梢造冠。每层树冠均有独立标高，部分相邻枝团间可以看到天空。导出器 `tools/arena/export_template.py` 接收技能目录、最终投影和目标 NBT，将 0.9.0 的原场地范围、0.10.0、[0.11.0](../art/cherry_arena/round-court-0.11.litematic) 和 [0.11.1](../art/cherry_arena/galleries-0.11.1.litematic) 的实际建筑位置与新投影一起处理。圆场外的旧角楼、树木和屋顶以空气原址替换。服务器在原坐标设置心镜和南门，按新场馆重算覆盖区块的天空光照，并将更新后的区段发送给玩家。

## 首领识别

场内按实体类型标签 absmod:arena_bosses、可选的 c:bosses 和 forge:bosses，以及实体类持有的服务端首领血条识别首领。凋灵、末影龙和心魔显式列入标签。竞技场保留其 AI 与伤害，用每个首领自身宽度约束它在中心场地上的高度与水平范围。普通玩家、宠物和飞行投射物保持各自的运动。使用自定义外置血条又未提供通用 Boss 标签的模组，可用数据包向该标签追加实体 ID。

首领能够以召唤、跨维度等方式入场。花门还支持自由活动的凋灵和未绑定原版末地战役的龙。原版末地战役维持自己的实体生命周期。
