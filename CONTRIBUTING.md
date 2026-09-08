# Contributing

## Layout and compatibility

- Keep loader-independent Java code and resources in `common/src/main`.
- Keep Fabric and NeoForge imports in their corresponding platform projects.
- Use `com.abyssredemption.absmod` as the base Java package and `absmod` as the namespace.
- Preserve registered item IDs when changing stages to avoid breaking existing saves.
- Keep code comments and technical identifiers in English. Update both `en_us` and `zh_cn` translations.
- Follow `.editorconfig` and `.gitattributes`. Do not commit IDE settings, caches, game runs, credentials or build outputs.
- Commit the Gradle Wrapper, including its JAR, and preserve the verified distribution checksum.

## Validation before pushing

Use JDK 25 and run from the repository root:

```powershell
./gradlew.bat build
git diff --check
git diff --cached --check
git diff --cached --stat
```

On Linux or macOS, use `./gradlew build`. Both loader builds must pass.
For item changes, inspect both output JARs for matching shared assets, language keys,
models and tags. Test gameplay in both loaders when changing gameplay behavior,
and explicitly report when game testing has not been performed.

## Commits and review

Use focused commits with descriptive English subjects, for example
`feat: add nine Meow Blade stages for Fabric and NeoForge`.
Describe the behavior, validation results and known placeholders in the change description.
Inspect staged files before pushing and push only the intended branch.

## License status

The mod metadata currently declares `All Rights Reserved`; an open-source license
has not been selected. Do not change that declaration or add third-party assets
without confirming their licensing and attribution requirements.
