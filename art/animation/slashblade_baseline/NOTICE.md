# SlashBlade baseline provenance

Source: https://github.com/0999312/SlashBlade_Resharped/tree/6e2a0a092fb794d7ea56fd83452869674f3ab1c7

The selected commit is fixed; `manifest.json` records SHA-256 of the four original binary assets. Originals are retained under `upstream/`; the runtime derivative is `assets/absmod/animations/slashblade_baseline.json`. Ab's Mod's own license has not changed.

## License inspection

The selected repository root contains MIT, copyright (c) 2024 M Mysterious Mountain Forging-shop Group. The complete notice is preserved in `upstream/LICENSE` and shipped in both loader JARs as `META-INF/licenses/slashblade-baseline-MIT.txt`.

Inspected assets are `src/main/resources/assets/slashblade/combostate/motion.vmd`, `model/bladeholder.pmd`, `model/pa/player_motion.vmd` and `model/pa/alex.pmd`. No separate license or attribution override for these four files was found in the selected archive's license/readme files or asset directories. PMD comments identify BladeHolder as "for SlashBladeMod motion base" and Alex as "Alex"; these comments are descriptions, not independent grants. Use here relies on the repository's MIT grant for these selected project assets, not a claim that every third-party asset in the repository is MIT.

NyMmd files have a separate MIT notice, copyright (C) 2008-2012 nyatla, and ARTK_MMD/PY attribution. Its complete inspected header is preserved separately as `upstream/NyMmd-reference-license.txt`. No NyMmd Java library, playerAnimator implementation, model mesh, sheath, sword OBJ, texture, or other third-party model is included in the runtime. The PMDs serve only as offline skeleton references. Preserve notices when replacing or redistributing this preset.

## Read source and adaptation

The seven requested Java sources are preserved under `upstream/java/`, retaining their headers (trailing whitespace normalized), including Furia's original creation credits. Their original prefix is `src/main/java/mods/flammpfeil/slashblade/`:

- `client/renderer/model/BladeMotionManager.java`: VMD motion lookup.
- `client/renderer/model/BladeFirstPersonRender.java`: first-person render replacement.
- `client/renderer/layers/LayerMainBlade.java`: PMD hardpoint hierarchy and weapon transforms.
- `compat/playerAnim/PlayerAnimationOverrider.java`: player motion selection.
- `compat/playerAnim/VmdAnimation.java`: player bone coordinates and movement blending.
- `registry/ComboStateRegistry.java`: source frame ranges, transitions, excluded attack callbacks.
- `init/DefaultResources.java`: actual VMD/PMD resource paths.

`tools/animation/import_slashblade_baseline.py` independently decodes VMD bone keys and PMD parent/rest bones. It composes the entire hardpointA chain and samples at 60 Hz. NyMmd's method called `QuaternionSlerp` actually performs shortest-path normalized linear interpolation; the extractor follows that behavior and position lerp. It does not reinterpret a VMD extension as Bedrock JSON or claim general MMD/IK support. Both selected PMDs contain no IK chains.

The generated format is explicitly `absmod_motion_v1`. Runtime interpolation, per-player playback, 26.2 render-state mixins and grip calibration are Ab's Mod adaptations. Legacy playerAnimator is not installed or presented as 26.2-compatible. Player legs retain vanilla movement; the existing held-item layer follows the posed arm without drawing an additional weapon or sheath.

## Clip mapping

| Preset clip | Strike source frames | Recovery source frames |
| --- | --- | --- |
| combo_a1 | 1–10 | 10–41 |
| combo_a2 | 100–115 | 115–151 |
| combo_a3_first | 200–209 | 230–245 |
| combo_a3_second | 209–218 | 230–245 |
| combo_a4 | 519–532 | 576–608 |

A3's two cuts are two successive authorized Ab's Mod attacks, never two damage callbacks for one authorization. Its recovery skips the other hit. A4 crops the active interval of the original 500–576 spin, excluding its repeated damage, stun and player rotation/displacement callbacks. Recovery joins are blended. The strike budget follows the supplied attack interval; recovery retains source timing and can be interrupted immediately by the next authorization. This is a replaceable initial adaptation, not an exact reproduction of the complete upstream combat chain.
