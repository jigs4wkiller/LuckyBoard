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

LuckyBoard is a Morphe patch source that adds many usability improvements and experimental features to Gboard. It optionally fully rebrands the app (for side-by-side installation with official Gboard) and removes region-specific content. Most experimental features can be controlled at runtime directly inside the patched app.

All patches and the UI are in clean English.

## Key Features

- Runtime toggleable **Feature Flags** (many hidden/experimental options)
- **Web Clipboard** – sync with desktop browser over LAN
- Strong **Clipboard** improvements + editing
- Better **Incognito** mode (clipboard + voice + forced incognito)
- Custom symbols, emoji tab reordering, Undo/Redo, QWERTY slide gestures
- Cleaned-up settings + universal optimization patches

## Included Patches

### Rebranding & Core (optional – for side-by-side install)

<details>
<summary><code>Add Gboard Signature Bypass</code></summary>

Bypasses Gboard signature checks so the patched version can run.
</details>

<details>
<summary><code>Package Rename</code></summary>

Renames package to `dev.lucky.com.google.android.inputmethod.latin` and the app to "LuckyBoard".
</details>

<details>
<summary><code>Replace Gboard with LuckyBoard</code></summary>

Replaces remaining "Gboard" strings in resources and UI.
</details>

### Usability & Input

<details>
<summary><code>Clipboard Enhancements</code></summary>

Improves retention time, item count, preview lines, timestamps and grid layout. Gear icon for per-feature options.
</details>

<details>
<summary><code>Custom Symbols</code></summary>

Adds dedicated symbols tab + quick access from comma long-press (China symbols removed).
</details>

<details>
<summary><code>Emojis, stickers & GIFs Tab Order</code></summary>

Drag & drop to customize the bottom tab order in the emoji panel.
</details>

<details>
<summary><code>Enable Undo/Redo feature</code></summary>

Enables Undo and Redo entry points.
</details>

<details>
<summary><code>English QWERTY Slide Symbols</code></summary>

Swipe down for symbols / swipe up to change case on QWERTY layouts.
</details>

<details>
<summary><code>Web Clipboard</code></summary>

Phone-hosted web portal for clipboard sync with desktop browsers + Quick Settings Tile.
</details>

### Experimental & Flags (Runtime Control)

> ⚠️ **Warning**: Not all flags work on every device or Gboard version. Some may cause instability or do nothing. Start with them disabled and test carefully.

<details>
<summary><code>Feature Flags</code></summary>

Master patch that exposes many internal/rollout/experimental flags as toggles inside the app (key shapes, voice widgets, OCR, dictation redesign, themes, AI features etc.). Can also inject a clean "Feature Flags" section directly into LuckyBoard settings.
</details>

<details>
<summary><code>Clipboard Entity Extraction</code> + <code>Clipboard Item Edit</code></summary>

Show extracted info from clipboard items and enable editing of clipboard entries.
</details>

<details>
<summary><code>Grammar Checker</code> + <code>Inline Suggestions</code></summary>

Enables grammar check and Smart Compose with their rollout gates.
</details>

<details>
<summary><code>Key Shape Selection</code></summary>

Extended key shape options and gradations in theme settings.
</details>

### Settings & Other

<details>
<summary><code>Incognito Enhancements</code></summary>

Enable clipboard and voice typing in incognito + force Gboard to always start in incognito mode (ported from Adobo).
</details>

<details>
<summary><code>Latin Globe Key Ignore Interval</code></summary>

Override for post-typing language switch delay on Latin layouts.
</details>

<details>
<summary><code>Settings Clean-Up</code></summary>

Removes Help & Feedback, Info, Rate and Privacy categories (diagnostics are disabled first).
</details>

<details>
<summary><code>Settings Homepage Override</code></summary>

Switch between new and legacy Gboard settings homepage style.
</details>

### Universal / Allrounder Patches (work on any app in Morphe)

These three patches are not Gboard-specific and can be used on almost any Android app in Morphe.

<details>
<summary><code>Universal Drawable Density Cleaner</code></summary>

Keeps only the DPI resources you select (ldpi to xxxhdpi). Very effective for reducing APK size.
</details>

<details>
<summary><code>Universal Language Cleaner</code></summary>

Keeps only the language folders you specify (e.g. "en de fr"). Enter codes in the patch options.
</details>

<details>
<summary><code>Universal PNG Optimizer</code></summary>

Optimizes all PNG and 9-patch files using pure Java (no external tools needed). Safe for 9-patches.
</details>

## Installation

Add this repository as a Morphe source:

- [Open in Morphe](https://morphe.software/add-source?github=jigs4wkiller/LuckyBoard)
- Or manually add `https://github.com/jigs4wkiller/LuckyBoard`

Pre-built `.mpp` bundles are available in the [Releases](https://github.com/jigs4wkiller/LuckyBoard/releases).

## Building from Source

### Authentication (required)

**Option 1** – Add to `~/.gradle/gradle.properties`:
```properties
gpr.user = your-github-username
gpr.key = your-personal-access-token
```

**Option 2** – Environment variables:
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

Outputs go to:
- `patches/build/libs/*.mpp`
- `patches-list.json` + `patches-bundle.json`

## Credits

- Based on / forked from the original **Gboard-patches** project
- Platform & tooling by **Morphe** (https://morphe.software)
- Incognito Enhancements patch ported from [Adobo by jkennethcarino](https://github.com/jkennethcarino/adobo)

## License

Released under the [GNU General Public License v3.0](LICENSE).
