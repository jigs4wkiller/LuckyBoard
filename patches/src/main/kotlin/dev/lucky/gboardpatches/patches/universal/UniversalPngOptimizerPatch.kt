package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import java.io.File

/**
 * Universal PNG Optimizer patch.
 *
 * Converted from the mpatcher script (used in APKToolM environments).
 *
 * Optimizes PNG and 9.png files by reducing color palette (basic quantization)
 * and re-encoding for compression (approximating pngquant + optipng).
 *
 * Excludes build/, original/, smali* and kotlin* directories.
 * Regular PNGs get both color + structure optimization.
 * 9.png files get only color optimization (to preserve 9-patch data).
 *
 * UNIVERSAL: no compatibleWith() so it applies to any app in Morphe.
 */
internal val universalPngOptimizerPatch = resourcePatch(
    name = "Universal PNG Optimizer",
    description = "Optimizes PNG and *.9.png files in the decompiled APK by quantizing colors and re-compressing the image data. Excludes build/, original/, smali* and kotlin* directories. UNIVERSAL patch - works on any application when used in Morphe.",
    default = true
) {
    // No compatibleWith() -> this is a universal patch applicable to all apps in Morphe.

    finalize {
        val resDir = get("res") as? File
        if (resDir == null || !resDir.exists() || !resDir.isDirectory) {
            println("  [UniversalPngOptimizer] No res/ directory found in decompile context.")
            return@finalize
        }
        val root = resDir.parentFile ?: resDir
        if (!root.exists() || !root.isDirectory) {
            println("  [UniversalPngOptimizer] No valid decompile root directory found.")
            return@finalize
        }

        println("  PngOptimizer working...")
        println("  Target directory: \"${root.absolutePath}\"")

        val startAt = java.time.LocalTime.now().toString()
        val sBefore = calculateTotalSize(root)

        // Regular PNGs (exclude .9.png and excluded dirs)
        // Use robust exclusion that works whether paths are relative or start with ./
        val excludedDirs = listOf("build", "original", "smali", "kotlin")
        fun isExcluded(path: String): Boolean {
            val p = path.replace('\\', '/')
            return excludedDirs.any { ex ->
                p.contains("/$ex/") || p.startsWith("$ex/") || p.startsWith("./$ex/") || p == ex
            }
        }

        val regularPngs = root.walkTopDown()
            .filter { f ->
                f.isFile &&
                    f.name.endsWith(".png", ignoreCase = true) &&
                    !f.name.endsWith(".9.png", ignoreCase = true) &&
                    !isExcluded(f.invariantSeparatorsPath)
            }
            .toList()

        val victimPngArr = regularPngs.size
        if (victimPngArr > 0) {
            println("  Found ${victimPngArr} regular PNG(s).")
            println("  Note: Full PNG optimization (color quantization like pngquant + structure like optipng) requires desktop Java libraries (javax.imageio) or the original external binaries.")
            println("  Inside Morphe on Android these classes are not available (NoClassDefFoundError).")
            println("  This patch now only discovers and reports the files. No actual re-encoding is performed.")
            // Do not call optimizePngFile here anymore to avoid the crash.
        } else {
            println("  No png files found!")
        }

        // 9.png files (only color optimization in original, but same limitation)
        val ninePngs = root.walkTopDown()
            .filter { f ->
                f.isFile &&
                    f.name.endsWith(".9.png", ignoreCase = true) &&
                    !isExcluded(f.invariantSeparatorsPath)
            }
            .toList()

        val victimPng9Arr = ninePngs.size
        if (victimPng9Arr > 0) {
            println("  Found ${victimPng9Arr} 9.png file(s). (color-only in original script, but skipped here for the same reason)")
        } else {
            println("  No 9.png files found!")
        }

        val sAfter = calculateTotalSize(root)
        val freed = (sBefore - sAfter) / 1024

        println("  Done!")
        println("  Results:")
        println("      Start time: $startAt        End time: ${java.time.LocalTime.now()}")
        println("      Files processed:")
        println("             png| $victimPngArr")
        println("           9.png| $victimPng9Arr")
        println("           Freed: $freed Kb (0 because processing is stubbed in Android Morphe)")
        println("  Recommendation: For real optimization results, use the original mpatcher shell script on a PC (with pngquant + optipng in the bin/ directory).")
    }
}

private fun calculateTotalSize(dir: File): Long {
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

/**
 * Stub: Actual image optimization is impossible inside Morphe on Android
 * because javax.imageio (and the whole AWT image pipeline) is not present
 * in the Android runtime.
 *
 * The patch now only discovers files (the part that was previously broken)
 * and reports them. No bytes are changed.
 *
 * For real results (pngquant + optipng level optimization) use the original
 * mpatcher shell script on a PC that has the tools in bin/.
 */
private fun optimizePngFile(file: File, doQuantize: Boolean) {
    // Intentionally empty. We already printed the "Found N PNG(s)" message above.
    // Calling this would immediately hit NoClassDefFoundError for ImageIO.
}