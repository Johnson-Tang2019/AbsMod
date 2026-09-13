# Bedrock renderer source provenance

Selected source: [TouhouLittleMaid, 1.21 branch, commit 207647c85740b1d0971de6040d6dd57cea62528c](https://github.com/TartaricAcid/TouhouLittleMaid/tree/207647c85740b1d0971de6040d6dd57cea62528c). The downloaded Git archive's comment identifies this commit; `upstream-manifest.json` records archive and individual source hashes. Its `gradle.properties` declares Minecraft **1.21.1**, NeoForge **21.1.219**, mod **1.5.3-neoforge+mc1.21.1**. This is reference source, not a 26.2 binary dependency.

Read implementations:

- `touhoulittlemaid/client/model/bedrock/BedrockModel.java`: named bone map and animation dispatch, with optional JS wrappers. Ab's Mod uses its own state-driven animation and does not include those scripts.
- `touhoulittlemaid/client/renderer/entity/EntityMaidRenderer.java`: selects model, texture and animation, then delegates the body to MobRenderer. Maid layers, events, tasks and alternative renderers are not ported.
- The **embedded** `com.github.tartaricacid.simplebedrockmodel` implementation in the same pinned tree: `AbstractBedrockEntityModel`, `BedrockModelUtil`, `BedrockEntityModelSet`, `BedrockPart`, `BedrockCubeBox`, `BedrockCubePerFace`, `BedrockCube`, `FaceItem` and geometry POJOs. It loads JSON, converts absolute pivots to relative joints, builds parent links, traverses pose stacks and emits UV-mapped cube faces.

The selected tree's root `LICENSE` is MIT for code (copyright 2019–2024 tartaric_acid). Its mod metadata separately lists MIT / CC BY-NC-SA 4.0. The port adapts the embedded implementation's coordinate, vertex-order and UV conventions; its MIT notice is preserved in `src/main/resources/META-INF/licenses/tlm-bedrock-MIT.txt`, included in both artifacts. No maid art was added for this batch; the test model and PNG are original. Existing borrowed assets and their separate notice remain historical work, not the active model.

Do not confuse that embedded snapshot with the separate [MCModderAnchor/SimpleBedrockModel](https://github.com/MCModderAnchor/SimpleBedrockModel) project, which declares LGPL-3.0. This module does not import that project's binary or claim its license/version applies to the embedded snapshot. Ab's Mod's overall license is unchanged.

On 2026-09-13, the official [TLM file listing](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid/files/all), [SBM file listing](https://www.curseforge.com/minecraft/mc-mods/simple-bedrock-model/files/all) and both GitHub release pages yielded **no verified 26.2 build**. This is a bounded availability check, not a claim that no experimental branch exists. GitHub API calls were rate-limited; the source archive itself was retrieved successfully. No 1.21.1 dependency is installed or advertised as compatible.

26.2 API adaptation was checked against the locally resolved NeoForge 26.2.0.77 merged sources: `LivingEntityRenderer.submit` submits a model plus render state; `ModelFeatureRenderer.prepareModel` calls `setupAnim(state)` immediately before drawing; `Model.renderToBuffer` is final and draws its arbitrary ModelPart root. Therefore this adapter bakes the Bedrock hierarchy into that root, without inheriting HumanoidModel for the active appearance. `EntityRenderDispatcher.onResourceManagerReload` recreates registered renderers, so loading is repeated without a stale static cache.
