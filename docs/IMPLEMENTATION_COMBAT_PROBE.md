# 连续攻击最小切片实现记录

日期：2026-09-10。工作树：main，HEAD `2e420203619b5d9cf0f1af7fbc5135ff8858f34f`；包含此前未提交的器灵身份与半透明刀光改动，本次保留这些实现。本次没有提交或推送。

## 任务范围

- BAL-01：采用 BALANCE_PROFILE 普通九阶候选总伤害与总攻速，仍为 PROPOSED。伤害减去玩家基础和钻石材质奖励后交给 sword 构造，总攻速使用 S0 - 4。`/absmod stages` 读取同一阶段表；原版属性 tooltip 显示实际默认修饰，另显示候选测试提示。
- AUTO-01：已接双加载器网络及客户端 tick 输入采样，包含会话 epoch、全连接递增 sequence、held、slot 和维度提示。不发送目标、伤害、攻速、层数或永久器灵身份。
- AUTO-02：只实现诊断动作授权。每玩家单一预算独立于输入会话，按服务端 tick 推进，正常连续输入保留小数余量；重复包不推进预算、停止不赠首刀、换成更快武器不能缩短前一刀尚未完成的恢复、不追赶墙钟或跳过的 tick。
- AUTO-03/04、实际长按伤害、连击 Buff、按 attackId 驱动刀光均未实现。此时原版仍是唯一伤害入口；诊断授权不等于实际攻击或有效命中。

## 使用

管理员执行 `/absmod combat probe @s true`，关闭聊天，主手持喵刀并松开攻击键，再按住游戏内映射的攻击键。建议先对空气测试。`/absmod combat inspect @s` 查看会话及累计诊断授权；`/absmod combat probe @s false` 关闭。

服务端日志中的 `AbsMod combat probe ONLY` 行记录玩家、tick、唯一 attackId、epoch、阶段、有效攻速及 `damageApplied=false`。该标记只表示原型没有施加伤害，不代表同一时间原版攻击没有造成伤害。没有每刀客户端确认或预测特效，不会双播刀光。

默认关闭，每玩家开启，断线清理且服务器停止清理；并非持久化配置。即使客户端发送请求，未启用的玩家也不会建立测试状态。

## 输入和权限边界

- 使用游戏 Attack 绑定的按住状态，客户端 tick 采样，非逐帧发送。极短的 tick 间点击与高低 FPS 必须游戏内验收。
- 屏幕/失焦/暂停、使用物品或按住使用键、方块准星/正在挖掘、死亡/旁观/睡眠/手部忙碌等本地状态停止；中断后需要松开再按。
- 服务端再次检查存活/旁观/睡眠/使用物品、维度交接、客户端加载状态、菜单、主手单把普通喵刀及槽位和维度提示。
- 活动会话绑定真实 ItemStack 对象、槽位、世界及可选 soul 引用；同类型替换也终止。正常耐久变化不视为新武器；有 soul 引用时只读校验档案，不初始化永久身份。
- GUI/失焦由客户端停止消息及服务端超时保护；服务器不能凭包证明物理按键或准星。此阶段不接目标/射线校验，亦不允许其产生伤害。
- 5 客户端 tick 心跳、15 服务端 tick 超时为集中定义的传输测试参数，非已定玩法。停止/超时保留会话墓碑，旧 epoch 或非递增序号不能重开。
- 读取玩家当前有效 ATTACK_SPEED 一次，不再乘连击因子。诊断预算最多 20 次/秒、每 tick 一次；超出时记录日志，不修改外部 Mod 属性。无效/非有限攻速不授权。
- 关闭/重新开启保留本连接动作预算；断线后的新连接是新临时状态，没有创建第二套永久 soulId。

## 文件职责

| 文件 | 职责 |
| --- | --- |
| `common/.../item/MeowBladeStage.java`、`MeowBladeItem.java` | 候选基础属性与提示 |
| `common/.../combat/AttackCadence.java` | 纯 tick 动作预算 |
| `common/.../combat/AttackInputSession.java` | 服务端序号、停止与过期 |
| `common/.../combat/AttackInputTracker.java` | 客户端输入边沿与心跳策略 |
| `common/.../combat/CombatProbeSettings.java` | 传输和诊断预算参数 |
| `common/.../combat/CombatProbeServer.java` | 持物/玩家校验和诊断授权 |
| `common/.../client/CombatProbeClient.java` | 读取当前游戏绑定与界面状态 |
| `common/.../network/AttackIntentPayload.java`、`CombatProbePayload.java` | 共享意图和测试控制数据包 |
| `common/.../command/CombatProbeCommands.java` | 管理员开启和查询 |
| `26.2Fabric/.../FabricCombatProbe.java`、`26.2NeoForge/.../NeoForgeCombatProbe.java` | 对应平台注册、tick 和连接生命周期 |
| 两端 `AbsMod*Client.java` | 客户端控制消息和输入 tick 接线 |
| `common/src/verification/.../combat/CombatVerification.java` | 实际生产逻辑的独立验证 |

## 验证与后续

自动验证覆盖九阶 JSON 对照、主手实际属性与副手不加成、1.8～20 次/秒及超限攻速、200 tick 内 60 次六速授权、4/3/3 间隔、随机连点及快慢武器切换预算、同 tick 重复推进、停顿不囤多刀、非有限值拒绝、序号重放/过期、独立会话、客户端停止与重新按下、数据包编码往返。命令权限、奶昔、刀光与器灵存档测试继续运行。

以上是独立 JVM 验证，不是加载器网络收发、物理输入、Minecraft 玩家保存、GPU 或真实伤害验收。最新构建结果见 STATUS。下一步先在两个加载器开启测试原型，验证网络/输入/持物/超时日志；再单独实现 AUTO-03/04，选择唯一伤害入口并测试局部蓄力与受伤窗口，之后接 AUTO-05 刀光去重。不能用当前授权计数宣称 5～6 次/秒有效伤害已完成。

API 对照：[Fabric 26.2 networking](https://docs.fabricmc.net/develop/networking)、[NeoForge payload registration](https://docs.neoforged.net/docs/networking/payload/)；具体签名以本地锁定 26.2 依赖编译为准。

导入完整性：原 ZIP 及导入前本地文件均通过 DOCUMENT_MANIFEST 的 19 项 SHA-256/大小校验，所有 docs JSON 可解析。原 manifest 和 PACKAGE_VALIDATION 保留为输入文档包历史证据；本次文档修改后不应再用原 hash 判定当前工作区未修改。
