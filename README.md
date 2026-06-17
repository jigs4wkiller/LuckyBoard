# LuckyBoard

<p align="center">
  Morphe patches for Gboard with strong privacy improvements, practical upgrades and experimental features.
</p>

<p align="center">
  <a href="https://github.com/jigs4wkiller/LuckyBoard/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/jigs4wkiller/LuckyBoard?display_name=tag&label=Release&style=for-the-badge"></a>
  <a href="https://github.com/jigs4wkiller/LuckyBoard"><img alt="Total downloads" src="https://img.shields.io/github/downloads/jigs4wkiller/LuckyBoard/total?label=Downloads&style=for-the-badge"></a>
  <a href="https://morphe.software/add-source?github=jigs4wkiller/LuckyBoard"><img alt="Add to Morphe" src="https://img.shields.io/badge/Morphe-Add%20Source-00A8FF?style=for-the-badge"></a>
  <a href="https://github.com/jigs4wkiller/LuckyBoard"><img alt="GitHub stars" src="https://img.shields.io/github/stars/jigs4wkiller/LuckyBoard?style=social"></a>
</p>

---

> **🚀 v1.1.0-beta — Now with full Gboard 17.5.x support!**
> 
> - Configurable `package_name` in **Luckify Gboard** (default: `dev.lucky.com.google.android.inputmethod.latin`) for flexible side-by-side installation
> - 25+ new **Feature Flags** for the latest Gboard 17.5.x settings UI
> - Refined **Universal Resource Cleaner** (stable, no Settings crashes)
> - Target: Gboard 17.5.7.917159154-lite arm64
> - Many bytecode & resource fixes for the new APK
>
> Pre-built `.mpp` bundles available in [Releases](https://github.com/jigs4wkiller/LuckyBoard/releases).

---

## Overview

LuckyBoard is a Morphe patch source focused on **privacy**, usability and experimental features for Gboard. It includes patches that reduce data collection, improve incognito mode and clean up settings. It can optionally fully rebrand the app for side-by-side installation with official Gboard.

All patches and the UI are in clean English. Privacy-related improvements are a core focus and will be expanded further in future updates.

## Key Features

- **Strong Privacy focus** — Force incognito, disable history & personalization, remove privacy-invasive settings
- Runtime toggleable **Feature Flags** (40+ flags including Writing Tools, Dynamic Art, Float Keyboard)
- **Web Clipboard** – sync with desktop browser over LAN
- Strong **Clipboard** improvements + editing
- Better **Incognito** mode (clipboard + voice + forced incognito)
- Custom symbols, emoji tab reordering, Undo/Redo
- **Luckify Gboard** — rename package + rebrand with custom app name and now configurable package name
- Universal optimization & cleanup patches

## Included Patches

### Privacy & Incognito (Core Focus)

These patches aim to reduce data collection and improve user control over privacy.

<details>
<summary><code>Incognito Enhancements</code></summary>

Enable clipboard and voice typing in incognito + force Gboard to always start in incognito mode. This disables typing history collection and personalization (ported from Adobo).
</details>

<details>
<summary><code>Settings Clean-Up</code></summary>

Removes Help & Feedback, Info, Rate and especially the Privacy category. All usage statistics, diagnostics, crash reporting and sharing options are disabled before the category is hidden.
</details>

### Rebranding & Core

<details>
<summary><code>Add Gboard Signature Bypass</code></summary>

Bypasses Gboard signature checks so the patched version can run.
</details>

<details>
<summary><code>Luckify Gboard</code></summary>

Renames package to a **configurable** `package_name` (default `dev.lucky.com.google.android.inputmethod.latin`) and replaces all "Gboard" branding with a custom app name (default: LuckyBoard). Allows side-by-side installation with official Gboard. Fully configurable via gear icon.
</details>

### Usability & Input

<details>
<summary><code>Clipboard Enhancements</code></summary>

Improves retention time, item count, preview lines, timestamps and grid layout. Gear icon for per-feature options.
</details>

<details>
<summary><code>Custom Symbols</code></summary>

Adds dedicated symbols tab + quick access from comma long-press.
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
<summary><code>Web Clipboard</code></summary>

Phone-hosted web portal for clipboard sync with desktop browsers + Quick Settings Tile.
</details>

### Experimental & Flags (Runtime Control)

> **Warning**: Not all flags work on every device or Gboard version. Some may cause instability or do nothing. Start with them disabled and test carefully.

<details>
<summary><code>Feature Flags</code></summary>

Master patch that exposes 40+ internal/rollout/experimental flags as toggles inside the app. Includes Writing Tools, Dynamic Art stickers, Float Keyboard, Auto-fill, AI features, modern themes, keyboard sizing, and more. Can also inject a "Feature Flags" section directly into LuckyBoard settings.
</details>

<details>
<summary><code>Latin Globe Key Ignore Interval</code></summary>

Override for post-typing language switch delay on Latin layouts.
</details>

<details>
<summary><code>Settings Homepage Override</code></summary>

Switch between new and legacy Gboard settings homepage style.
</details>

### Universal / Allrounder Patches (work on any app in Morphe)

These patches are not Gboard-specific and can be used on almost any Android app in Morphe.

<details>
<summary><code>Universal Resource Cleaner</code></summary>

Combined DPI and language resource cleaner. Choose which density to keep (default: xxxhdpi) and additional languages via the gear icon. English (en) is always kept. Keeps critical icon folders intact for stability.
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
