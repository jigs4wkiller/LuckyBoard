# LuckyBoard Changelog

All changes in this repository are for the initial public release of LuckyBoard, 
a rebranded and enhanced set of Morphe patches for Gboard from @jasonwu1994.


## v1.1.0-beta - Gboard 17.5.x Update

### Target APK Update
- Updated target to Gboard 17.5.7.917159154-lite_release-arm64-v8a.
- All obfuscated class references updated for the new APK version.

### Patch Consolidation
- **Luckify Gboard**: Merged "Package Rename" and "Replace Gboard with LuckyBoard" into a single patch with configurable app name (default: LuckyBoard). Users can now set any custom name via the gear icon.
- Removed deprecated separate "Universal Drawable Density Cleaner" and "Universal Language Cleaner" — replaced by combined "Universal Resource Cleaner".

### Removed Patches
- **QWERTY Slide Symbols**: Disabled pending reverse engineering for new APK (obfuscated types Loaa;→Lkwf;, ~40 resource IDs changed).
- Removed unregistered patches: Chinese Voice, Zhuyin Slide, Zhuyin Traditional/Simplified Toggle, Zhuyin Custom Symbols (were never exposed to users).

### Feature Flags Update
- Added 25 new flags from Gboard 17.5.x: Writing Tools (7 flags), Dynamic Art, Float Keyboard, Auto-fill, Custom Sticker Tab, Animated Emoji Suggestions, Contextual GIF Search, Dynamic Font Size, Dynamic Diacritic Key, Backup Personal Dictionary, Text Preview, Tablet Large, Voice Edit, Voice Chip Tooltip, Clipboard Action Chips, and more.
- Removed 6 obsolete flags not present in new APK: enable_clipboard_text_editor, support_accessory_keyboard, enable_voice_widget, enable_ocr, enable_regular_dictation_redesign, bright_key_on_dynamic_color_dark_theme.
- Fixed Feature Flags patch: use concrete `Ljil;` class instead of `Ljie;` interface.

### Bytecode Patches Updated
- Signature Bypass: `Lpuo;` → `Lmgg;`
- Clipboard Enhancements: `Leln;` → `Ldas;`, `Lemr;` → `Ldca;`, `Lemk;` → `Ldbu;`, `Lkm;` → `Lkh;`
- Symbol Footer Order: `Lfsg;` → `Leej;`, `Ltvg;` → `Lpuv;`
- Undo/Redo: `Lmku;` → `Ljih;`, `Lmkr;` → `Ljie;`
- Settings Homepage: `Lddg;` → `Lbtn;`
- Latin Globe: method `U()` → `T()`, return type `Lvky;` → `Lrdm;`
- Web Clipboard: `Lnfl;` → `Lkbp;`

### Resource Patches Updated
- Settings Clean-Up: Updated hardcoded resource keys for new APK.
- About Page: Updated version key reference.
- Patches Settings: Updated fallback icon resource ID.

### Resource Cleaner
- Fixed language cleaning: only targets actual language folders (values-xx), not config folders (values-sw411dp, values-night, etc.).
- Universal Resource Cleaner made truly universal (no compatibleWith constraint).
- Keeps mipmap-anydpi and drawable-anydpi folders (contain app icon XMLs).

### Settings Crash Fix
- Fixed Luckify patch: skips class name/package name attributes in manifest to prevent SettingsActivity crash.
- Resource Cleaner no longer deletes entire density folders (was breaking layout inflation).

## v1.0.0 - Initial Public Release 

### Core Rebranding & Localization
- Full rebrand from original Gboard-patches to LuckyBoard (dev.lucky.gboardpatches packages, markers, activities, providers, PREFS, strings, about page, README).
- All patch descriptions, in-app Feature Flags menu, toasts, restart notes, and UI strings cleaned to pure English.
- Brand rename patch: "Replace Gboard with LuckyBoard" (replaces package references and app name for side-by-side installation).
- Updated author/contact to jigs4wkiller/LuckyBoard in all metadata, patch menu, and docs.
- Cleaned duplicate entries, stale Chinese strings, and ensured 19 patches (verified via CLI list-patches and pure buildAndroid .mpp).

### In-App Feature Flags Menu (Centralized Runtime Control)
- New "Feature Flags" section injected into Gboard's Patches/LuckyBoard settings (via Settings Patch + carrier for extension).
- All flags start disabled by default; toggle them on directly inside Gboard at runtime (no per-flag selection needed at patch/install time).
- Feature Flags patch simplified: always applies UI marker + all individual flag markers (grammar checker, inline suggestions, clipboard enhancements, key shape, etc.).
- Added many additional flags from decompile analysis (e.g. silk_theme, material3_theme, enable_adjust_default_keyboard_height, silk_popup, enable_ai_core_smart_reply, keyboard_redesign_google_sans, bright_key_on_dynamic_color_dark_theme, and more).
- Special handling in runtime for key shape related flags (more_pill_keys, pill_shaped_key, belka_rounded_* etc.) to ensure they are forced ON when the customization marker is active.
- In-app menu supports search, descriptions, and restart prompts where needed.
- Deduplication: removed standalone per-flag patches (now centrally handled here to avoid duplicates in the list).

