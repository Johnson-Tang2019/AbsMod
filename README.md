# Ab's Mod

**Minecraft 1.21.1 NeoForge is the sole active development target.** The field guide has 76 pages. The optional FTB chapter has 111 quests. [Setup](docs/PORT_1211.md)

**0.12.0 · Moonlit Blossom Trial Garden.** Flowering boughs and warm lanterns frame the south gate. Follow the open cherry court to the heart mirror, with a crescent moon rising beside the northern pool. Two sculpted five-petal blossoms open toward the court. Stepping stones cross a pale pink brook, opposite three cherry trees. [Garden guide and in-game views](docs/CHERRY_ARENA.md)

**0.10.0 · Grand Court of Falling Blossoms.** The fighting radius grows from 15 to 34 blocks. Five tiers of seats overlook the blossom mosaic. Take the stair aisles to the north hall, the east drum stage or the west lookout. Existing arena worlds upgrade in place. [Arena guide](docs/CHERRY_ARENA.md)

**0.9.0 · Court of Falling Blossoms.** Build a four-wide, five-high cherry-log frame and use a fragrant cherry blossom on its sill. Inside the void arena, bosses fight on the floor, the structure is protected, and falling returns you to the south landing. The south gate takes you back. Guide pages 75–76 explain both journeys.

**0.8.0 · Cherry orchard and hot springs.** Gather fragrant blossoms from vanilla cherry trees and pick the hanging cherries once they turn pale pink. Make honey tea, cherry cordial, blossom dumplings or a cherry tart. Find a spring in a cherry grove or warm a shallow pool at home with a spring stone. Bathing grants Regeneration and Resistance. Soaking with your own manifested spirit for five seconds earns daily affection. See guide pages 69–74.

**0.7.0 · Blossom paths and a travelling lantern.** Equip a bow or staff to play either ranged profession. Each has nine blossom tiers and eight dark tiers with individual sprites. Carry the lantern in your offhand or on a Curios belt for moving light. Opening an existing world on a new release automatically backs up the entire world. Guide pages 62–68 cover recipes, controls and restoration. [Progression table](docs/BLOSSOM_PATHS.md) · [In-game weapon sheet](art/blossom_vocations/in-game-weapons.png)

**0.6.0 · Tea, cats and the Heart Mirror.** Mount a tray on a low cherry table, then use the Bond Bell to invite your spirit to a particular cup. Cats can lie in their own nests or cherry cushions. The bell remembers each cat’s last rest. Carry your bound normal VIII blade into the Heart Mirror trial, dodge the sweep and slam, then attack the open echo. At 250 affection, combine the winning blade with 16 amethyst blocks in the Resonance Workshop to reach IX. The eight dark blades each have their own sprite. See guide pages 56–61.

**0.5.0 · Cherry interiors.** Mount items or maps in an open pink display frame. Pink glowing glass and panes provide level 12 light. Switch the two-block floor lamp from either half, pair a table lamp with the 27-slot cabinet, or arrange tea tables and flower stands. Guide pages 52–55 list the recipes. [In-game room](art/cherry_interiors/room.png) · [Development plan (Chinese)](docs/CHERRY_HOME_ROADMAP.md)

**0.4.0 · Rose Gold and Blush Cookery.** Combine 2 gold ingots, 4 copper ingots and a Paw Crystal for 2 rose gold ingots. Craft the upgrade template with a diamond, a copper block, a Paw Crystal and pink dye. In a smithing table, use the template, diamond gear and one alloy ingot to upgrade five tools/weapons or four pieces of armor. The tools have intrinsic Silk Touch. Ingots repair the gear and compact into storage blocks. The same template gives Kaleidoscope Cookery 1.6.0 utensils a pale pink finish. Existing rose-patterned tools and armor independently add one effective level to each enchantment, even above its vanilla maximum. Guide pages 45–51 explain all three paths.

