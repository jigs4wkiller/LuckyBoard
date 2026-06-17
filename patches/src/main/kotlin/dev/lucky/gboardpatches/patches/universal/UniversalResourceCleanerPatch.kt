package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import java.io.File
import java.io.FileFilter

/**
 * Universal Resource Cleaner
 *
 * Ein Patch, der zwei Dinge erledigt:
 * 1. Entfernt ungenutzte DPI-Ordner (drawable-*/mipmap-*)
 * 2. Entfernt ungenutzte Sprach-Ordner (values-*)
 *
 * Der User kann über das Zahnrad-Menü auswählen:
 * - Welche Density behalten werden soll (Default: xxxhdpi)
 * - Welche zusätzlichen Sprachen behalten werden sollen (Default: de + en)
 *
 * Universal Patch - funktioniert bei jeder App.
 */
internal val universalResourceCleanerPatch = resourcePatch(
    name = "Universal Resource Cleaner",
    description = "Entfernt ungenutzte DPI- und Sprachressourcen in einem Patch. Wähle über das Zahnrad, welche Density (Default xxxhdpi) und welche Sprachen behalten werden sollen.",
    default = true
) {
    // === Density Auswahl ===
    val keepLdpi = booleanOption(key = "keep_ldpi", title = "Keep ldpi", default = false)
    val keepMdpi = booleanOption(key = "keep_mdpi", title = "Keep mdpi", default = false)
    val keepHdpi = booleanOption(key = "keep_hdpi", title = "Keep hdpi", default = false)
    val keepXhdpi = booleanOption(key = "keep_xhdpi", title = "Keep xhdpi", default = false)
    val keepXxhdpi = booleanOption(key = "keep_xxhdpi", title = "Keep xxhdpi", default = false)
    val keepXxxhdpi = booleanOption(key = "keep_xxxhdpi", title = "Keep xxxhdpi (empfohlen)", default = true)

    // === Sprachen ===
    val keepLanguages = stringOption(
        key = "keep_languages",
        title = "Zusätzliche Sprachen behalten",
        description = "Codes mit Leerzeichen oder Komma trennen (z.B. de fr it). Englisch (en) bleibt immer erhalten, außer du gibst es explizit an. Standard: de",
        default = "de"
    )

    finalize {
        val resDir = get("res") ?: return@finalize
        if (!resDir.isDirectory) return@finalize

        println("[UniversalResourceCleaner] Starte...")

        // === 1. Density Cleaning ===
        val chosenDensity = when {
            keepXxxhdpi.value -> "xxxhdpi"
            keepXxhdpi.value -> "xxhdpi"
            keepXhdpi.value -> "xhdpi"
            keepHdpi.value -> "hdpi"
            keepMdpi.value -> "mdpi"
            keepLdpi.value -> "ldpi"
            else -> "xxxhdpi"
        }
        println("  Behalte Density: $chosenDensity")
        keepOnlyDensity(resDir, chosenDensity)

        // === 2. Language Cleaning ===
        val input = keepLanguages.value?.trim() ?: "de"
        val keepLangs = input.split(Regex("[,\\s]+"))
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toMutableSet()

        // Englisch bleibt immer erhalten, außer der User will es explizit entfernen
        if (!keepLangs.contains("en")) {
            keepLangs.add("en")
        }

        println("  Behalte Sprachen: ${keepLangs.joinToString()}")
        removeUnusedLanguages(resDir, keepLangs)

        // Leere Ordner aufräumen
        removeEmptyDirectories(resDir)

        println("[UniversalResourceCleaner] Fertig.")
    }
}

// ==================== Density Logic ====================
private fun keepOnlyDensity(resDir: File, keepDensity: String) {
    val keepSuffix = "-$keepDensity"

    val keepFilter = FileFilter { f ->
        f.isDirectory && (f.name.startsWith("drawable") || f.name.startsWith("mipmap")) && f.name.endsWith(keepSuffix)
    }

    val keepDirs = resDir.listFiles(keepFilter)?.toList() ?: emptyList()
    if (keepDirs.isEmpty()) return

    // Temporär umbenennen
    val tempDirs = keepDirs.mapNotNull { dir ->
        val temp = File(resDir, "urc_${dir.name}")
        if (dir.renameTo(temp)) temp else null
    }

    // Alle Dateinamen sammeln, die wir behalten wollen
    val keepFiles = mutableSetOf<String>()
    tempDirs.forEach { tempDir ->
        tempDir.walkTopDown().filter { it.isFile }.forEach { keepFiles.add(it.name) }
    }

    if (keepFiles.isEmpty()) {
        // Zurück umbenennen und abbrechen
        tempDirs.forEachIndexed { i, t -> t.renameTo(keepDirs[i]) }
        return
    }

    // Alle anderen Density-Ordner durchgehen und überflüssige Dateien löschen
    val otherDirs = resDir.listFiles { f ->
        f.isDirectory &&
            (f.name.startsWith("drawable") || f.name.startsWith("mipmap")) &&
            !f.name.startsWith("urc_")
    }?.toList() ?: emptyList()

    otherDirs.forEach { otherDir ->
        otherDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name in keepFiles) {
                file.delete()
            }
        }
    }

    // Temporäre Ordner zurückbenennen
    tempDirs.forEachIndexed { i, tempDir ->
        if (tempDir.exists()) {
            tempDir.renameTo(keepDirs[i])
        }
    }
}

private fun removeUnusedLanguages(resDir: File, keepLangs: Set<String>) {
    val langDirs = resDir.listFiles { f ->
        f.isDirectory && f.name.startsWith("values")
    } ?: return

    langDirs.forEach { dir ->
        val langCode = if (dir.name == "values") "en" else dir.name.removePrefix("values-")
        if (langCode.lowercase() !in keepLangs) {
            println("      Entferne Sprachordner: ${dir.name}")
            dir.deleteRecursively()
        }
    }
}

private fun removeEmptyDirectories(dir: File) {
    dir.walkBottomUp()
        .filter { it.isDirectory && it.listFiles()?.isEmpty() == true }
        .forEach {
            it.delete()
            println("      Leeren Ordner entfernt: ${it.name}")
        }
}
