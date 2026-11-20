# Contributing

## Layout and compatibility

- Keep loader-independent Java code and resources in `common/src/main`.
- Keep Fabric and NeoForge imports in their corresponding platform projects.
- Use `com.abyssredemption.absmod` as the base Java package and `absmod` as the namespace.
- Preserve registered item IDs when changing stages to avoid breaking existing saves.
- Use meaningful English technical names, never pinyin, for files, packages, classes, methods, variables, item IDs and commands.
- Keep code comments and docstrings in English. Update both `en_us` and `zh_cn` translations.
- Maintain `README.md` in English and `README.zh-CN.md` in Simplified Chinese with matching content and cross-links.
- Follow `.editorconfig` and `.gitattributes`. Do not commit IDE settings, caches, game runs, credentials or build outputs.
- Commit the Gradle Wrapper, including its JAR, and preserve the verified distribution checksum.

## Validation before pushing

Use the root wrapper with JDK 25 and a Java 21 runtime. The default build targets Minecraft 1.21.1 NeoForge. Install the pinned optional test dependencies with `tools/quests/fetch_1211_dependencies.ps1` and merge the project's FTB chapter into the development instance for integration testing.

```powershell
./gradlew.bat build
./gradlew.bat -p 1.21.1NeoForge runClient -PworkshopTest
git diff --check
git diff --cached --check
git diff --cached --stat
```

Rebuild after a fixture run to exclude verification classes from the release. For an explicit historical 26.2 regression build, use `./gradlew.bat build -ParchivedTargets`.

## Commits and review

Use focused commits with descriptive English subjects, for example
`feat: add nine Meow Blade stages for Fabric and NeoForge`.
Describe the behavior, validation results and known placeholders in the change description.
Inspect staged files before pushing and push only the intended branch.

## License status

The mod metadata currently declares `All Rights Reserved`; an open-source license
has not been selected. Do not change that declaration or add third-party assets
without confirming their licensing and attribution requirements.

## Minecraft 1.21.1 port

The independent `1.21.1NeoForge` project compiles `common/src/minecraft1211/java` against NeoForge 21.1.265. This is the sole target for ongoing gameplay development. Also run `./gradlew.bat -p 1.21.1NeoForge build` when changing port or shared resources. Runtime mods and optional fixture instructions are documented in docs/PORT_1211.md; never package fixture classes or third-party runtime jars.