**0.3.0 adds three companion keepsakes.** Calibrate a compass with your bound stage 2+ blade at a cartography table to trace a nearby companion. Finish a game with your stage 3+ spirit while holding a rabbit foot offhand to earn a drop-gathering anklet. Give your spirit the first raw cod of the day in open rain with a bottle offhand to earn a rain-catching locket. Wear them in Curios or hold one offhand when playing without Curios. See guide pages 42–44 and the [item artwork](art/rose_workshop/blush-keepsakes.png).

Hold a physical Meow Blade in your main hand and an amethyst shard offhand. Use a smithing table while standing to engrave a Paw Crystal. Craft seven wearable charms with crystals, from quarry mittens to a slow-falling cat cape. Each workshop charm has a matching Curios slot. Without Curios, one charm works in your offhand. The smithing table also upgrades four iron tools, a full suit of iron armor and eight optional Farmer’s Delight kitchen implements. Put pink dye on the left, the original gear in the middle and a Paw Crystal on the right. Recipes are on guide pages 38–41.

Craft the **Cat Field Guide** with a book and raw cod. Use it in the air to read the guide or on a cat to see its genes, coat and lineage. Sneak-use on the first cat, then observe another nearby to see breeding odds. Offspring can have 2048 ordinary and 256 rare blush-pink coat combinations. Existing cats retain their saved coats. [Cat genetics and breeding odds](docs/CAT_GENETICS.md)

Crafting and progression are now playable prototypes on both loaders. Craft a normal blade from a diamond sword, four separate amethyst shards, raw cod and pink dye; craft a dark blade from a netherite sword, four separate crying obsidian and purple dye. Craft a **Resonance Heart** from four amethyst shards, a gold ingot and pink dye, then right-click it to upgrade the same blade. The active 1.21.1 version supports nine normal stages and eight dark stages.

The vanilla Advancements screen guides both routes, affection and resonance. Earn daily-capped affection through talking, raw-cod gifts, active following and combat. Dark heavy strike defaults to **G** (rebindable): 0.6s windup, 8s cooldown, 1.8× stage base damage, sharing the existing attack budget. Broken blades are retained for repair. Numerical values remain prototype candidates.

Use a Resonance Heart to open the pink workshop. Place the blade on the left and add materials on the right. Check the next-stage preview and affection requirement, then select **Resonate**. Take the upgraded blade and reopen the menu for another stage. After a disconnect, rejoin to recover any interrupted resonance. If your inventory was full, make room and reopen the Heart for the remaining items. Dark regular attacks draw a compact violet arc. A dark heavy strike plays a steep downstroke. On a hit, a violet ring and action-bar message mark the result.

Hover over the required materials to see how many you have inserted and how many are missing. The combat status line above the hotbar shows ordinary combo stacks and their decay, or dark heavy strike preparation and cooldown. Set **Toggle Meow Blade combat status** in Controls to hide or show it during the session.

Two new AI-generated textures are included: Resonance Heart and the original dark-blade prototype sprite. The original nine sword textures are unchanged. See [implementation and validation limits](docs/IMPLEMENTATION_PROGRESSION.md), [asset provenance](art/progression/ASSETS.md), and [actual advancement UI](art/progression/advancements.png).


A short pose transition now smooths consecutive blade actions. Two real NeoForge clients were connected locally and 54 observer screenshots were captured; see [validation](docs/MOTION_MULTIPLAYER_VALIDATION.md). The [growth and recipe design](docs/MEOW_BLADE_PROGRESSION_PROPOSAL.md) remains the numerical prototype reference; the latest implementation is described above.

Custom first-person rendering now yields when its motion preset is unavailable. Late local animation confirmations are ignored while a screen is open or mining is active; remote players remain independent.

The replaceable `slashblade_baseline` preset now drives real first-person weapon transforms and third-person torso/arm poses on 26.2, using imported SlashBlade VMD samples without its damage callbacks. Original nine-stage art and server combat are preserved. Both loader builds and 12 Fabric GameTests passed. Actual NeoForge screenshot sequences are available; full multiplayer/input/pose acceptance remains incomplete. See [implementation and limits](docs/IMPLEMENTATION_SLASHBLADE_BASELINE.md), [game screenshots](art/animation/slashblade_baseline/game/preview.html), and [source/licenses](art/animation/slashblade_baseline/NOTICE.md).

