# Ab's Mod

English | [简体中文](README.zh-CN.md)

Base package: `com.abyssredemption.absmod`. Mod ID: `absmod`.

## Project layout

| Directory | Purpose |
| --- | --- |
| `common/src/main/java` | Shared Minecraft 26.2 item behavior, commands and constants |
| `common/src/main/resources` | Shared translations, models and item tags |
| `26.2NeoForge` | NeoForge entry point, registration and metadata |
| `26.2Fabric` | Fabric entry point, registration and metadata |

The root project builds separate JARs for both loaders without an Architectury runtime dependency.
Only Minecraft Java 26.2 is currently implemented. Add separate subprojects for other game versions
and select compatible shared code and resources; do not assume binary compatibility across versions.

## Build and run

Install JDK 25, import the root `settings.gradle` in your IDE, and select JDK 25 as the Gradle JVM.
The first build downloads Gradle, Minecraft and loader dependencies.

```powershell
./gradlew.bat build
./gradlew.bat :neoforge:runClient
./gradlew.bat :fabric:runClient
```

On Linux or macOS, use `./gradlew` instead of `./gradlew.bat`.
Build separately with `:neoforge:build` or `:fabric:build`.
Outputs are in `26.2NeoForge/build/libs` and `26.2Fabric/build/libs`.
Install the JAR matching your loader, excluding `-sources` JARs. Fabric requires Fabric API.

## Meow Blade

- Nine independent stages appear in stage order in the Combat creative tab.
- Stage 1 retains `absmod:meow_blade`. Stages 2–9 use `absmod:meow_blade_stage_2` through `absmod:meow_blade_stage_9`.
- Independent damage values are defined in `common/src/main/java/com/abyssredemption/absmod/item/MeowBladeStage.java`.
  They represent total damage for a player with vanilla base attributes. Final values have not been supplied:
  all stages currently use placeholder damage **7** and attack speed **1.6**.
- Durability, enchantability and repair material follow the diamond sword. All stages belong to `minecraft:swords`.
- Obtain items from the Combat tab or use `/give @s absmod:meow_blade` or `/give @s absmod:meow_blade_stage_9`.
- Progression, stage switching and crafting recipes are not implemented.

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
