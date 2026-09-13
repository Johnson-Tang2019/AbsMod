# Floral Blade Spirit model

Active appearance now uses the borrowed Chen model and paired texture. See BORROWED_MODEL_NOTICE.md. The exported OBJ and preview show this temporary model; the floral model and its generators below are preserved for later work.

The user's original reference is preserved byte-for-byte as `reference.png`. The runtime texture is an AI-assisted atlas adaptation, not a pixel-exact crop/repack. The existing sword textures and earlier placeholder remain intact.

- Runtime editable rig: `../../common/src/main/resources/assets/absmod/models/entity/blade_spirit.json` (`absmod_model_v1`).
- Texture: `../../common/src/main/resources/assets/absmod/textures/entity/blade_spirit/floral_atlas.png` (1254 square, opaque RGB).
- `blade_spirit.obj` / `blade_spirit.mtl`: neutral-pose interchange geometry with face UVs; no animation armature. OBJ groups retain bone names. Keep repository layout for the MTL texture path.
- `preview.html`: self-contained WebGL preview with rotation/zoom and depth-tested geometry. It is not a Minecraft screenshot. No network dependencies.
- Re-export after editing the runtime model: `python tools/art/export_spirit_model.py` from the repository root. This exports geometry and embeds the existing atlas; it does not edit image pixels.

The atlas is divided into 4 by 4 conceptual tiles using normalized UVs, so image dimensions need not be powers of two. The model samples cropped subsets for shared hair, individual stockings, ears and small colored ornaments. It does not map the generated checkerboard ornament backgrounds. Flowers and crystals are actual opaque geometric pieces; transparency and emissive layers are not implemented in this model.

Runtime bones preserve the humanoid head/body/arms/legs and add hair, ears, flowers, skirt and a continuous curved tail. Geometry uses Minecraft model coordinates (Y down, front -Z); OBJ exports Y up and front +Z. Rotations are local radians applied X, then Y, then Z. UV face order is DOWN, UP, WEST, NORTH, EAST, SOUTH. Optional `quads` contain four local vertices and four normalized UV pairs; these curved surfaces share the same bones and export path. Each legacy cube has box [x,y,z,width,height,depth], UV rectangles [u0,v0,u1,v1], and top/bottom X/Z taper factors.

The renderer loads the JSON through Minecraft's resource manager. Java supplies walking/look poses and subtle secondary motion. No external model or animation library is required. Both loader builds and non-GPU geometry checks passed; actual client lighting, walking intersections and multiplayer rendering await in-game acceptance.

## Curved revision

The current mesh has 1768 quads: a shaped jaw/crown, tapered curved hair, fitted torso, round sleeves/legs, bell skirt and swept tail. Rebuild with `python tools/art/refine_spirit_model.py`, then re-export. `box_model_v1.json` retains the earlier rig as the generator input and rollback source. Neither command edits texture pixels.

Touhou Little Maid's public model/renderer separation was reviewed; the model-file endpoint could not be retrieved (HTTP 403), so its detailed geometry was not copied or claimed as verified. This is an original curved-mesh revision compatible with the current shared renderer. See `docs/SPIRIT_CURVED_VALIDATION.json` for actual checks; Minecraft was not launched.

## Measured proportions, revision 3

Three actual models from the user-specified local TLM pack were read: Chen, Goutokuzi Mike and Hakurei Reimu. Their base head is 8 units high and the body below it is 18, excluding decorations. The spirit now follows a 26-unit base height, 8-unit head, 9-unit legs of width 3 and 8-unit arms. Feet remain at Y=24; head/body origins are Y=6 and hip origins Y=15. Hair and skirt were repositioned to suit the shorter body. Runtime verification checks the measured bounds and post-animation pivots.

Source paths, SHA-256 and measurement scope are recorded in `docs/SPIRIT_PROPORTION_REFERENCES.json`. Local source files were only read and remain unchanged. No external model or texture is redistributed. This resolves the earlier inability to retrieve model files remotely. The portrait proportions are an implementation revision, not an age or final gameplay-height decision.