Blade spirits are immune to damage. Attacking with the spectral blade automatically recalls the matching loaded spirit before the existing attack scheduler runs; holding attack continues across the replacement. Air swings also recall; block mining does not. Stopping attack does not automatically manifest the spirit.

Sneak-use a main-hand Meow Blade to manifest its spirit; its slot retains a translucent, inert spectral blade. Sneak-use your own spirit while holding that link (or with an empty selected slot) to restore the original sword, including its identity, damage and components. No operator commands are needed. Other held items are never overwritten.

A loaded spirit catches up at safe spots. When you change dimensions, wait on safe ground for it to follow. If separated, use the spectral blade in your main hand to see its last position. Rejoining checks interrupted handoffs and returns the original sword when delivery is clear. If the handoff slot is occupied, free it and rejoin or use the matching link again. [Recovery behavior and verification](docs/IMPLEMENTATION_SPIRIT_RECOVERY.md)

The default blade spirit now uses the user-supplied **Nurse Wine Fox** model and original texture (196 bones), rendered through the existing Bedrock adapter. Both loader builds pass; NeoForge in-world views and resource reload were checked with no fallback. Basic spirit animation is bound; the original Gecko/Molang animation pack is not supported. See [integration evidence](docs/IMPLEMENTATION_NURSE_WINE_FOX.md), [game screenshots](art/blade_spirit/preview.html), and [asset provenance](art/blade_spirit/nurse-wine-fox/NOTICE.md).

English | [简体中文](README.zh-CN.md)

Base package: `com.abyssredemption.absmod`. Mod ID: `absmod`.

Sneak-use the Field Guide in the air to open your cat collection and family trees. Observe cats with the guide and filter by coat, pattern, or eyes. Use a Bond Bell on your cat and then on a placed Moonfelt Cat Nest to assign a rest spot. Use the bell in the air for companion details, or sneak-use to call a nearby cat. Recipes and controls are on guide pages 25–28. In 1.21.1 and the archived 26.1.2 build, select your manifested spirit from the Bell menu to see affection, durability, the next stage requirement and today’s activity. See page 29 for the controls. Page 30 explains the Gentle Brush, an optional reusable tool for your cat or manifested spirit. Brushing a spirit shares the existing daily talk reward. See [cat collection and home setup](docs/CAT_GENETICS.md).

In 1.21.1 and the archived 26.1.2 build, the spirit’s Bell menu offers 12 craftable accessories and three games against the spirit: tic-tac-toe, Gomoku, and Reversi. The accessory selection follows the same sword after recall. Any completed round counts toward that game’s FTB quest. Field guide pages 31–35 cover every recipe and the rules of each game.

The Milkshake Tray displays up to four drinks for anyone to take. Right-click a coaster with a milkshake in your main hand to place one, then use an empty main hand to pick it up. Breaking the tray drops any drinks left on it. The recipe and controls are on pages 36–37 of the field guide.

## Build and run

Install JDK 25 for Gradle and JDK 21 for the game. Import the repository root in your IDE. The root build produces `1.21.1NeoForge/build/libs/absmod-neoforge-0.3.0+1.21.1.jar`.

```powershell
./gradlew.bat build
./gradlew.bat runClient
```

The active gameplay sources are in `common/src/minecraft1211/java`, with NeoForge registration and version-specific resources in `1.21.1NeoForge`. The shared assets remain in `common/src/main/resources`. To load the optional kitchen, accessory slots and FTB tasks in the development world, see [1.21.1 setup](docs/PORT_1211.md). The historical 26.2 loader projects can be built explicitly with `-ParchivedTargets`.

## Meow Blade

