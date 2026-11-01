# Cat material combinations

The pale blush source was edited with the built-in image_gen tool, then lightened after the user requested the vanilla pink axolotl palette. Backend model version is not exposed. The accepted source is source/pale_pink.png; the saturated draft is not shipped.

Prompt: "Substantially lighten the fur to vanilla pink axolotl, extremely pale creamy blush nearly ivory, soft salmon ears/nose/paws/tail tip; no saturated rose or magenta. Preserve exact cat UV positions, adult64x32 top half and baby32x32 lower left, crisp pixels and transparency; no axolotl gills."

Normal shading references the previous generated lilac atlas. tools/art/CatCoatBaker.cs bakes deterministic material layers:8 normal palettes plus blush pink,8 code-defined patterns,4 white masks,4 eye options,and colorpoint. This is AI-painted source art plus procedural material composition, not2304 separately AI-painted cats. Original UV and geometry remain vanilla-derived; licensing unchanged.

Run from repository root in PowerShell7: Add-Type -AssemblyName System.Drawing; Add-Type -Path tools/art/CatCoatBaker.cs -ReferencedAssemblies System.Drawing.Common,System.Drawing.Primitives,System.Runtime,System.Private.Windows.GdiPlus,System.Private.Windows.Core; [CatCoatBaker]::Bake((Get-Location).Path).

There are2304 unique adult PNG hashes. Baby textures include2232 distinct images: white bicolor masks conceal some spotted patterns at the smaller baby resolution, which separate visibly on adulthood. No count of unique baby silhouettes is claimed. Existing six legacy textures remain intact.

Screenshots are from actual26.2 and1.21.1 clients. Pink display fixtures explicitly force pink for visual inspection; natural rarity is checked separately by seeded breeding samples. No claim of waiting for a natural pink birth in live gameplay.
