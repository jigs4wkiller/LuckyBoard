# Gboard Patches

Gboard Patches is a Morphe patch bundle focused on improving the Gboard experience for users in Taiwan.

It currently includes enhancements for Zhuyin slide input, quick Traditional/Simplified switching, English QWERTY slide symbols, and optional package rename support for side-by-side installation.

## Included Patches

- `Package Rename`
- `English QWERTY Slide Symbols`
- `Zhuyin Slide Input`
- `Zhuyin Quick Traditional/Simplified Toggle`

## Build

Before running Gradle locally, authenticate to Morphe's GitHub Packages registry with either:

- `gpr.user` and `gpr.key` in `~/.gradle/gradle.properties`
- `GITHUB_ACTOR` and `GITHUB_TOKEN` as environment variables

Build the Android patch bundle:

```powershell
.\gradlew.bat :patches:buildAndroid
```

Regenerate patch metadata:

```powershell
.\gradlew.bat generatePatchesList
```

Generated outputs:

- `patches/build/libs/*.mpp`
- `patches-list.json`
- `patches-bundle.json`

## License

Released under the [GNU General Public License v3.0](LICENSE).
