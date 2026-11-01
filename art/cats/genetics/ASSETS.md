# Cat coat assets

Generated with the built-in image_gen tool on 2026-11-01. The tool does not expose a reliable backend model identifier; no specific model version is claimed.

Six separate UV-preserving palette-edit calls: chocolate, lilac, seal point, blue point, chocolate point, lilac point. Selected originals are retained in source/. Each is an AI-generated edit of a vanilla Siamese UV reference (Minecraft 26.2 adult and baby atlases), not an independently invented model or wholly original UV layout. Minecraft reference originals are local-only and excluded from distribution. Existing project licensing is unchanged; no new third-party license is asserted.

Prompt used: "Exact UV-preserving palette edit of this Minecraft CAT texture atlas. Keep EVERY rectangular UV island, eye pixel and transparent cell at exactly the same coordinates. Recolor to [coat palette]. Preserve 64x64 logical grid; top half adult64x32, lower left baby32x32, lower right blank. No island movement/new shapes/labels/margins/antialiasing. Pixel art texture only."

Palettes: warm dark chocolate with amber eyes; muted pinkish-gray lilac with amber eyes; ivory with dark seal-brown, blue-gray, chocolate-brown or soft lilac extremities and blue eyes. The lilac-point prompt explicitly specifies porcelain ivory, dove-gray/pinkish-taupe points, blue eyes and pink nose.

Packing: tools/art/pack_cat_textures.ps1 samples logical pixel centers and splits adult/baby rectangles; it preserves generated alpha. Adult textures are64x32;26.2 babies32x32. 1.21.1 uses the adult map for its scaled baby model. Generated unused pixels remain outside model UV areas; no denoising claims. Both real clients are checked before installation. See validation screenshots in 26.2/ and 1.21.1/.
