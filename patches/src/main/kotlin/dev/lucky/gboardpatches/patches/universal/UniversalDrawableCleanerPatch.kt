package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import java.io.File

/**
 * Universal Drawable Density Cleaner (Drawableclean)
 *
 * Converted from the original mpatcher script.
 *
 * Instead of auto-detection, the user selects via the patch options (gear icon in Morphe)
 * which density should remain.
 *
 * This patch removes duplicate drawable/mipmap assets from other densities,
 * keeping only the files that exist in the selected "keep" density.
 *
 * It handles both drawable-* and mipmap-* folders (including XMLs for mipmaps).
 *
 * UNIVERSAL: No compatibleWith() — can be used on any app in Morphe.
 *
 * Options:
 * - Keep ldpi / mdpi / hdpi / xhdpi / xxhdpi / xxxhdpi
 *   (select the one you want to keep; default is xxhdpi if none selected)
 *
 * After running, empty density folders are cleaned up.
 */
internal val universalDrawableCleanerPatch = resourcePatch(
    name = "Universal Drawable Density Cleaner",
    description = "Removes unused DPI resources (drawable-*/mipmap-*) by keeping only the files from the user-selected density. User chooses via patch options which DPI remains (no auto-detect). Works on any app (universal).",
    default = true
) {
    // No compatibleWith() — this is a universal patch for any app in Morphe.

    // User selects via these options which density should remain.
    // Only one should be enabled. If multiple or none, defaults to xxhdpi.
    val keepLdpi = booleanOption(
        key = "keep_ldpi",
        title = "Keep ldpi",
        description = "Keep only ldpi drawables/mipmaps and remove duplicates from other densities.",
        default = false
    )

    val keepMdpi = booleanOption(
        key = "keep_mdpi",
        title = "Keep mdpi",
        description = "Keep only mdpi drawables/mipmaps and remove duplicates from other densities.",
        default = false
    )

    val keepHdpi = booleanOption(
        key = "keep_hdpi",
        title = "Keep hdpi",
        description = "Keep only hdpi drawables/mipmaps and remove duplicates from other densities.",
        default = false
    )

    val keepXhdpi = booleanOption(
        key = "keep_xhdpi",
        title = "Keep xhdpi",
        description = "Keep only xhdpi drawables/mipmaps and remove duplicates from other densities.",
        default = false
    )

    val keepXxhdpi = booleanOption(
        key = "keep_xxhdpi",
        title = "Keep xxhdpi",
        description = "Keep only xxhdpi drawables/mipmaps and remove duplicates from other densities.",
        default = true
    )

    val keepXxxhdpi = booleanOption(
        key = "keep_xxxhdpi",
        title = "Keep xxxhdpi",
        description = "Keep only xxxhdpi drawables/mipmaps and remove duplicates from other densities.",
        default = false
    )

    finalize {
        val resDir = get("res")
        if (resDir == null || !resDir.exists() || !resDir.isDirectory) {
            println("  [DrawableCleaner] No res/ directory found.")
            return@finalize
        }

        println("  Drawableclean working...")

        val chosen = when {
            keepXxxhdpi.value == true -> "xxxhdpi"
            keepXxhdpi.value == true -> "xxhdpi"
            keepXhdpi.value == true -> "xhdpi"
            keepHdpi.value == true -> "hdpi"
            keepMdpi.value == true -> "mdpi"
            keepLdpi.value == true -> "ldpi"
            else -> "xxhdpi" // default as in the original script
        }
        println("  Keeping density: $chosen")

        val startAt = java.time.LocalTime.now().toString()
        val beforeSize = calculateSize(resDir)

        keepDensity(resDir, chosen)

        // Clean empty directories (matching the script)
        println("  Removing empty dir's:")
        removeEmptyDirectories(resDir)

        val afterSize = calculateSize(resDir)
        val freed = (beforeSize - afterSize) / 1024

        println("  Done!")
        println("  Results:")
        println("      Start time: $startAt        End time: ${java.time.LocalTime.now()}")
        println("      Removed files: $freed Kb")
    }
}



private fun keepDensity(resDir: File, keep: String) {
    val suffix = "-$keep"

    // Process drawable* folders for the keep density
    val drawableKeepDirs = resDir.listFiles { f ->
        f.isDirectory && f.name.startsWith("drawable") && f.name.contains(suffix)
    }?.toList() ?: emptyList()

    if (drawableKeepDirs.isNotEmpty()) {
        processDensityGroup(resDir, drawableKeepDirs, suffix, isMipmap = false)
    }

    // Process mipmap* folders for the keep density (also removes XMLs)
    val mipmapKeepDirs = resDir.listFiles { f ->
        f.isDirectory && f.name.startsWith("mipmap") && f.name.contains(suffix)
    }?.toList() ?: emptyList()

    if (mipmapKeepDirs.isNotEmpty()) {
        processDensityGroup(resDir, mipmapKeepDirs, suffix, isMipmap = true)
    }
}

private fun processDensityGroup(
    resDir: File,
    keepDirs: List<File>,
    suffix: String,
    isMipmap: Boolean
) {
    // Rename keep dirs to temp
    val tempDirs = keepDirs.map { keepDir ->
        val tempName = "dr_cl_${keepDir.name}"
        val tempDir = File(resDir, tempName)
        if (keepDir.renameTo(tempDir)) {
            tempDir
        } else {
            println("  WARNING: Could not rename ${keepDir.name}")
            keepDir
        }
    }

    // Collect all basenames from the temp (keep) dirs
    val canonicalNames = mutableSetOf<String>()
    tempDirs.forEach { tempDir ->
        tempDir.walkTopDown().filter { it.isFile }.forEach { f ->
            canonicalNames.add(f.name)
        }
    }

    if (canonicalNames.isEmpty()) return

    // Delete matching files from all other (non-temp) density dirs
    val otherDirs = resDir.listFiles { f ->
        f.isDirectory &&
            (f.name.startsWith("drawable") || f.name.startsWith("mipmap")) &&
            !f.name.startsWith("dr_cl_")
    }?.toList() ?: emptyList()

    otherDirs.forEach { otherDir ->
        otherDir.listFiles()?.forEach { file ->
            if (file.isFile && canonicalNames.contains(file.name)) {
                // For mipmaps we also delete XMLs (already covered by name match)
                file.delete()
            }
        }
    }

    // Rename temp dirs back
    tempDirs.forEachIndexed { index, tempDir ->
        if (tempDir.exists()) {
            val originalName = keepDirs[index].name
            val originalDir = File(resDir, originalName)
            if (!tempDir.renameTo(originalDir)) {
                println("  WARNING: Could not restore ${originalName}")
            }
        }
    }
}

private fun removeEmptyDirectories(dir: File) {
    dir.walkBottomUp().filter { it.isDirectory && it.listFiles()?.isEmpty() == true }.forEach {
        it.delete()
        println("      Removed empty: ${it.name}")
    }
}

private fun calculateSize(dir: File): Long {
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}