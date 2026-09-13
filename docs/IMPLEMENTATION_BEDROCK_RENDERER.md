# Bedrock 剑灵渲染首批记录

2026-09-13；任务 ART-02 / SPIRIT-CLIENT-01。开始前核对 main / HEAD `2e42020` 与大量未提交工作，以本地实现为准，未覆盖其他功能、未提交或推送。

## 源码改动

| 文件 | 本批内容 |
| --- | --- |
| `client-bedrock/module.gradle`、`src/main/java/.../BedrockGeometry.java` | 独立源码适配模块，由两个加载器各自编译；Bedrock JSON→骨骼/UV→26.2 ModelPart，无加载器专属导入 |
| `client-bedrock/README.md`、`UPSTREAM.md`、`upstream-manifest.json`、MIT notice | 格式范围、具体上游版本、源码哈希、许可证及发布包声明 |
| 根 `build.gradle` | 显式引入源码模块，不更换 ModDevGradle/Loom，不添加旧版运行依赖 |
| `common/.../client/spirit/BladeSpiritRenderer.java` | 主路径从 HumanoidMobRenderer 改为 MobRenderer + 独立 EntityModel |
| `BedrockSpiritModel.java` | 全骨架重置、行走、已有服务端视角驱动转头、尾根/尾尖运动 |
| `SpiritModelResources.java` | 模型和 PNG 成对检查；失败回退到内置旧占位几何；每次渲染器重建重新读取，无静态可变模型缓存 |
| `assets/absmod/models/entity/blade_spirit_test.geo.json` | 原创 Bedrock 1.12.0 单 geometry，10 骨骼，头/身/四肢/双耳/两段尾 |
| `assets/absmod/textures/entity/blade_spirit/bedrock_test.png` | 原创128×128诊断PNG，独立部位色块及单独正脸，非正式美术 |
| `BedrockModelVerification.java`、`SpiritModelVerification.java` | 实际资源载入、层级/法线/UV、姿态交错、异常资源与恢复检查 |
| `26.2NeoForge/build.gradle` | 修复开发客户端参数文件中 Ab'sMod 单引号被解析截断的问题 |
| 双语README、STATUS、CURRENT_TASK、文档入口与美术规范 | 更新正式路线及真实验证边界，保留历史 |

服务端实体、注册 ID、身份、跟随、显现/召回和所有战斗参数未改。两端已有客户端入口注册同一渲染器；NeoForge `Dist.CLIENT` 与 Fabric client entrypoint 保持隔离。此模块是源码适配边界，不是一个已发布、可替代上游的独立模组。

## 编译与自动检查

- `:neoforge:build`：首先通过，`build/bedrock-neoforge.log`。
- 最终根 `build`：双端构建及共享验证通过，`build/bedrock-final-build.log`。
- CPU检查：真实模型10骨骼/60面、有限顶点/单位法线/UV范围、父子关系、A→B→A姿态不串扰、两个模型实例隔离、环/缺失父骨骼/不支持格式拒绝、缺失资源/损坏JSON/损坏PNG回退、修复资源后重新加载恢复。故障注入的 WARN 是预期日志，不是构建失败。
- `-PgameTests :fabric:runGameTest`：9项模组测试加1项框架测试，全部10项通过。启用 JVM `class+load` 日志，本模组 `client` / `bedrock.client` 包没有加载记录；日志 `build/bedrock-server-classload.log`。
- 两个输出JAR均检查到模型、PNG、适配类和MIT notice。
- `git diff --check` 通过；既有文件存在Git提示的CRLF→LF规范化警告。

## 客户端显示

实际启动 NeoForge 26.2 客户端。第一次启动的原版资源目录被截断为 `D:\Ab`，见 `build/bedrock-client.log`。修复参数引号后再次启动，资源加载阶段记录 `Ab's Mod Bedrock spirit loaded ... (10 bones)`，见 `build/bedrock-client-retry.log`。随后停止本次测试进程。

这只证明客户端启动和模型资源载入，**没有进入世界观察实体，没有游戏截图，不认定头/身体/耳尾的实际 GPU 显示验收通过**。浏览器旧预览仍是前一版橙，不能用作本批视觉证据。Fabric客户端本轮未启动。

## 动画结果

源代码和CPU检查确认四肢反相摆动、头部俯仰/偏航、尾根/尾尖摆动，以及每个渲染状态前重置。26.2的绘制实现会在每次实际绘制前调用 `setupAnim(state)`；因此未在提取状态时依赖某个全局最终姿态。没有新增服务端行为。

**游戏世界内看向主人、实际移动时的动作、多人同屏及F3+T恢复均未视觉验收**。资源恢复已在CPU载入路径验证，不能替代真实重载期间的GPU检查。

## 剩余兼容项与下一步

1. NeoForge测试世界召出两个剑灵，分别面向不同方向和移动；检查正/背/侧面、光照、关节、耳尾及同屏姿态。
2. 资源包临时破坏模型/PNG，按F3+T观察回退；恢复后再次重载，确认实体立即恢复且没有旧姿态。不要修改现有存档的服务端规则来测试美术。
3. Fabric客户端运行与NeoForge独立服务端启动仍待验证；当前Fabric服务器类加载证据不能冒充NeoForge服务器证据。
4. 正式粉白猫娘美术、透明材质分层、模型选择、多 geometry、旧格式、Bedrock动画/Molang、女仆包清单/JS、定位骨骼装备层及第三方渲染优化兼容尚未实现。不声明全女仆模型包、Gecko或YSM兼容。

目前不需要用户提供新PNG；已有原创测试资源足以继续客户端验收。正式模型与UV布局确定后再提供相应贴图要求。
