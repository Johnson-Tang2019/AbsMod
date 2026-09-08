# Ab's Mod

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
  参数表示玩家默认基础属性下的总攻击伤害。具体平衡数值尚未提供，九阶段目前均以 7 占位，攻速均为 1.6。
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

## 命令

所有命令的权限均与原版 `/give` 相同，需要游戏管理员权限（通常为 OP 等级 2）。

| 命令 | 用途 |
| --- | --- |
| `/absmod` 或 `/absmod help` | 查看命令帮助 |
| `/absmod stages` | 列出九阶段、ID、基础伤害和攻速，并提示占位状态 |
| `/absmod give <targets> <stage>` | 给每个目标发放一把指定阶段喵刀 |
| `/absmod kit <targets>` | 给每个目标发放九阶段各一把的测试套装 |
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

## 开发约定

技术命名使用含义明确的英文，不使用拼音。代码注释和文档字符串使用英文。
`README.md` 与 `README.zh-CN.md` 内容保持同步，具体规则见 [CONTRIBUTING.md](CONTRIBUTING.md) 和 [AGENTS.md](AGENTS.md)。

## 游戏内验收

分别运行两种加载器的客户端，确认模组加载、战斗栏物品、中文/英文名称和手持模型；
用原版钻石剑对照喵刀的属性提示、攻击冷却和攻击行为，检查附魔与钻石修理。
命令需检查一阶和九阶、拒绝 0 和 10、OP 与普通玩家权限、多个目标、控制台及满背包场景。
构建成功只能验证编译和打包，不能代替游戏内验收。

`build` 会通过 `:fabric:verifyCommands` 测试共享命令逻辑，在不启动游戏服务器的情况下验证合法语法、
非法阶段数值、缺失参数、权限限制和只读命令执行。两个加载器均执行编译；NeoForge 运行行为、物品入包及主手检查仍需游戏内验收。

2026-09-08 验证结果：JDK 25 / Gradle 9.5.1 下两个加载器均构建成功；
已对照 Minecraft 26.2 原版 `Items` 中的钻石剑配置，并检查两个 JAR 的共享资源、
物品类、版本元数据及加载器隔离。尚未进行游戏内实测。

## 构建依据

- [NeoForge 26.2 MDK](https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle)
- [Fabric 26.2 示例](https://github.com/FabricMC/fabric-example-mod/tree/26.2)

依赖版本在根目录 `gradle.properties` 和 `build.gradle` 中固定。
