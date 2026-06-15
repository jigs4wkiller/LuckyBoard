package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import java.io.File

/**
 * Universal Language Cleaner (Dellang)
 *
 * Converted from the mpatcher script.
 *
 * Instead of auto-detection, the user enters the language/country codes
 * (e.g. "en de fr") via the patch options gear in Morphe. Only those
 * "values-*" and "values-*-*" folders will be kept; all others are removed.
 *
 * No auto-detect. User explicitly chooses which languages remain.
 *
 * UNIVERSAL: No compatibleWith() - can be applied to any app in Morphe.
 */
internal val universalLanguageCleanerPatch = resourcePatch(
    name = "Universal Language Cleaner",
    description = "Removes unused language resources (values-*, values-*-*) by keeping only the folders for the user-specified language codes (e.g. en de fr). Enter codes via patch options gear. No auto-detect. Universal for any app.",
    default = true
) {
    // No compatibleWith - universal

    val keepLanguages = stringOption(
        key = "keep_languages",
        title = "Languages to keep (codes)",
        description = "Enter space or comma separated language codes to KEEP (e.g. \"en de fr\"). All other values-* folders will be deleted. Default: en",
        default = "en"
    )

    finalize {
        val resDir = get("res")
        if (resDir == null || !resDir.exists() || !resDir.isDirectory) {
            println("  [LanguageCleaner] No res/ directory found.")
            return@finalize
        }

        println("  Dellang working...")

        val input = keepLanguages.value?.trim() ?: "en"
        val keepLangs = input.split(Regex("[,\\s]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        if (keepLangs.isEmpty()) {
            println("  No languages specified, nothing to do.")
            return@finalize
        }

        println("  Keeping languages: ${keepLangs.joinToString()}")

        val startAt = java.time.LocalTime.now().toString()
        val beforeSize = calculateSize(resDir)

        // Rename kept language folders to temp (main + variants like values-en-rUS)
        val tempRenames = mutableListOf<Pair<File, File>>()
        for (lang in keepLangs) {
            val main = File(resDir, "values-$lang")
            if (main.exists() && main.isDirectory) {
                val temp = File(resDir, "dl_tmp_values-$lang")
                if (main.renameTo(temp)) {
                    tempRenames.add(main to temp)
                }
            }
            // variants: values-${lang}-*
            resDir.listFiles { f -> f.isDirectory && f.name.startsWith("values-$lang-") }?.forEach { v ->
                val temp = File(resDir, "dl_tmp_${v.name}")
                if (v.renameTo(temp)) {
                    tempRenames.add(v to temp)
                }
            }
        }

        // Delete all other values-* language folders (matching script patterns)
        var removed = 0
        val patterns = listOf(
            Regex("^values-[a-z]$"),
            Regex("^values-[a-z]\\+.*$"),
            Regex("^values-[a-z][a-z]$"),
            Regex("^values-[a-z][a-z]$"), // duplicate in script
            Regex("^values-[a-z][a-z]-.*$"),
            Regex("^values-[a-z][a-z][a-z]$"),
            Regex("^values-[a-z][a-z][a-z]-.*$")
        )

        resDir.listFiles { f -> f.isDirectory && f.name.startsWith("values-") && !f.name.startsWith("dl_tmp_") }?.forEach { d ->
            if (patterns.any { it.matches(d.name) }) {
                if (d.deleteRecursively()) {
                    removed++
                }
            }
        }

        // Restore kept from temp
        tempRenames.forEach { (orig, temp) ->
            if (temp.exists()) {
                temp.renameTo(orig)
            }
        }

        // Remove empty dirs
        println("  Removing empty dir's:")
        removeEmptyDirectories(resDir)

        val afterSize = calculateSize(resDir)
        val freed = (beforeSize - afterSize) / 1024

        println("  Done!")
        println("  Results:")
        println("      Start time: $startAt        End time: ${java.time.LocalTime.now()}")
        println("      Lang's removed: $removed")
        println("      Removed files: $freed Kb")
    }
}

private fun calculateSize(dir: File): Long {
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

private fun removeEmptyDirectories(dir: File) {
    dir.walkBottomUp().filter { it.isDirectory && it.listFiles()?.isEmpty() == true }.forEach {
        it.delete()
        println("      Removed empty: ${it.name}")
    }
}