### Extended Key Shape Options
- Deep integration for extended key shapes beyond the stock 3 options (None/Rectangular/Rounded).
- New options: "Less rounded" (subtle curve) and "Horizontal lines" (flat with lines), plus full gradations (light/medium/strong/very strong/pill).
- Implemented via:
  - Precise flag forcing (safe boolean flags only: more_pill_keys, pill_shaped_key, belka_rounded_keyboard, enable_rounded_key_by_default etc. to avoid numeric radius crashes on round/pill).
  - Custom XML injection (ExtendedPreference entries) into relevant theme/key shape preference files (settings_*.xml, theme XMLs, Japanese keyboard files) via marker patch.
  - Whitelist restricted to safe preference files only (prevents bloat/SAX issues on broad walks).
  - Aggressive shouldForceFlagTrue logic in GboardFeatureFlagsRuntime for key_shape_selection + customization markers.
- Key shape related flags default to ON in the in-app prefs when marker present (ensures options appear in Theme > Key shape picker).
- Fixes for crashes on round/pill, pill breakage (community Rboard/GboardThemes research), and safe injection (exact-match only, real rounded/shape pref detection before insert).
- Japanese and other keyboard settings XMLs included in whitelist for full coverage.

### Universal Patches (Work on Any App, User-Controlled via Gear/Options)
- **Universal PNG Optimizer**: Real structural optimization using pure-Java pngtastic library (filter selection, compression levels, zopfli path) inside Morphe/Android runtime. No javax.imageio/AWT (not available in Morphe). Discovers and reports PNGs/9.pngs, applies optimizations where beneficial, reports freed space. Ant classes stripped at build time for CLI/app compatibility. Replaces stub/discovery-only mode. Note: for maximum lossy color quantization (pngquant-level), use original external script on PC.
  - Fixes: proper Morphe context root (get("res").parentFile), robust exclusion (build/original/smali/kotlin), no more "no pngs found" or ImageIO crashes.
- **Universal Drawable Density Cleaner** (from mpatcher script conversion): Select via patch options (gear) exactly which DPIs to keep (ldpi/mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi; default xxhdpi). Removes duplicates from other densities + mipmap. Universal (no compatibleWith). Reports removed files.
- **Universal Language Cleaner** (Dellang): Enter space/comma-separated language codes to KEEP (e.g. "en de fr") via string option in gear. Removes all other values-*/mipmap language folders. No auto-detect. Universal. Reports removed count and space freed.

### Settings & UI Improvements
- **Settings Homepage Override**: FORCE_NEW option always available (isForceNewSupported returns true unconditionally). Allows forcing the new expressive settings UI via in-app menu even on devices without expressive aconfig runtime, "is_expressive_design_enabled" system property, or required resources (older Android, custom ROMs). Style decision driven purely by preference + bytecode hook. AUTO still respects system checks. Includes trial guards, crash recovery, and warnings.
- **Settings Clean-Up**: Robust removal of Help & Feedback, Info, Rate Us, and Privacy categories from main settings (and about). Precise class-name based (HeaderPreference/RateUsPreference) + exact decompile key fallbacks on settings*.xml only. Privacy subs disabled (set defaultValue/enabled=false) in setting_privacy.xml by attribute only (no node removal to avoid SAX errors). Safe whitelist for key shape injection. Icon/symbol next to 'Patches' entry restored (unconditional fallback icon from decompile).
- Menu header normalized to 'Settings'. All from decompile analysis for compatibility with supported Gboard version.

### Other Patches & Enhancements
- Incognito Enhancements (clipboard retention/voice/always-incognito, ported from Adobo).
- Web Clipboard (desktop browser sync, pairing code, Quick Settings tile, bundled Web UI).
- Custom Symbols tab + long-press comma heart shortcut.
- Emojis, Stickers & GIFs Tab Order (drag-and-drop reordering).
- QWERTY Slide Symbols (English/Latin QWERTY letter keys only; crash fixes with valid smali stubs).
- English Globe Key Ignore Delay (configurable 0-1000ms, 0 to disable).
- Signature Bypass.
- Undo/Redo access points.
- Latin Globe Key Ignore Interval.
- Package rename support.
- Various stability fixes: PatchLoader NPE (rebind aliases), QWERTY slide crash, clipboard live-update stability and order index, release size/metadata consistency.
- Pure build workflow: always use buildAndroid + generatePatchesList for full ~3MB .mpp (no half-size releases). Self push/build/release on main. CLI list-patches verification before upload. patches-list.json/bundle.json always regenerated for accurate 19 patches (English only).

### Technical & Compatibility
- Supports official Gboard 17.0.10.880768217-release-arm64-v8a (and compatible).
- All patches verified loadable in pure .mpp via morphe-cli list-patches and real APK patch tests (including PNG opti reporting "Found N PNG(s)", cleaners reporting removals/freed space).
- No more duplicates (flag patches centralized in Feature Flags).
- PNG optimizer: full discovery + real optimization (pngtastic), proper exclusion, no desktop Java deps.
- Permanent fixes for CLI/app compatibility (ant class stripping at dep time for pngtastic, null-safe DOM walkers).
- Build produces clean vanilla .mpp (rve for extension carrier properly visible).

### Installation & Usage
- Add as Morphe source: https://github.com/jigs4wkiller/LuckyBoard (or deep link).
- Pre-built .mpp in Releases.
- Patches start with sensible defaults; use gear (options) for universals; use in-app LuckyBoard/Patches > Feature Flags for runtime toggles.
- Many changes require Gboard restart (button in settings where applicable).
- For maximum PNG size reduction, combine with external pngquant + optipng on PC if desired.

This initial release consolidates years of community research (decompiles, Rboard/GboardThemes flag work) into a clean, English-only, self-maintained LuckyBoard experience. All features above were active and current as of the commits scanned for this changelog.

For the absolute latest patch list and options, see the generated patches-list.json in the repo or the .mpp metadata.
