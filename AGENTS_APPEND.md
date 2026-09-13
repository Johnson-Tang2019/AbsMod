# Suggested AGENTS.md addition

This is a merge fragment, not a replacement for the repository's existing AGENTS.md.
Document package revision: `2026-09-10.repository-progress.1`.

## Project specifications and implementation evidence

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

## Reconcile the existing placeholder sentence

If the existing AGENTS says stage values have not been supplied, refine that statement rather than discarding its intent:

The code still contains placeholder stage values. Candidate PvE values are documented in docs/BALANCE.md and docs/BALANCE_PROFILE.json, but remain PROPOSED and are not yet gameplay-validated. Do not invent unresolved progression, heavy-hit, affection, or death-penalty parameters.