- Nine independent stages appear in stage order in the Combat creative tab.
- Stage 1 retains `absmod:meow_blade`. Stages 2–9 use `absmod:meow_blade_stage_2` through `absmod:meow_blade_stage_9`.
- Independent damage values are defined in `common/src/minecraft1211/java/com/abyssredemption/absmod/item/MeowBladeStage.java`.
  They represent totals for a player with vanilla base attributes. The PROPOSED PvE profile now supplies
  stage damage **7/8/9/10/12/14/16/18/22** and total speed **1.6/2/2.2/2.5/2.8/3.2/3.6/4/5**.
  Successful hits build a normal blade combo that increases damage and attack speed. These values are balance prototypes.
- Normal blades have stage-specific durability, use diamonds for repairs and belong to `minecraft:swords`.
- Obtain items from the Combat tab or use `/give @s absmod:meow_blade` or `/give @s absmod:meow_blade_stage_9`.
- In 1.21.1 survival, the Heart Mirror trial unlocks the last normal resonance for the blade that wins it.

Each stage has its own client item definition, model JSON and supplied texture.
The supplied `meowsword1.png` through `meowsword9.png` map to stages 1–9 respectively.
They are stored in `common/src/main/resources/assets/absmod/textures/item/` as `meow_blade.png`
and `meow_blade_stage_2.png` through `meow_blade_stage_9.png`.
All nine 1254 × 1254 RGBA PNGs are copied byte-for-byte, preserving transparency and artwork.
Matching models reference `absmod:item/<item_id>`. Both loaders share the same textures.
In-game appearance, atlas mipmapping and performance at this resolution still need client verification.

## Milkshakes

Ported from the author's [MilkShakeMod](https://github.com/Johnson-Tang2019/MilkShakeMod/tree/b9a1ac762b18c29973301048f6984987a4601fe2) (`master`, commit `b9a1ac7`).
All four implemented drinks and their original 16 × 16 RGBA textures are included in both loaders.
The unused green texture in the source repository has no registered item or recipe and is not included as a new drink.

| Item ID | Display name | Effects |
| --- | --- | --- |
| `absmod:milkshake` | Milkshake | Health Boost II, 120 seconds |
| `absmod:chorus_milkshake` | Chorus Fruit Milkshake | Health Boost II, 120 seconds; random teleport |
| `absmod:gold_milkshake` | Gold Milkshake | Health Boost II and Absorption IV, 120 seconds; Fire Resistance II and Resistance II, 300 seconds; Regeneration II, 20 seconds |
| `absmod:pink_milkshake` | The Best Milkshake | Health Boost II, 120 seconds; Speed IV, 300 seconds |

All drinks restore 6 hunger points, use saturation modifier 1.0 (12 saturation points), can be consumed
at full hunger, take 3 seconds to drink, use the honey drinking sound, stack to 16 and have Uncommon rarity.
Crafting with a milkshake returns a bucket, matching the original `craftRemainder` behavior.
Drinking does not add a new empty-container return or clear status effects.
Chorus milkshakes retain the original 15 random teleport attempts within roughly 8 blocks of the start,
with portal particles and success/failure sounds, performed only on the server.

All six recipes are shapeless:

| Ingredients | Result |
| --- | --- |
| Milk bucket + sugar | 2 milkshakes |
| Milk bucket + sugar + chorus fruit | 2 chorus fruit milkshakes |
| Milkshake + chorus fruit | 1 chorus fruit milkshake |
| Milk bucket + sugar + gold block | 2 gold milkshakes |
| Milkshake + gold block | 1 gold milkshake |
| Milkshake + cherry sapling | 1 best milkshake |

Obtaining a milk bucket unlocks all six recipes. Invalid legacy recipe conditions, the inventory trigger
and incomplete recipe reward references were replaced with valid 26.2 data.
The new namespace is `absmod`; this does not automatically convert existing `armilkshake` items in old saves.
Find drinks in the Food & Drinks creative tab or run `/absmod milkshakes @s` for one of each.
Use `/give @s absmod:gold_milkshake 16` to test a full stack of one variant.
Verify drinking, effects, teleportation, crafting remainders and recipe-book unlocks in-game on both loaders.

## Commands

All commands require the same game-master permission as vanilla `/give` (normally operator level 2).

