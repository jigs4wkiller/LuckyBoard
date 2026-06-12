package dev.jason.gboardpatches.patches.gboard.registry

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.about.gboardAboutPageResourcePatch
import dev.jason.gboardpatches.patches.gboard.features.addsymbols.*
import dev.jason.gboardpatches.patches.gboard.features.clipboard.*
import dev.jason.gboardpatches.patches.gboard.features.webclipboard.*
import dev.jason.gboardpatches.patches.gboard.features.englishqwerty.*
import dev.jason.gboardpatches.patches.gboard.features.featureflags.*
import dev.jason.gboardpatches.patches.gboard.packagerename.*
import dev.jason.gboardpatches.patches.gboard.settingshomepage.*
import dev.jason.gboardpatches.patches.gboard.signaturebypass.*
import dev.jason.gboardpatches.patches.gboard.symbolfooter.*
import dev.jason.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.jason.gboardpatches.patches.gboard.shared.gboardPatchesSettingsPatch
import dev.jason.gboardpatches.patches.gboard.features.undoredoaccesspoint.*
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

// ... (Rest of existing code)

@Suppress("unused")
val gboardClipboardCountdownPatch = resourcePatch(
    name = "Clipboard: Countdown",
    description = "Zeigt die verbleibende Haltezeit über jedem Clipboard-Item an."
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardClipboardEnhancementsPatch, gboardClipboardCountdownMarkerPatch)
}

@Suppress("unused")
val gboardClipboardCreationTimePatch = resourcePatch(
    name = "Clipboard: Erstellungszeit",
    description = "Zeigt die Erstellungszeit des Clipboard-Items an."
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardClipboardEnhancementsPatch, gboardClipboardCreationTimeMarkerPatch)
}

@Suppress("unused")
val gboardClipboardOrderIndexPatch = resourcePatch(
    name = "Clipboard: Reihenfolge-Index",
    description = "Zeigt den Index der Reihenfolge an."
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardClipboardEnhancementsPatch, gboardClipboardOrderIndexMarkerPatch)
}

@Suppress("unused")
val gboardClipboardPreviewLinesPatch = resourcePatch(
    name = "Clipboard: Vorschauzeilen",
    description = "Ermöglicht das Anpassen der maximalen Vorschauzeilen."
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardClipboardEnhancementsPatch, gboardClipboardPreviewLinesMarkerPatch)
}

@Suppress("unused")
val gboardClipboardColumnCountFeaturePatch = resourcePatch(
    name = "Clipboard: Spaltenanzahl",
    description = "Ermöglicht das Anpassen der Spaltenanzahl im Clipboard."
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardClipboardEnhancementsPatch, gboardClipboardColumnCountMarkerPatch)
}
