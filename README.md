# Ab's Mod

包名：`com.abyssredemption.absmod`，模组 ID：`absmod`。

## 项目结构

| 目录 | 用途 |
| --- | --- |
| `common/src/main/java` | 26.2 两种加载器共用的物品行为与常量 |
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

每阶段都有独立的客户端物品定义和模型 JSON，贴图暂引用原版钻石剑。
收到正式 PNG 后，将文件放入 `common/src/main/resources/assets/absmod/textures/item/`，
文件名与该阶段 ID 对应，例如 `meow_blade.png`、`meow_blade_stage_2.png`。
在 `common/src/main/resources/assets/absmod/models/item/` 中修改同名 JSON 的
`textures.layer0`，例如二阶改为 `absmod:item/meow_blade_stage_2`。
两种加载器会同时使用新贴图，各阶段可单独替换。

## 游戏内验收

分别运行两种加载器的客户端，确认模组加载、战斗栏物品、中文/英文名称和手持模型；
用原版钻石剑对照喵刀的属性提示、攻击冷却和攻击行为，检查附魔与钻石修理。
构建成功只能验证编译和打包，不能代替游戏内验收。

2026-09-08 验证结果：JDK 25 / Gradle 9.5.1 下两个加载器均构建成功；
已对照 Minecraft 26.2 原版 `Items` 中的钻石剑配置，并检查两个 JAR 的共享资源、
物品类、版本元数据及加载器隔离。尚未进行游戏内实测。

## 构建依据

- [NeoForge 26.2 MDK](https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle)
- [Fabric 26.2 示例](https://github.com/FabricMC/fabric-example-mod/tree/26.2)

依赖版本在根目录 `gradle.properties` 和 `build.gradle` 中固定。
