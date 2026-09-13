# 喵娘剑灵手动闭环原型

日期：2026-09-12。任务 ID：SPIRIT-01、SPIRIT-02（部分）。根据最新用户决定继续剑灵开发，允许占位贴图，参考车万女仆的渲染分层方式。此前 BAL-02 仅完成状态切片，属性加成仍待实现。

## 已实现

- 共用 `PathfinderMob` 实体，Fabric/NeoForge 分别注册实体、属性和客户端渲染器。
- 主人与灵魂 UUID 随实体保存；活动状态必须同时匹配服务端档案的形态、主人和实体 UUID。
- 每 10 tick 检查跟随，距离超过 3 格尝试寻路，小于 2 格停止；24 格外、主人离线/死亡/旁观或档案无效时停止。无跨维度跟随、传送追赶或区块票据。跟随距离、20 生命、0.25 移速为测试参数，不是最终数值。原型设置无敌以避免未定义的死亡成本介入；管理员移除/虚空等特殊情况仍须后续恢复处理。
- 粉白原创占位皮肤、人形行走与转头、猫耳、轻摆尾巴。现有刀光与原图保留。
- 管理员初始化主手喵刀后，可手动显现并召回到明确指定的空槽。绑定不会自动创建，阶段和好感不改变。
- Schema 1 剑态继续可读；显现期间使用 Schema 2，保存完整 ItemStack 副本、entityId、operationId、源槽和 revision。召回后恢复无需 carrier 的 Schema 1 剑态；不是全库自动迁移。
- SavedData 编解码保留传入 RegistryOps，避免物品组件丢失；单条非法记录仍拒绝整个档案，不采用部分解码结果。
- 先记录交接态，再移除剑/加入实体；召回先记录 recalling，再写入空槽并切回剑态、移除实体。重复召回、非主人、已占槽、受阻生成点及已发现的重复身份会拒绝。

## 测试方式

拿一把喵刀在主手，管理员依次执行：

```text
/absmod soul init @s
/absmod spirit manifest @s
/absmod spirit recall @s @e[type=absmod:blade_spirit,sort=nearest,limit=1] 7
```

`7` 是从零起算的第八个快捷栏槽，必须为空。可以将选择器换成显现时返回的实体 UUID；多只剑灵同时测试时应使用 UUID。允许目标槽 0～35，0～8 是快捷栏。

## 本批实际检查

- JDK 25、Gradle 9.5.1 双加载器构建与共享验证。
- Fabric 真实服务器 GameTest：完整剑组件/名称/耐久往返；档案 Schema 2 编解码；占槽、非主人、重复召回、受阻显现拒绝；保存快照不向调用者泄露可变引用。
- 独立 SavedData 文件落盘/重读：同时保存 Schema 1 剑与 Schema 2 实体载体，核对实体 UUID 与完整剑副本；拒绝载体中的错配灵魂。
- 命令解析、管理员边界、0～35 槽位边界。
- 详细执行结果见 `SPIRIT_VALIDATION.json`。已运行 Fabric GameTest 服务器；未启动客户端或 NeoForge 服务器。未验收实际寻路、实体区块重载后的跟随、多人渲染或正式美术。

## 未完成与边界

SPIRIT-01/02 是手动测试闭环，不是完整器灵系统。交接记录是服务器内顺序约束，**不是跨玩家/实体/SavedData 文件的磁盘原子事务**。强制断电、旧档回滚、丢失实体、交接中断的自动恢复/交付尚未实现；遇到待恢复记录不会自动重建第二把剑。当前应在测试世界使用。

重复扫描只覆盖在线玩家背包/末影箱与已加载掉落物，不扫描离线玩家、未加载或嵌套容器。卸载不触发死亡回剑；死亡成本、好感增长、阶段升级、自动战斗化剑、跨维度跟随、正式变形动画均未实现或仍 TBD。没有添加战斗目标、攻击、繁殖、交易或拾取装备行为。

## 变更文件

新增共享 `entity/BladeSpiritEntity.java`、`entity/SpiritEntityType.java`、`client/spirit/BladeSpiritModel.java`、`client/spirit/BladeSpiritRenderer.java`、`spirit/SpiritCarrier.java`、`spirit/SpiritTransformationService.java`、`command/SpiritCommands.java` 和占位 PNG。调整 `BladeSoulRecord`、`BladeSoulStore`、`SoulCommands`、`ModCommands`。接入两加载器入口和客户端入口。新增 Fabric `SpiritGameTests`，扩展共享 `SoulVerification`/`CommandVerification`，同步语言、双语 README、STATUS、CURRENT_TASK、SPIRIT_SYSTEM、ART_GUIDE 与美术需求。

未提交或推送。下一批应先补中断恢复、实体加载/存档一致性与客户端视觉验收，再决定自动化与死亡规则。
