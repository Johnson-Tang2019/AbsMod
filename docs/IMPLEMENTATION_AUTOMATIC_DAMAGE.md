# 自动伤害与取消蓄力实现记录

日期：2026-09-10。用户明确追加要求接入自动伤害和取消蓄力；本记录接续 IMPLEMENTATION_COMBAT_PROBE.md，后者为诊断阶段历史记录。

## 实现范围

- AUTO-01/02：BladeCombatClient / BladeCombatServer 接管喵刀主手输入与服务端节奏；登录启用，日志开关不刷新恢复或禁用攻击。原版实体攻击包不能另行造成一次喵刀伤害。
- AUTO-03：BladeAttackExecutor 使用服务端当前位置、朝向和攻击距离射线选取目标，经距离、视线和世界检查后调用 Player.attack。BladeAttackScope 仅在这次调用内覆盖蓄力系数，保留普通近战管线；Fabric 注册 AttackEntityCallback，NeoForge 保留原生攻击事件。
- AUTO-04：LivingDamageMixin 只允许同一主目标和 DamageSource 首次伤害调用绕过受伤冷却读值，并跳过该调用对 invulnerableTime / lastHurt 的写入。嵌套攻击、额外伤害和其他目标不继承授权；异常用 finally 清理。不移除其他无敌/护甲/盾牌等原生检查。
- AUTO-05：BladeSwingPayload 发送玩家与 attackId；附近客户端按 ID 去重，播放原粉色六轨迹刀光和挥手，避免同时走旧主手挥刀观察器。HUD 仅在主手喵刀且自动攻击通道可用时隐藏原版蓄力指示，不修改用户设置。
- 未接入 BAL-02 连击层数，不增加升级或阶段切换机制。

## 主要改动位置

共享 combat/、network/、mixin/、client/BladeCombatClient.java、client/MeowSlashEffects.java；两个加载器的事件/网络入口与 Mixin 元数据；CombatVerification；中英文 README、语言资源、STATUS 和 CURRENT_TASK。

## 实际验证

- JDK 25，Gradle 9.5.1，根项目 build 成功，Fabric 与 NeoForge 均产出 JAR。
- 共享独立检查通过：阶段属性、分数节奏、重按预算、旧包拒绝、输入停止、数据包编码，以及新增授权隔离、嵌套调用拒绝、来源/目标限制、异常清理。它们不是世界内伤害测试。
- Fabric --initSettings 初始化启动检查通过；没有加载世界。首次发现继承字段 Shadow 定位错误，已修复并复查。
- NeoForge --initSettings 能进入初始化，但 FML 因没有实际服务器线程而退出，不能算成功的服务器启动验收；没有自动接受 EULA。
- 构建日志：build/automatic-damage-build.log（本地忽略文件）。旧 LOCAL_VALIDATION.json 是上一批快照。

## 待游戏内验收

双加载器分别验证：持续按住、快速点击、失焦/界面/换物品停止；各攻速下实际伤害频率；满强度攻击与原版武器对照；盾牌、护甲、无敌、PvP限制与事件取消；反伤及其他伤害不继承局部绕过；近旁多人每次动作只显示一次刀光；HUD 和挖掘行为。客户端 Mixin 与实际网络/渲染尚未启动验收，不声称上述项目已通过。

本次未提交或推送。
