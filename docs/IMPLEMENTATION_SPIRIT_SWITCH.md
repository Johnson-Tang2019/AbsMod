# 手动切换与虚化占位

验证完成：双加载器 build 通过（build/spirit-switch-final-build.log）；Fabric GameTest 全部11项通过（build/spirit-switch-gametest.log），包括普通右键不初始化、潜行显现/占位/交互召回、原剑数据、占槽/非主人/重复召回拒绝。NeoForge世界自动驱动切换与显示检查通过；真实鼠标网络、多玩家与Fabric客户端仍未实测。

## 2026-09-14：手动形态切换与虚化剑（SPIRIT-02）

CONFIRMED：潜行右键主手喵刀显现；潜行右键自己的剑灵召回；显现后在原槽位保留虚化剑。当前以潜行使用入口显式初始化新剑身份，已有身份缺档拒绝重新初始化。召回目标为所选主手槽位，仅接受空槽或对应身份的虚化剑，不覆盖其他物品。

SOURCE_PRESENT：共用SpiritTransformationService交接原剑，保留完整ItemStack和身份；九阶独立spectral占位物品不继承MeowBladeItem，无剑属性或连击能力。对应原剑纹理在渲染时施加40%不透明度淡粉着色，原始九张PNG不变。召回清除主人背包内同身份虚化占位并交还原剑。遗留在外部容器/掉落物中的占位仍无武器或发剑能力，也不计为实体剑副本。

保留护士酒狐模型与全部此前未提交修改。未实现自动战斗切换、渐变变身动画、跨区块远程召回或崩溃恢复闭环；不补写死亡/好感扣罚规则。本地基线78ff086；未提交或推送。详细验证见 IMPLEMENTATION_SPIRIT_SWITCH.md。

源码改动：SpiritInteraction、SpiritTransformationService、MeowBladeItem、SpectralBladeItem、BladeSpiritEntity、SoulCommands副本诊断；Fabric/NeoForge占位物品注册；客户端SpectralBladeTintMixin与九阶物品模型声明；双语提示、README、状态文档及SpiritGameTests。沿用并扩展可选SpiritVisualTest，普通构建不打包测试入口。

实际启动NeoForge测试世界，通过物品use和实体interact入口执行显现/召回（服务端自动驱动，未模拟真实鼠标网络输入）。日志build/spirit-switch-client.log记录spectral_meow_blade占位、196骨骼非回退显示及召回后19耐久损伤恢复；查看art/blade_spirit/switch-test/manifested.png和recalled.png。检查了手持半透明外观和物品栏状态；资源重载正常。Fabric客户端、多玩家高延迟、九阶逐一视觉及强制中断恢复未实测。

最初构建因26.2消息API签名错误失败，改用ServerPlayer.sendSystemMessage后通过。最初新增GameTest遇到假玩家无网络连接，消息提示现在跳过无连接玩家；交接逻辑不依赖提示消息。
