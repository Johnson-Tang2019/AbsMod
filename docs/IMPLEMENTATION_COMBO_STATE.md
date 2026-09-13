# BAL-02：有效命中与连击状态

日期：2026-09-12。此批为可独立验收的状态切片，不是完整增伤/提速实现。

## 已实现

生产 BladeAttackExecutor 返回实际主命中结果。LivingDamageMixin 在唯一授权主调用中观察生命与吸收总值减少，并要求伤害管线接受；同目标嵌套 hurtServer 的减少量从主观察值中扣除。免疫、无伤害、空挥与非主目标不提供合格结果；不新增扣血调用。

BladeCombatServer 使用同一服务端动作序号更新每玩家 BladeComboState，单次编号最多一层。阶段上限来自 BALANCE_PROFILE 对应 5、6、8、10、12、14、16、18、20 层候选。命中前先衰减，命中后才增加层数。

最后命中后前 64 tick 保持；65 tick 首次减一层，随后每 5 tick 减一层。空挥不刷新计时，按键会话停止与连击绑定分离，所以松键保层。不同 ItemStack 实例、器灵引用、主手槽位、世界、死亡或旁观会清空；断线删除整份服务端状态。同物品 ID 的另一把刀不继承。长期器灵好感不改动。

查询：`/absmod combat inspect @s`，输出 `comboStacks` 和 `comboBonusesApplied=false`。追踪：`/absmod combat probe @s true`，日志记录 `primaryHit`、`stacksBefore`、`stacksAfter`；查询不改变状态，日志开关不重置层数或动作预算。

## 尚未实现

连击的伤害和攻速增益、客户端层数 HUD 尚未接入。下一批在本刀旧层数快照上增加局部武器伤害贡献，及单一固定 ID 的临时攻速修饰；保留外部修饰器并补清理验证。当前不宣称已达成九阶满层 35.2/6.0 或 211.2 DPS。

## 本次验证

`./gradlew.bat --gradle-user-home .gradle-user-home -PgameTests build`：Fabric/NeoForge 构建与独立检查通过。Fabric 测试服务器实际运行，8 项模组测试加 1 项框架测试共 9 项通过；新增测试确认免疫/空操作不计命中，只有吸收损失也能确认。既有满伤、60 次主命中、护甲和普通伤害历史回归通过。

纯状态检查覆盖上限、重复和旧编号拒绝、65/70 tick 衰减边界、命中前先衰减、同绑定保层、换刀/引用与失去持物清理，并核对所有阶段上限与设计 JSON。

日志：`build/combo-state-validation-final.log`。测试夹具仍是受控模拟玩家，不代表完整真实玩家输入/网络集成；尚未启动客户端或 NeoForge 服务器。盾牌、嵌套反伤和多人兼容仍需进一步实际验证。不是最终平衡验收。

改动文件：common 的 combat/BladeComboState.java、BladeAttackScope.java、BladeAttackExecutor.java、BladeCombatServer.java、mixin/LivingDamageMixin.java、item/MeowBladeStage.java；CombatVerification；Fabric gametest/BladeCombatGameTests；两份 README 及 docs 进度说明。新导入的远端快照保留，当前工作树状态单独追加。

本批不需要贴图。没有提交或推送。
