package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import com.googlecode.pngtastic.core.PngException
import com.googlecode.pngtastic.core.PngImage
import com.googlecode.pngtastic.core.PngOptimizer
import java.io.File

/**
 * Universal PNG Optimizer patch.
 *
 * Converted from the mpatcher script (used in APKToolM environments).
 *
 * Uses the pure-Java pngtastic library (https://github.com/depsypher/pngtastic) for
 * real structural PNG optimization inside Morphe (no javax.imageio, no native .so).
 * Performs filter selection, compression level search and optional Zopfli for
 * better deflate (comparable to optipng / zopflipng level gains).
 *
 * Lossy color quantization (pngquant / libimagequant style, big wins on many assets)
 * is NOT performed here to keep everything pure Java + Android-Morphe compatible.
 * For maximum size reduction use the original desktop mpatcher script + pngquant+optipng.
 *
 * Excludes build/, original/, smali* and kotlin* directories.
 * Both regular PNGs and *.9.png are optimized (pngtastic is lossless so safe for 9-patches).
 *
 * UNIVERSAL: no compatibleWith() so it applies to any app in Morphe.
 */
internal val universalPngOptimizerPatch = resourcePatch(
    name = "Universal PNG Optimizer",
    description = "Optimizes PNG and *.9.png files in the decompiled APK using pure-Java pngtastic (structural opti like optipng/zopflipng: better filters + compression). No color quantization (that still needs desktop pngquant). Excludes build/original/smali*/kotlin* dirs. Safe for 9-patches. UNIVERSAL patch - works on any app in Morphe.",
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

        println("  PngOptimizer working (pngtastic pure-Java)...")
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

        val ninePngs = root.walkTopDown()
            .filter { f ->
                f.isFile &&
                    f.name.endsWith(".9.png", ignoreCase = true) &&
                    !isExcluded(f.invariantSeparatorsPath)
            }
            .toList()

        val victimPngArr = regularPngs.size
        val victimPng9Arr = ninePngs.size

        if (victimPngArr > 0) {
            println("  Found ${victimPngArr} regular PNG(s).")
        } else {
            println("  No regular png files found!")
        }
        if (victimPng9Arr > 0) {
            println("  Found ${victimPng9Arr} 9.png file(s). (lossless opti safe for 9-patch borders)")
        } else {
            println("  No 9.png files found!")
        }

        if (victimPngArr == 0 && victimPng9Arr == 0) {
            println("  Done (nothing to do).")
            return@finalize
        }

        // Real optimizer from pngtastic (pure Java, works in Morphe/Android runtime)
        val optimizer = PngOptimizer("error")
        // Default brute-force best filters + compression levels.
        // For even better ratios (slower) you could do: optimizer.setCompressor("zopfli", 15)
        // but we keep default for reasonable patch time.
        optimizer.setCompressor(null, null)

        var processed = 0
        regularPngs.forEach { f ->
            if (optimizePngFile(f, optimizer)) processed++
        }
        ninePngs.forEach { f ->
            if (optimizePngFile(f, optimizer)) processed++
        }

        val sAfter = calculateTotalSize(root)
        val freed = (sBefore - sAfter) / 1024

        val totalSavings = optimizer.getTotalSavings()
        val resultsCount = optimizer.getResults().size

        println("  Done!")
        println("  Results:")
        println("      Start time: $startAt        End time: ${java.time.LocalTime.now()}")
        println("      Files processed:")
        println("             png| $victimPngArr")
        println("           9.png| $victimPng9Arr")
        println("      Optimized: $resultsCount (attempted $processed)")
        println("           Freed: $freed Kb  (internal: ${totalSavings / 1024} Kb reported by pngtastic)")
        println("  Note: Structural optimization (filters/compression) via pngtastic. For lossy color")
        println("        quantization (pngquant-style 60-80% wins) use the original mpatcher script on PC.")
    }
}

private fun calculateTotalSize(dir: File): Long {
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

/**
 * Optimize a single PNG using pngtastic PngOptimizer (pure Java, no AWT).
 * Writes to a temp, then replaces original only on success + smaller size.
 * Returns true if we successfully produced an optimized version (even if size same or slightly larger due to overhead; pngtastic usually wins).
 */
private fun optimizePngFile(
    file: File,
    optimizer: PngOptimizer
): Boolean {
    val srcPath = file.absolutePath
    val tmp = File("$srcPath.optipngtmp")
    try {
        val image = PngImage(srcPath, "error")
        // Write optimized to temp (pngtastic always produces a new file)
        optimizer.optimize(image, tmp.absolutePath, false, null)

        if (tmp.exists()) {
            val newLen = tmp.length()
            val oldLen = file.length()
            if (newLen > 0 && (newLen < oldLen || oldLen == 0L)) {
                // Good win (or first time)
                if (!file.delete()) {
                    // Rare: can't delete orig, leave tmp and skip
                    tmp.delete()
                    return false
                }
                if (!tmp.renameTo(file)) {
                    // Cleanup if rename failed
                    tmp.delete()
                    return false
                }
                return true
            } else {
                // No win or larger (rare) - keep original, discard tmp
                tmp.delete()
                return false
            }
        }
    } catch (e: PngException) {
        tmp.delete()
        // quiet; many 9-patches or weird chunks may trigger on some images, that's ok
    } catch (e: Exception) {
        tmp.delete()
        println("    Skipped ${file.name}: ${e.javaClass.simpleName}")
    }
    return false
}