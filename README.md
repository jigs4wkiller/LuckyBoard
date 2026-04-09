<h1 align="center">Gboard Patches</h1>

<p align="center">
  Morphe patches for Gboard focused on improving the experience for users in Taiwan.
</p>

<p align="center">
  <a href="https://github.com/jasonwu1994/Gboard-patches/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/jasonwu1994/Gboard-patches?display_name=tag&label=Release&style=for-the-badge"></a>
  <a href="https://github.com/jasonwu1994/Gboard-patches"><img alt="Total downloads" src="https://img.shields.io/github/downloads/jasonwu1994/Gboard-patches/total?label=Downloads&style=for-the-badge"></a>
  <a href="https://morphe.software/add-source?github=jasonwu1994/Gboard-patches"><img alt="Add to Morphe" src="https://img.shields.io/badge/Morphe-Add%20Source-00A8FF?style=for-the-badge"></a>
  <a href="https://github.com/jasonwu1994/Gboard-patches"><img alt="GitHub stars" src="https://img.shields.io/github/stars/jasonwu1994/Gboard-patches?style=social"></a>
</p>

## Overview

Gboard Patches is a public Morphe source for a curated set of Gboard enhancements focused on improving the overall Gboard experience for users in Taiwan, with emphasis on local input habits and day-to-day usability.

## Included Patches

- `Package Rename`
- `English QWERTY Slide Symbols`
- `Zhuyin Slide Input`
- `Zhuyin Quick Traditional/Simplified Toggle`
- `Chinese Online Voice Input`

## Install

Add this repository as a Morphe source:

- [Open in Morphe](https://morphe.software/add-source?github=jasonwu1994/Gboard-patches)
- Or manually add `https://github.com/jasonwu1994/Gboard-patches`

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
