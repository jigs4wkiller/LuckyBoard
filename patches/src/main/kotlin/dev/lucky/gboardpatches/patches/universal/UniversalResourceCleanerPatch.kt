package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import java.io.File
import java.io.FileFilter

@Suppress("unused")
val universalResourceCleanerPatch = resourcePatch(
    name = "Universal Resource Cleaner",
    description = "Removes unused DPI resources (drawable-*/mipmap-*) and language resources in one patch. " +
                  "Choose which density to keep (default: xxxhdpi) and additional languages via the gear icon. " +
                  "English (en) is always kept.",
    default = true
) {
    val keepDensity = stringOption(
        key = "keep_density",
        title = "Keep only this density",
        description = "Which DPI density to keep (all other drawable/mipmap folders will be cleaned). Default: xxxhdpi",
        default = "xxxhdpi"
    )

    val extraLanguages = stringOption(
        key = "extra_languages",
        title = "Additional languages to keep",
        description = "Comma or space separated language codes (e.g. de,fr,es). English (en) is always kept unless you explicitly list it.",
        default = "de"
    )

    finalize {
        val resDir = get("res")
        if (resDir == null || !resDir.exists() || !resDir.isDirectory) {
            println("  [ResourceCleaner] No res/ directory found.")
            return@finalize
        }

        println("  [UniversalResourceCleaner] Starting...")

        val chosen = keepDensity.value ?: "xxxhdpi"
        println("  Keeping density: $chosen")
        keepOnlyDensity(resDir, chosen)
        removeEmptyDirectories(resDir)

        val langsInput = extraLanguages.value ?: "de"
        val keepLangs = langsInput.split(Regex("[,\\s]+"))
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toMutableSet()

        if (!keepLangs.contains("en")) {
            keepLangs.add("en")
        }
        println("  Keeping languages: ${keepLangs.joinToString()}")
        removeUnusedLanguages(resDir, keepLangs)
        removeEmptyDirectories(resDir)

        println("  [UniversalResourceCleaner] Finished.")
    }
}

// ==================== Density Cleaning Logic ====================
private fun keepOnlyDensity(resDir: File, keep: String) {
    val suffix = "-$keep"

    val keepFilter = object : FileFilter {
        override fun accept(f: File): Boolean {
            return f.isDirectory &&
                    (f.name.startsWith("drawable") || f.name.startsWith("mipmap")) &&
                    f.name.endsWith(suffix)
        }
    }

    val keepDirs = resDir.listFiles(keepFilter)?.toList() ?: emptyList()
    if (keepDirs.isEmpty()) return

    val tempDirs = keepDirs.mapNotNull { dir ->
        val temp = File(resDir, "urc_${dir.name}")
        if (dir.renameTo(temp)) temp else null
    }

    val keepFiles = mutableSetOf<String>()
    tempDirs.forEach { tempDir ->
        tempDir.walkTopDown().filter { it.isFile }.forEach { keepFiles.add(it.name) }
    }

    if (keepFiles.isEmpty()) {
        tempDirs.forEachIndexed { i, t -> if (t.exists()) t.renameTo(keepDirs[i]) }
        return
    }

    val otherDirs = resDir.listFiles { f ->
        f.isDirectory &&
                (f.name.startsWith("drawable") || f.name.startsWith("mipmap")) &&
                !f.name.startsWith("urc_") &&
                !f.name.endsWith("-anydpi") && !f.name.endsWith("-nodpi")
    }?.toList() ?: emptyList()

    otherDirs.forEach { otherDir ->
        otherDir.listFiles()?.forEach { file ->
            if (file.isFile && keepFiles.contains(file.name)) {
                file.delete()
            }
        }
    }

    tempDirs.forEachIndexed { i, tempDir ->
        if (tempDir.exists()) {
            tempDir.renameTo(keepDirs[i])
        }
    }
}

// ==================== Language Cleaning Logic ====================
private fun removeUnusedLanguages(resDir: File, keepLangs: Set<String>) {
    val langPattern = Regex("^values-[a-z]{2,3}(-r[A-Z]{2}|\\+.+)?$")
    val langDirs = resDir.listFiles { f ->
        f.isDirectory && f.name.startsWith("values") && f.name != "values"
    } ?: return

    langDirs.forEach { dir ->
        if (!langPattern.matches(dir.name)) return@forEach
        val qualifier = dir.name.removePrefix("values-")
        val lang = qualifier.substringBefore("-").substringBefore("+").lowercase()
        if (!keepLangs.contains(lang)) {
            println("      Removing language folder: ${dir.name}")
            dir.deleteRecursively()
        }
    }
}

private fun removeEmptyDirectories(dir: File) {
    dir.walkBottomUp()
        .filter { it.isDirectory && it.listFiles()?.isEmpty() == true }
        .forEach {
            it.delete()
            println("      Removed empty directory: ${it.name}")
        }
}
