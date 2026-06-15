# LuckyBoard PNG Companion (for Morphe)

Companion APK that lets the Universal PNG Optimizer patch offload PNG optimization to a separate process ("auf andere Weise").

The in-.mpp pure-JDK basic re-compress usually frees 0KB (Gboard resources are already well compressed by build tools). By delegating here we can:
- Run in full Android app context (no Morphe patch loader restrictions like missing ant classes or javax.imageio)
- Easily add real PNG libs (pngtastic etc.) in *this* project's build.gradle only
- Extend with future advanced filters, multiple tries, native pngquant .so, etc. independently of the patches mpp

## How the delegation works
1. Patch (running inside Morphe) walks res/, copies PNGs/9.png to public work dir, writes a map file (origPrivatePath|publicWorkCopyPath).
2. Patch sends broadcast `dev.lucky.gboardpatches.pngcompanion.OPTIMIZE_PNGS` (with request_file extra) using ActivityThread reflection to obtain Context.
3. This app's exported receiver wakes (even if not running), reads the map, optimizes the *public copies* using its optimizer, writes `done.txt`.
4. Patch polls (≤60s), copies any smaller results back over the originals in Morphe's temp tree, cleans work dir.
5. If no companion or delegation fails/times out → fallback to the (weak) basic in-patch impl.

Work dir (must be writable by both): `/sdcard/LuckyBoardPngWork`

## Critical: Storage permissions (Android 10/11+)
- Pre-Android 11: runtime READ/WRITE_EXTERNAL_STORAGE (button in the app).
- Android 11+ (API 30+): "All files access" / MANAGE_EXTERNAL_STORAGE is required for arbitrary /sdcard paths.
  - Both **Morphe Manager** and **this companion** need it.
  - Grant in system Settings → Apps → [App] → Permissions → "Allow access to all files".
- The companion UI has a button that opens the settings screen for you.

Without the perm the copies fail and it gracefully falls back (0KB).

## Build the companion APK (easiest)
1. Android Studio: New Project → Empty Activity (or "No Activity").
2. Package name **must** be: `dev.lucky.gboardpatches.pngcompanion`
3. Copy **everything under `companion/src/main/`** from this repo into your new project's `app/src/main/`.
   - Keep the default `res/mipmap-*/ic_launcher*` that the template created (manifest references them).
4. (Recommended) In the app's `build.gradle` (module) you can now safely add a real optimizer:
   ```groovy
   dependencies {
       // Example for much better lossless results (pure Java, no native needed):
       // implementation 'com.googlecode.pngtastic:pngtastic:1.1'
   }
   ```
5. Build → Build APK(s). Install the APK.
6. Open the companion once, tap "Request storage permission", grant all-files on 11+.

## Usage in Morphe + LuckyBoard
- Add the LuckyBoard source in Morphe that includes the "Universal PNG Optimizer" patch (the one on beta-pure-png-impl or newer).
- Enable the patch (gear for options if any added later).
- Apply to Gboard.
- You should see a toast from the companion ("optimized X files").
- Check patch log in Morphe for "Companion found...", "Broadcast sent...", "Companion finished, copied back...".
- Freed should be > 0KB when companion succeeds (at minimum the chunk stripping + re-compress, more if you enhance the receiver).

If companion not present or can't write: patch falls back and logs the note to install companion.

## Manual trigger (debug / if auto missed)
Open the companion app → "Manually process pending request". It will also write done.txt so a waiting patch can finish.

## Extending for real savings (the point of companion)
Edit `PngOptimizeReceiver.java` → `optimizePngPure(...)`:

Current: IDAT re-inflate + BEST_COMPRESSION re-deflate + drop text/time/etc ancillary chunks (safe).

Better options you can add **only here**:
- Add pngtastic dependency and use `PngOptimizer` (try quant + compress level 9 + different strategies).
- Implement adaptive PNG filtering (choose best filter type 0-4 per scanline before deflate — biggest lossless win after basic zlib).
- Multiple passes + keep best result.
- For 9-patches be careful to keep exact pixel data (we already do).
- Bundle a small pngquant binary + Runtime.exec (root or termux not needed if you ship .so + JNI).
- Stats screen, per-folder button, "optimize my current Gboard APK" etc.

The patch side stays tiny and pure — all smarts live in the companion.

## Files in this skeleton
- `AndroidManifest.xml` — exported receiver + launcher activity + legacy storage perms
- `java/.../PngOptimizeReceiver.java` — the brains + full pure optimizer port + map support
- `java/.../MainActivity.java` — minimal UI for perms + manual + status (no extra res needed)
- (res/ left empty on purpose — use the ones from your new AS project template)

## Notes
- The patches .mpp on beta-pure-png-impl contains **only** the delegation + tiny fallback. No pngtastic, no ant classes.
- Still recommended for absolute max: run desktop tools (oxipng, pngquant, zopflipng) on a full Gboard APK before/after.
- This branch + companion is the "neuer Ansatz" after the 0KB pure-JDK experience.

Tested flow: patch detects companion via PM, copies out, broadcast, optimize in other process, copy back smaller, report freed Kb.

Happy optimizing!
