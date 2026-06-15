package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

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
        val root = File(".")
        if (!root.exists() || !root.isDirectory) {
            println("  [UniversalPngOptimizer] No valid decompile root directory found.")
            return@finalize
        }

        println("  PngOptimizer working...")
        println("  Target directory: \"${root.absolutePath}\"")

        val startAt = java.time.LocalTime.now().toString()
        val sBefore = calculateTotalSize(root)

        // Regular PNGs (exclude .9.png and excluded dirs)
        val excludedDirs = listOf("build", "original", "smali", "kotlin")
        val regularPngs = root.walkTopDown()
            .filter { f ->
                f.isFile &&
                    f.name.endsWith(".png", ignoreCase = true) &&
                    !f.name.endsWith(".9.png", ignoreCase = true) &&
                    excludedDirs.none { ex -> f.invariantSeparatorsPath.contains("/$ex/") }
            }
            .toList()

        val victimPngArr = regularPngs.size
        if (victimPngArr > 0) {
            println("  Optimizing colors...")
            regularPngs.forEach { f -> optimizePngFile(f, doQuantize = true) }

            println("  Optimizing the structure...")
            regularPngs.forEach { f -> optimizePngFile(f, doQuantize = false) }
        } else {
            println("  No png files found!")
        }

        // 9.png files (only color optimization, no structure pass - matching original script)
        val ninePngs = root.walkTopDown()
            .filter { f ->
                f.isFile &&
                    f.name.endsWith(".9.png", ignoreCase = true) &&
                    excludedDirs.none { ex -> f.invariantSeparatorsPath.contains("/$ex/") }
            }
            .toList()

        val victimPng9Arr = ninePngs.size
        if (victimPng9Arr > 0) {
            println("  Optimizing colors for 9.png...")
            ninePngs.forEach { f -> optimizePngFile(f, doQuantize = true) }
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
        println("           Freed: $freed Kb")
    }
}

private fun calculateTotalSize(dir: File): Long {
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

private fun optimizePngFile(file: File, doQuantize: Boolean) {
    try {
        val src = ImageIO.read(file) ?: return
        val processed = if (doQuantize) reduceColors(src) else src

        // Write to a temp file then replace (safer)
        val temp = File(file.parentFile, file.name + ".opt.tmp.png")
        if (ImageIO.write(processed, "png", temp)) {
            // Only replace if smaller or if we are doing structure pass (re-encode)
            if (temp.length() < file.length() || !doQuantize) {
                temp.copyTo(file, overwrite = true)
            }
            temp.delete()
        } else {
            temp.delete()
        }
    } catch (_: Exception) {
        // Skip unreadable or problematic PNGs silently (matches original behavior for bad files)
    }
}

/**
 * Basic color reduction / quantization.
 * Approximates pngquant behavior (color reduction + dither).
 * Uses Java's indexed color model with dithering enabled.
 * For production-grade results identical to pngquant, use the original external tool.
 */
private fun reduceColors(src: BufferedImage): BufferedImage {
    // Create an indexed image (typically results in 256 or fewer colors with dither)
    val dest = BufferedImage(src.width, src.height, BufferedImage.TYPE_BYTE_INDEXED)
    val g = dest.createGraphics()
    g.setRenderingHint(
        java.awt.RenderingHints.KEY_DITHERING,
        java.awt.RenderingHints.VALUE_DITHER_ENABLE
    )
    g.drawImage(src, 0, 0, null)
    g.dispose()
    return dest
}