| Command | Purpose |
| --- | --- |
| `/absmod` or `/absmod help` | Show command usage |
| `/absmod stages` | List all nine stages, IDs and base damage/attack speed, with a placeholder notice |
| `/absmod give <targets> <stage>` | Give each target one blade of the specified stage |
| `/absmod kit <targets>` | Give each target all nine stages, one blade per stage |
| `/absmod milkshakes <targets>` | Give each target one of each milkshake |
| `/absmod inspect <targets>` | Report each target's main-hand blade stage and remaining/maximum durability |

```mcfunction
/absmod give @s 1
/absmod give @a 9
/absmod give PlayerName 4
/absmod kit @s
/absmod stages
/absmod inspect @s
```

Syntax: `/absmod give <targets> <stage>`.
The stage must be an integer from 1 through 9; each selected online player receives one blade.
The command also works from the server console when targeting a player name or `@a`.
An inventory overflow drops the blade at the target player's location for that player to pick up.
This command grants a new item; it does not switch the stage of an existing item.
Command behavior is shared by Fabric and NeoForge; feedback is translated into English and Chinese.

## Development conventions

Use meaningful English technical names, never pinyin. Comments and docstrings are in English.
Keep this README and `README.zh-CN.md` in sync. See [CONTRIBUTING.md](CONTRIBUTING.md) and [AGENTS.md](AGENTS.md).

## Meow Blade slash effects

All nine stages emit pink-white curved light ribbons when the held blade swings, including air swings.
The client observes vanilla swing animations for the local player and other tracked players, so no extra
damage, entities or networking packets are introduced. Block-breaking swings also produce the visual.
The actual swinging hand determines the effect; left-handed players and off-hand swings are mirrored.
Each blade swing immediately advances through six visual motions: opposing diagonals, a horizontal sweep,
an upward sweep, a steep diagonal and a reverse sweep. Each player has an independent sequence, with
no added delay or idle timeout reset. This changes the slash effect, not the vanilla arm animation;
clients joining or starting to track a player mid-sequence may begin at a different visual step.

The original procedural mesh combines a pearl-white edge, translucent pink veils, flowing filaments and
drifting light motes. A cherry-blossom pink alpha-blended body preserves the hue on bright backgrounds,
with a separate additive glow pass. Brightness holds through the first half of the effect before fading.
Both passes use depth testing, no depth writes and no face culling.
The enlarged sweep has 12 ribbon layers, an expanding halo and 32 rotating petal-shaped light motes.
Each slash lasts 18 game ticks (0.9 seconds at 20 TPS); up to 32 effects within 32 blocks are retained.
The larger visual radius does not change attack reach or damage.
Effects clear on disconnect/world changes and freeze when the game pauses. Higher stages have a small
visual size increase. This is an interpretation of the concept image, not a pixel-identical reproduction.
No SlashBlade OBJ/PNG assets or generated concept backgrounds are packaged.

Use `/absmod kit @s`, select a blade and swing to preview. Check stages 1 and 9 in first/third person,
left/right main arm, repeated swings, looking up/down, wall occlusion, pause/resume and world changes.
In multiplayer, check that a second client sees one effect per observed swing. Compare diamond swords
and empty hands (no effect). Automated geometry/swing checks run with `:fabric:verifyCommands`;
they do not verify GPU output or replace in-game visual acceptance on both loaders.

## In-game validation

Run both loaders and check loading, creative items, English/Chinese names and held models.
Compare damage, cooldown and attacks with a vanilla diamond sword; check enchanting and diamond repairs.
For commands, check stages 1 and 9, rejection of 0 and 10, operator/non-operator permissions,
multiple targets, console use and a full inventory. A successful build does not replace in-game testing.

`build` also runs `:fabric:verifyCommands` against the shared command logic to check valid syntax, invalid stage values,
missing arguments, permission restrictions and read-only command execution without a game server.
The verification also checks the migrated milkshake effect durations, amplifiers and counts.
Both loader modules are compiled; NeoForge runtime behavior, inventory delivery and inspection still require in-game verification.

The nine-stage builds and packaged shared resources were verified on 2026-09-08 using JDK 25 / Gradle 9.5.1.
The sword configuration was checked against vanilla Minecraft 26.2. In-game testing has not been performed.

