# Development rules

- Use meaningful English names for all technical identifiers and files. Do not use pinyin names.
- Write code comments and docstrings in English; preserve localized user-facing text.
- Keep `README.md` (English) and `README.zh-CN.md` (Simplified Chinese) in sync.
- Implement shared behavior in `common`; register platform events in the Fabric and NeoForge modules.
- Validate both loader builds and follow `CONTRIBUTING.md` before pushing.
- Stage values may use the PROPOSED PvE table in docs/BALANCE.md for clearly labeled prototypes; they are not final or gameplay-validated. The nine stage textures have been supplied and integrated; preserve the original artwork. Do not invent progression or stage-switching mechanics.
- Before implementation, read `docs/STATUS.md` and all relevant specifications completely.
- After implementation, update `docs/STATUS.md` with actual code and validation evidence; distinguish automated checks from in-game acceptance.
- Synchronize new confirmed settings into the relevant documents; preserve CONFIRMED, PROPOSED, DRAFT and TBD distinctions. Never finalize unspecified parameters.

## Specification workflow

- Read `docs/README.md`, `docs/STATUS.md`, and `docs/CURRENT_TASK.md` before planning changes.
- Read the relevant detailed specification before implementing gameplay, art, entities, progression, or combat.
- `docs/CONTINUOUS_COMBAT.md` is the canonical hold-to-attack specification. `docs/COMBAT_INPUT.md` is a legacy pointer only.
- Latest explicit user decisions describe desired behavior. Repository source describes implemented behavior. Actual test results describe validation. Do not substitute one for another.
- Treat CONFIRMED as user decisions, PROPOSED/DRAFT as proposals, and TBD as unresolved. Numerical candidates may be used for clearly labeled prototypes; do not call them final balance.
- The current repository uses ModDevGradle and Fabric Loom. Do not migrate build tools to match older chat suggestions.
- Preserve item IDs, existing milkshakes, original textures, and existing slash effects unless the user explicitly authorizes a change.
- Visual slash sequences are not damage combos. Do not double-trigger vanilla swing observers and custom attack acknowledgments.
- Before updating STATUS, inspect the actual local branch and uncommitted work. Do not overwrite newer local progress with a remote-only snapshot.
- After a task, record the changed files, task IDs, checks actually executed, remaining work, and whether Minecraft was launched. Never mark gameplay verified because a formula or geometry test passed.
- Keep existing English identifiers/comments and synchronized English/Chinese READMEs. Do not change licensing automatically.
- Do not commit or push unless authorized for the current task.

## Confirmed delivery workflow (2026-11-01)

- After each completed feature, update the FTB quest pack with prerequisites, concrete steps, controls, materials and remaining limitations. Keep quest IDs stable.
- For 26.2, retain the FTB export and use vanilla advancements plus the in-game guide until a compatible official FTB Quests build is verified. Do not install an incompatible dependency or silently substitute another quest mod.
- After verification, back up the existing Ab's Mod JAR and install the NeoForge build into `D:/Program Files/PCL2/.minecraft/versions/26.2-NeoForge_26.2.0.87_miao/mods`; preserve unrelated mods and saves. Verify copied hashes.
- The user has authorized automatic commits and pushes after completed features. Report failed pushes honestly.
- After the current cat genetics and guidance work, implement a separate Minecraft 1.21.1 NeoForge target and validate its FTB quest integration. Do not replace or downgrade the 26.2 target.
- Install the validated 1.21.1 NeoForge artifact and its tested FTB chapters into `D:/Program Files/PCL2/.minecraft/versions/1.21.1-NeoForge_21.1.265_miao`; preserve existing quest progress and unrelated files.

- The 26.2 instance was found renamed with `_miao` on 2026-11-01; verified existing Ab's Mod JAR and version metadata. Use Simplified Chinese in local gameplay/visual acceptance, retaining English translation resources.
