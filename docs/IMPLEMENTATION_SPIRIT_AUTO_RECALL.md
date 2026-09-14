# 剑灵无敌与攻击召回

本轮验证完成：双加载器最终build与持续输入接续CPU检查通过（build/spirit-auto-recall-final.log）；Fabric 12项GameTest通过；实际启动NeoForge进入世界，服务端攻击意图驱动自动召回并确认原剑恢复（build/spirit-auto-recall-client.log）。该测试未模拟物理鼠标/真实客户端网络延迟；Fabric客户端和多人尚未实测。

## 2026-09-14：剑灵无敌与攻击自动收回（SPIRIT-04）

CONFIRMED：用户要求剑灵无敌，攻击自动收回。当前剑灵拒绝hurtServer伤害，包括绕过原版无敌标记的伤害；此前死亡扣罚设计不在此版本触发，不擅自删除历史设定或实现死亡惩罚。

攻击触发采用主手对应虚化剑的现有攻击输入（包括空挥，不包括挖方块）。服务端先验证包序号、槽位、维度、玩家状态、灵魂主人和有效实体，再经同一交接服务恢复原剑，之后由原有唯一攻击节奏器处理攻击。原版攻击包对虚化剑同样拦截，避免先打一次无武器伤害。虚化剑到同身份真剑的同步替换保留按住输入，其他换物/槽位仍要求松键。停止攻击不自动重新显现。

只召回同维度当前已加载的匹配剑灵；实体未加载、身份缺失或不属于玩家则不发剑、不强制加载区块。行政直接移除、跨维度、崩溃恢复和完整变形动画不属于此次伤害免疫实现。

改动：BladeSpiritEntity伤害入口；SpiritTransformationService.recallForAttack；BladeCombatServer攻击接收；ServerAttackPacketMixin；BladeCombatClient与AttackInputTracker；SpiritGameTests与CombatVerification；可选NeoForge视觉测试；双语README及状态规格。保留所有既有未提交美术和切换修改，未提交/推送。

自动验证：双端构建和Fabric全部12项GameTest通过（build/spirit-auto-recall.log），含普通/虚空伤害免疫、复制虚化链接的非主人拒绝、召回原剑和重复调用拒绝。持续按住输入接续的CPU检查另随最终build运行。