## Build references

- [NeoForge 26.2 MDK](https://github.com/NeoForgeMDKs/MDK-26.2-ModDevGradle)
- [Fabric 26.2 example](https://github.com/FabricMC/fabric-example-mod/tree/26.2)

Dependency versions are pinned in the root `gradle.properties` and `build.gradle`.

## Development history

The following entries record early prototypes and checks. See the sections above and [current status](docs/STATUS.md) for current behavior.

## Blade Soul persistence prototype

Operators can test the blade held in a player's main hand:

```text
/absmod soul init <target>
/absmod soul inspect <target>
/absmod soul affection set <target> <value>
/absmod soul affection add <target> <value>
```

Initialization is explicit and records the target as owner for this test. It does not define automatic binding gameplay. Each blade carries only its soul UUID; the server archive stores owner, route, stage, optional affection, form, schema and revision. Affection starts unset; finite test values are accepted without inventing a final default or range. Queries never initialize blades. Missing or unreadable archives are rejected rather than recreated.

Duplicate diagnostics cover online inventories, ender chests and loaded dropped items. They block these commands but do not scan offline players, unloaded/nested containers or enforce a global combat lock. Stage migration currently produces a plan only, preserving identity and affection while retaining target item defaults; no upgrade is applied. See [current implementation and runtime checklist](docs/IMPLEMENTATION_BATCH_1.md).

The pink slash now has a wider translucent veil and outer halo, with lower outer opacity and the bright core retained. In-game visual acceptance remains pending.

## Continuous Meow Blade attacks

Hold the configured Attack key with a Meow Blade in your main hand to attack along your current aim. The server controls the cadence using effective attack speed; clicks and holds share one budget. Release, screens, focus loss, block targeting/mining, item use, death and weapon/world changes stop the session. Release and press again after an interruption.

Authorized primary attacks use full attack strength and a local hurt-window bypass, retaining the normal melee damage pipeline. Other weapons, secondary damage and vanilla cooldown history keep their usual behavior. Server confirmations trigger the existing pink slash motions with attack-ID deduplication. The vanilla charge indicator is hidden only while holding a Meow Blade with this pipeline available.

`/absmod combat probe @s true` enables tracing, `false` disables tracing, and `/absmod combat inspect @s` displays session state. These commands do not toggle automatic damage or reset attack recovery. Log counts represent authorized actions, not successful hits.

Both loader builds and standalone logic checks pass. In-game damage, physical input, shield/armor interactions and multiplayer visuals remain unverified. See [implementation and acceptance checklist](docs/IMPLEMENTATION_AUTOMATIC_DAMAGE.md).

## Creative inventory

Open the **Ab's Mod** creative tab (Meow Blade icon) to find all nine blade stages in order, followed by the four milkshakes. Items also remain in their vanilla Combat and Food & Drinks categories.

## Runtime combat tests

Run `./gradlew.bat --gradle-user-home .gradle-user-home -ParchivedTargets -PgameTests :fabric:runGameTest` with JDK 25. The opt-in `-PgameTests build` also runs these tests alongside both loader builds. The separate test mod is excluded from release JARs.

Eight AbsMod server-world tests cover repeated full-strength damage, 60 actual hits over 200 game ticks, vanilla attack charge, armor, immunity, ordinary damage cooldown history, primary hit confirmation (including absorption) and creative-tab registration/icon. These controlled fixtures use mock players with explicit attributes; they do not verify physical input, targeting/networking, shields, multiplayer visuals, NeoForge runtime or final balance. See [runtime validation](docs/RUNTIME_VALIDATION.md).

## Combo state prototype (BAL-02)

Authorized primary attacks that actually remove health or absorption now gain one server-side combo stack. Caps follow the PROPOSED balance table (5–20 stacks). Misses and immune hits do not refresh the timer. Release keeps stacks; decay begins 65 game ticks after the last valid hit and removes one stack every 5 ticks. Replacing the weapon, changing its soul reference/slot/world, death or disconnect clears this temporary state.

Use `/absmod combat inspect @s` to read `comboStacks`; `/absmod combat probe @s true` logs the before/after count and confirmed primary-hit result. **Damage and attack-speed bonuses are not applied yet.** This slice establishes hit confirmation and state lifecycle before connecting attribute bonuses. No new textures are required. See [implementation record](docs/IMPLEMENTATION_COMBO_STATE.md).

## Catgirl blade spirit prototype

The first manual spirit prototype has a pink-white placeholder skin, cat ears, a gently moving tail, humanoid walking and owner following. It retains the full original sword while manifested. With a Meow Blade in the main hand, an operator can run:

```text
/absmod soul init @s
/absmod spirit manifest @s
/absmod spirit recall @s @e[type=absmod:blade_spirit,sort=nearest,limit=1] 7
```

Slot 7 is the eighth hotbar slot and must be empty; allowed slots are 0–35. Use the returned entity UUID instead of the selector when testing multiple spirits. No automatic recall, progression, affection gain or death penalties are implemented. Use a test world: interrupted-save recovery and global duplicate detection remain incomplete. Health, movement and following distances are test values; the prototype is invulnerable.

A ninth AbsMod server-world test covers manifestation/recall, sword preservation and rejected operations; all ten required tests (including one framework test) passed. Both loader builds and SavedData file reload checks passed; no client or NeoForge server was launched. Rendering and actual following still require in-game acceptance. See [implementation](docs/IMPLEMENTATION_SPIRIT_PROTOTYPE.md) and [texture/model requirements](docs/SPIRIT_ART_REQUIREMENTS.md).

## Floral spirit model

The normal-route spirit now uses a model adapted from the supplied pink-white catgirl artwork: layered long hair, pointed ears, flower/crystal ornaments, a flared skirt and a curved five-part tail. The 33-piece model uses a newly arranged texture atlas with per-face UVs. Original artwork and the earlier placeholder are preserved; the atlas is AI-assisted artwork adaptation rather than pixel-exact repacking.

[Rotate the model](art/blade_spirit/preview.html) or inspect the [editable geometry and OBJ export](art/blade_spirit/README.md). Both loader builds, geometry/UV/animation-reset checks and browser visual inspection passed. Minecraft was not launched for this art change; lighting and motion intersections still need client acceptance. See [validation](docs/SPIRIT_MODEL_VALIDATION.json).

## Softer spirit silhouette

The latest revision replaces the box-shaped head, hair and skirt with a shaped jaw, curved tapered locks, fitted waist, round sleeves/legs, a bell-shaped skirt and a continuous curved tail (1768 quads). The existing atlas is unchanged. Both loader builds and geometry/UV/animation checks pass; front/back/angled browser views were inspected. Minecraft lighting and movement still await in-game acceptance. [Validation](docs/SPIRIT_CURVED_VALIDATION.json).

## Proportions measured from installed maid models

The latest model uses the actual Chen, Goutokuzi Mike and Hakurei Reimu rigs in the supplied local pack as dimensional references: base height 26, head height 8 (3.25 heads), leg length 9, leg width 3 and arm length 8. Hair, skirt and pivots were adjusted together. The original art and atlas are unchanged. Both builds, geometry/proportion/animation checks and browser views passed; Minecraft was not launched. [Measurements and evidence](docs/SPIRIT_PROPORTION_REFERENCES.json).

Side-view refinement adds head/torso depth, anchors flowers at the temples and flattens the frontal face region to prevent eyes wrapping onto the profile. Both builds and model checks passed (`build/spirit-side-validation.log`); this remains a draft model without Minecraft visual acceptance.

## Temporary existing maid model

The active spirit now uses Chen from the user-supplied Touhou Little Maid pack with its paired texture. Original AbsMod art is preserved. Geometry rotation conventions are adapted; the blink overlay is disabled until expression animation is implemented. Existing humanoid walking/look and manifestation/recall remain. See [third-party provenance](art/blade_spirit/BORROWED_MODEL_NOTICE.md). Both loader builds and model checks pass; Minecraft client acceptance remains pending.
