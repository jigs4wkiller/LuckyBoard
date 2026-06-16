# LuckyBoard

<p align="center">
  Morphe patches for Gboard with practical upgrades, experimental features and clean English UI for global users.
</p>

<p align="center">
  <a href="https://github.com/jigs4wkiller/LuckyBoard/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/jigs4wkiller/LuckyBoard?display_name=tag&label=Release&style=for-the-badge"></a>
  <a href="https://github.com/jigs4wkiller/LuckyBoard"><img alt="Total downloads" src="https://img.shields.io/github/downloads/jigs4wkiller/LuckyBoard/total?label=Downloads&style=for-the-badge"></a>
  <a href="https://morphe.software/add-source?github=jigs4wkiller/LuckyBoard"><img alt="Add to Morphe" src="https://img.shields.io/badge/Morphe-Add%20Source-00A8FF?style=for-the-badge"></a>
  <a href="https://github.com/jigs4wkiller/LuckyBoard"><img alt="GitHub stars" src="https://img.shields.io/github/stars/jigs4wkiller/LuckyBoard?style=social"></a>
</p>

---

## Overview

LuckyBoard is a Morphe patch source that brings many usability improvements and experimental features to Gboard. It includes optional full rebranding (so it can be installed side-by-side with official Gboard) and removes region-specific content. Most experimental features can be toggled at runtime directly inside the app.

All patches and UI are in clean English.

## Key Features

- **Runtime Feature Flags** — Toggle dozens of hidden/experimental Gboard options directly in the app (with warning — see below)
- **Web Clipboard** — Sync clipboard between phone and desktop browser over LAN
- **Clipboard Enhancements** — Better retention, preview, editing and more
- **Incognito Mode Improvements** — Clipboard + voice typing in incognito + force incognito
- **Custom Symbols + Emoji Tab Reordering**
- **Undo/Redo, QWERTY slide symbols, Globe key timing**
- **Settings Cleanup** — Remove bloat from settings
- **Universal Patches** (work on any app in Morphe):
  - Drawable Density Cleaner
  - Language Resource Cleaner
  - PNG Optimizer

## Included Patches

### Rebranding & Core (Optional)
These allow side-by-side installation with official Gboard:

- **Add Gboard Signature Bypass**
- **Package Rename** → `dev.lucky.com.google.android.inputmethod.latin` + app name "LuckyBoard"
- **Replace Gboard with LuckyBoard** (UI strings)

### Usability & Input

- **Clipboard Enhancements** — Retention time, item limits, preview, timestamps, grid layout (gear icon for per-feature options)
- **Custom Symbols** — Dedicated symbols tab + quick access from comma long-press
- **Emojis, stickers & GIFs Tab Order** — Drag & drop reordering
- **Enable Undo/Redo**
- **English QWERTY Slide Symbols** — Swipe down for symbols, swipe up for case switch
- **Web Clipboard** — Phone-hosted web portal for desktop sync + Quick Settings Tile

### Experimental & Flags (Runtime Control)

> ⚠️ **Warning**: Not all flags work on every device or Gboard version. Some may cause instability, crashes or simply do nothing. Enable only what you need and test carefully. Flags start disabled by default.

- **Feature Flags** — Master patch that exposes many internal/rollout flags as toggles inside LuckyBoard settings (key shapes, voice widgets, OCR, dictation redesign, themes, AI features and more). Includes option to inject a clean "Feature Flags" section directly into the app.
- **Clipboard Entity Extraction** & **Clipboard Item Edit**
- **Grammar Checker** & **Inline Suggestions (Smart Compose)**
- **Key Shape Selection** — Extended key shape options

### Settings & Other

- **Incognito Enhancements** — Clipboard + voice in incognito + always-incognito mode (ported from Adobo)
- **Latin Globe Key Ignore Interval**
- **Settings Clean-Up** — Removes Help, Info, Rate and Privacy categories (with diagnostics disabled first)
- **Settings Homepage Override** — Switch between new and legacy settings layout

### Universal / Allrounder Patches (work on any Morphe app)

- **Universal Drawable Density Cleaner** — Keep only selected DPI resources (ldpi–xxxhdpi). Great for reducing APK size.
- **Universal Language Cleaner** — Keep only chosen language resources (e.g. "en de"). Enter codes via options.
- **Universal PNG Optimizer** — Optimizes PNGs and 9-patches using pure Java (no external tools needed).

## Installation

Add this repository as Morphe source:

- [Open in Morphe](https://morphe.software/add-source?github=jigs4wkiller/LuckyBoard)
- Or manually add `https://github.com/jigs4wkiller/LuckyBoard`

Pre-built `.mpp` bundles are available in the [Releases](https://github.com/jigs4wkiller/LuckyBoard/releases).

## Building from Source

### Authentication (required)
Authenticate to Morphe's GitHub Packages registry:

**Option 1** — Add to `~/.gradle/gradle.properties`:
```properties
gpr.user = your-github-username
gpr.key = your-personal-access-token
```

**Option 2** — Environment variables:
```bash
export GITHUB_ACTOR=your-github-username
export GITHUB_TOKEN=your-personal-access-token
```

### Linux / macOS
```bash
./gradlew :patches:buildAndroid
./gradlew generatePatchesList
```

### Windows
```powershell
.\gradlew.bat :patches:buildAndroid
.\gradlew.bat generatePatchesList
```

Outputs:
- `patches/build/libs/*.mpp`
- `patches-list.json` + `patches-bundle.json`

## Credits

- Based on / forked from the original **Gboard-patches** project
- Platform & tooling by **Morphe** (https://morphe.software)
- Incognito Enhancements patch ported from [Adobo by jkennethcarino](https://github.com/jkennethcarino/adobo)

## License

Released under the [GNU General Public License v3.0](LICENSE).
