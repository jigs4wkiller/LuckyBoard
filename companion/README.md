# LuckyBoard PNG Companion (for Morphe)

This is a companion Android app for users of LuckyBoard (Morphe patches for Gboard).

## Why?
The in-patch PNG optimizer (pure JDK) gives limited savings.
For better results (and future advanced algos like better filters, native tools, etc.), the optimization is delegated to this companion app's process.

The main patch writes a request and broadcasts; this app's receiver does the work and signals completion.
This keeps the patch .mpp small and free of heavy/buggy external libs.

## How to build the APK
1. In Android Studio: File > New > New Project > Empty Activity (or No Activity).
2. Set package to `dev.lucky.gboardpatches.pngcompanion`.
3. Copy the contents of this `companion/src/main/` folder into your project's `app/src/main/`.
4. (Optional) Add an icon.
5. Build > Build APK(s).
6. Install the APK on your device (alongside Morphe Manager and LuckyBoard patches).

## Usage
- Install the companion.
- When patching with LuckyBoard source that has the PNG optimizer patch:
  - The patch will detect the companion and delegate.
  - You may see a toast from the companion when it processes.
- If companion not installed, falls back to basic in-patch (may give 0 savings).

## Extending
- In `PngOptimizeReceiver.java`, improve the `optimizePngPure` method (copy the full parser+recompress logic from the patch, or add your own).
- You can add dependencies in this app's build.gradle (e.g. a PNG library) without affecting the main patch .mpp.
- Add more features: manual folder optimize button in MainActivity, stats, etc.

## Notes
- Requires storage permission (request at runtime if needed on newer Android).
- Works with the pure implementation in the LuckyBoard PNG patch on the beta-pure-png-impl branch.
- For max savings, still consider desktop tools for your APKs.

This is the "different way" – optimization happens in the companion's own process.
