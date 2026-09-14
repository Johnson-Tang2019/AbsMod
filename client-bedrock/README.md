# Ab's Mod Bedrock 26.2 source adapter

This is an isolated **source module**, included by `module.gradle` in each loader build, not a standalone published mod or a binary-compatible port of SimpleBedrockModel. It depends on Gson and the real Minecraft 26.2 client API supplied by the consuming loader. There are no Fabric or NeoForge imports in this module. Only the loader's existing client entrypoint reaches the spirit renderer; server entities do not reference it.

The adapter supports a deliberately bounded Bedrock geometry profile:

- `format_version: "1.12.0"`, one entry in `minecraft:geometry`.
- `description.texture_width` / `texture_height`: independently configurable, 1–4096. No shared humanoid UV requirement.
- Named bones, absolute pivots, optional parent/rotation, cubes with origin/size, optional cube pivot/rotation/inflate.
- Box UV arrays with inherited/overridden mirror, or per-face `uv` / signed `uv_size` / 0,90,180,270 `uv_rotation`. Omitted faces are hidden. Per-face mirroring uses signed UV size.
- Maximum 256 bones, depth 32 and 512 cubes per bone; invalid hierarchy, dimensions, UVs and non-finite values fail loading.

The original test resources are `assets/absmod/models/entity/blade_spirit_test.geo.json` and `assets/absmod/textures/entity/blade_spirit/bedrock_test.png`. They can be opened as a Bedrock entity model in a compatible editor. Ten bones exercise parented head/ears, limbs and a two-part tail. This is a colored diagnostic model, not final character art.

The spirit animation binding requires `head`, `body`, `left_arm`, `right_arm`, `left_leg`, `right_leg`, `left_ear`, `right_ear`, `tail`; `tail_tip` is optional. Geometry can contain further bones. Animation resets the complete root before each render state, then adds walk, head pitch/yaw and tail movement. Owner tracking consumes the existing server-driven head rotation; no new owner selection or server AI is introduced.

On missing/malformed JSON, invalid rig or missing/corrupt/mismatched PNG, the renderer logs the reason and uses built-in placeholder geometry and the preserved old PNG. The old humanoid geometry is **fallback only**. F3+T recreates the renderer and retries current resources. No global mutable model cache is retained.

Not supported: legacy geometry versions, multiple geometry selection, locators, arbitrary material controllers, smooth skinning, Bedrock animation JSON/Molang, maid resource-pack manifests or JS, held-item positioning layers, translucent material splits, Gecko/YSM, custom Sodium/Embeddium backends. Unknown optional metadata is not interpreted. Do not claim compatibility with arbitrary maid packs.

Build from the repository root with JDK 25: `gradlew :neoforge:build` first, then `gradlew build`. Shared verification includes actual JSON/PNG loading, bad-resource recovery and interleaved animation states. CPU checks do not establish GPU or in-world acceptance. See [source provenance](UPSTREAM.md) and [batch evidence](../docs/IMPLEMENTATION_BEDROCK_RENDERER.md).

## Curved polygon subset (2026-09-14)

The active spirit is now `blade_spirit_ethereal.geo.json` / `ethereal_atlas.png`. `poly_mesh` accepts explicit indexed triangle/quad lists of [position, normal, UV], absolute positions, pixel or normalized UVs, and normal arrays. Limits per bone: 32768 positions/normals/UVs and 8192 polygons. Invalid indices, winding/normal disagreement and degenerate faces fail into the existing resource fallback. Triangle-list/quad-list shorthand strings are not implemented. The 26.2 ModelPart backend uses one averaged normal per face; this is not smooth per-vertex skinning. See the [Microsoft Bedrock 1.12 schema](https://github.com/MicrosoftDocs/minecraft-creator/blob/main/creator/Reference/Content/SchemasReference/Schemas/minecraftSchema_geometry_1.12.0.md). The schema marks poly_mesh experimental; this is a bounded Ab's Mod implementation, not universal maid-pack compatibility.
