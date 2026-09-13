# 运行时验证：AUTO-06 / BAL-03 局部验收

日期：2026-09-12。范围：完成文档要求的服务端伤害机制首批验证，不将部分检查标为完整游戏体验验收。

## 本次改动

- `26.2Fabric/build.gradle`：通过 `-PgameTests` 显式启用独立 GameTest source set；默认构建流程保持原状。
- `26.2Fabric/src/gametest/resources/fabric.mod.json`：独立测试模组与测试入口。
- `26.2Fabric/src/gametest/java/com/abyssredemption/absmod/gametest/BladeCombatGameTests.java`：七项真实服务器世界测试。
- 中英文 README、STATUS、CURRENT_TASK 与本记录：运行方式、实际结果和剩余边界。

## 实际结果

JDK 25 / Gradle 9.5.1，执行：

```powershell
./gradlew.bat --gradle-user-home .gradle-user-home -PgameTests build
```

构建成功；Fabric 与 NeoForge 均编译。Fabric GameTest 日志报告 8 项 required tests 全部通过，其中 7 项为本模组测试，另 1 项为框架测试。游戏测试服务器实际启动、加载世界、执行测试并正常关闭；没有启动图形客户端或 NeoForge 服务器。

| 本模组测试 | 本次结果 |
| --- | --- |
| 连续两次授权主攻击 | 每次实际扣 7 生命，原有 17 tick 冷却保持；授权外强度不为满值 |
| 持续高速命中 | 6 次/秒预算在 200 游戏 tick 内完成 60 次实际主命中，合计扣 420 生命 |
| 原版未蓄力攻击 | 实际伤害大于 0、小于满值 7 |
| 目标无敌 | 授权攻击不扣血 |
| 护甲 | 授权伤害仍被减免 |
| 普通伤害冷却历史 | 先承受普通 6 点，再承受授权 7 点；普通 3 点被拒绝，普通 8 点只扣差值 2 |
| 创造标签 | 注册成功，图标为一阶喵刀 |

高速测试使用显式基础属性、模拟玩家和受控牛目标，调用生产 AttackCadence 与授权 Player.attack 管线。测试服务器可加速运行，200 游戏 tick 不宣称等于实测 10 秒墙钟；这也不是九阶满连击 DPS 测试。范围/视线选择、输入会话和网络接线不在此夹具中。

日志：`build/runtime-validation-final.log`。机器可读摘要见 `RUNTIME_VALIDATION.json`。双端发布 JAR 已检查不包含测试代码；测试 metadata 入口解析检查和 git diff --check 通过。

技能附带的旧版 Bash 布局检查器在 Windows 因 signal pipe 权限错误未能运行；其 1.21 模板规则不作为 26.2 结论。实际以当前 Fabric API、测试入口检查及 GameTest 启动结果核对。本次没有写入 EULA 接受设置；使用 Fabric 自带测试服务器流程。

## 仍待验证与开发

- NeoForge 同类运行时回归；真实玩家输入、网络、客户端 Mixin、盾牌、PvP保护、事件取消、嵌套反伤和多人视觉。
- 创造标签的界面位置、翻页和实际取物。
- BAL-02 连击层数、有效主命中结果接线、临时增伤/攻速、衰减与清理。
- 当前批次不需要新增贴图；暗黑喵刀、器灵实体或正式 3D 资源开始前再列出资产要求。

原有未提交改动已保留，本次没有提交或推送。

接口参考：[Fabric 26.2 自动测试](https://docs.fabricmc.net/develop/automatic-testing)、项目已解析的 26.2 GameTestHelper 与 Fabric GameTest API。
