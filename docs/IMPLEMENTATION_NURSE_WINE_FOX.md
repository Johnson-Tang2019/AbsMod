# 护士酒狐接入记录

## 2026-09-14：使用用户指定护士酒狐模型（ART-02）

CONFIRMED：用户指定直接使用 art/护士酒狐女仆包。以本地 78ff086 与已有未提交修改为准；之前粉白候选外形未获确认，暂停该造型修订。

当前默认为 absmod:models/entity/nurse_wine_fox.geo.json 与 absmod:textures/entity/blade_spirit/nurse_wine_fox.png。原模型196骨骼及默认256×256贴图逐字节复制，几何/旋转/层级/逐面UV完整保留；按原包0.6比例显示。沿用Bedrock渲染路径和基础行走/看向/尾摆绑定，没有引入GeckoLib或YSM。服务端规则、剑灵身份与九阶剑图未改。

实际检查：双端 build 通过（build/nurse-fox-final-build.log），包含实际资源加载、护士骨骼绑定、姿态隔离、失败回退与恢复检查。NeoForge实际进入新测试世界，正面/侧面/背面/斜面已截图检查；资源重载前后 fallback=false、bones=196（build/nurse-fox-client.log）。测试实体为无AI静态外观样本，未验收主人跟随/行走、多实体及Fabric客户端。基础动作自动检查不能代替原包动画验收。

原包依赖的Gecko/Molang专用动作未接入；互斥表情、狐狸形态、GUI背景保留在模型中但不同时显示。来源、哈希与未确认再分发授权见 art/blade_spirit/nurse-wine-fox/NOTICE.md。未提交/推送。改动及剩余项见 [接入记录](IMPLEMENTATION_NURSE_WINE_FOX.md)。

改动文件：SpiritModelResources.java（默认资源及显式回退日志）、BedrockSpiritModel.java（原骨骼别名/外观选择/比例）、nurse_wine_fox.geo.json、nurse_wine_fox.png、BedrockModelVerification.java、双语README与美术/状态文档。沿用本地已有NeoForge可选visualtest测试入口，普通构建排除该入口。

保留此前未提交的原创24骨骼候选与生成器形状修订。旧检查误填26骨骼已纠正为实际24；生成器变量遮蔽修复，但未重新生成旧候选或继续造型工作。

游戏截图位于 art/blade_spirit/nurse-wine-fox/。当前preview.html展示本次真实游戏截图；旧候选交互页保存在ethereal-candidate-preview.html。没有用旧预览冒充新模型。
