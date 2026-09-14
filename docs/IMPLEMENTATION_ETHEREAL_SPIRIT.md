# 粉白曲面剑灵修订

2026-09-14，ART-02；main / 8a035c5，初始工作树干净。用户要求重绘贴图并去掉原版方正人物轮廓。

已替换当前外观：原创21骨骼、2314个三角/四边面，圆润收下颌、分层收尖长发、立体猫耳、收腰衣裙、外扩裙摆、圆化袖腿与两段弯尾。用用户原图作为配色与风格参考，经 imagegen 重绘1254×1254独立图集，16格分别容纳脸、肤色、头发、衣裙、袖袜、鞋、毛发与饰品；没有改动参考原图或九阶武器图。

## 文件

- `common/src/main/resources/assets/absmod/models/entity/blade_spirit_ethereal.geo.json`：当前Bedrock模型。
- `common/src/main/resources/assets/absmod/textures/entity/blade_spirit/ethereal_atlas.png`：重绘图集；复制生成原文件，未另行修改像素。
- `client-bedrock/.../BedrockPolyMesh.java` 与 `BedrockGeometry.java`：有限索引多边面加载，接入原有骨骼和26.2提交路径。
- `SpiritModelResources.java`：使用新模型和PNG；旧占位仍为资源失败回退。
- `BedrockModelVerification.java`：真实曲面资源、法线/UV、索引错误、动画隔离和恢复检查。
- `tools/art/build_ethereal_spirit.py`、`spirit_preview.template.html`：可重现模型与预览，不编辑PNG。旧导出脚本改为生成 borrowed-preview.html，避免覆盖当前预览。
- `art/blade_spirit/preview.html`、`ethereal-preview.png`：新模型预览；旧OBJ仍是历史模型，不能视为当前模型。
- 双语README、STATUS、CURRENT_TASK、ART_GUIDE、SPIRIT_ART_REQUIREMENTS 与模块说明已同步。

## 验证

根 `build` 通过，包含Fabric和NeoForge构建与共享CPU检查，日志 `build/ethereal-final-build.log`。实际测试包括21骨骼曲面加载、有限顶点、单位法线、UV范围、A/B/A动画姿态隔离、非法索引拒绝，以及资源故障回退/恢复。旧方体测试资产也继续受原检查覆盖。

隔离浏览器检查正面、背面、侧面与斜面；预览开启背面剔除，修正模型曲面朝向与预览相机转换后的绕序。截图 `build/spirit-preview-{front,back,side,angle}.png`。`git diff --check`通过，仅有已有换行规范化警告。

本轮未启动Minecraft客户端或服务器，因此游戏内光照、走动穿插、多人同屏与实际F3+T仍待验收。服务端身份、AI、显现召回、伤害、好感、变身规则和现有刀光没有修改。未提交或推送。

## 格式与剩余限制

沿用Bedrock 1.12骨骼结构，新增其实验性 `poly_mesh` 字段的有限实现；[Microsoft schema](https://github.com/MicrosoftDocs/minecraft-creator/blob/main/creator/Reference/Content/SchemasReference/Schemas/minecraftSchema_geometry_1.12.0.md)描述位置、法线、UV与多边形索引。只支持显式索引三角/四边面，不声明所有女仆模型包兼容。当前后端按面使用法线，不是顶点平滑蒙皮；未接入外部动画脚本或半透明材质分层。

新贴图和模型已可测试，不需要额外提供图片。当前衣装细节和比例仍可根据实际游戏观感调整，不写为最终人设。
