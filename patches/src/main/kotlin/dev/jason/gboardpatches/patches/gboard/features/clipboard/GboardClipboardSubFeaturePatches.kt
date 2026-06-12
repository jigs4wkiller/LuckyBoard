package dev.jason.gboardpatches.patches.gboard.features.clipboard

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.shared.ANDROID_NS
import dev.jason.gboardpatches.patches.gboard.shared.childElements
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD
import org.w3c.dom.Element

internal fun createClipboardSubFeaturePatch(name: String, marker: String) = resourcePatch(
    description = "Aktiviert das Gboard Clipboard Sub-Feature: $name"
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    finalize {
        applyClipboardSubFeatureMarker(marker)
    }
}

context(context: ResourcePatchContext)
private fun applyClipboardSubFeatureMarker(marker: String) = with(context) {
    document("AndroidManifest.xml").use { document ->
        val manifest = document.documentElement
        val application = manifest.childElements("application").firstOrNull()
            ?: error("Could not find application element in AndroidManifest.xml")

        val markerName = "dev.jason.gboardpatches.feature.clipboard_$marker"

        val metaData = application.childElements("meta-data").firstOrNull {
            it.androidAttribute("name") == markerName
        } ?: document.createElement("meta-data").also { createdMetaData ->
            application.appendChild(createdMetaData)
        }

        metaData.setAndroidAttribute("name", markerName)
        metaData.setAndroidAttribute("value", "true")
    }
}

private fun Element.androidAttribute(localName: String): String? {
    val namespaced = getAttributeNS(ANDROID_NS, localName)
    if (namespaced.isNotBlank()) return namespaced
    return getAttribute("android:$localName").takeIf { it.isNotBlank() }
}

private fun Element.setAndroidAttribute(localName: String, value: String) {
    setAttributeNS(ANDROID_NS, "android:$localName", value)
}

// Einzel-Patches für die patches-list.json
internal val gboardClipboardCountdownMarkerPatch = createClipboardSubFeaturePatch("Countdown", "countdown")
internal val gboardClipboardCreationTimeMarkerPatch = createClipboardSubFeaturePatch("Creation Time", "creation_time")
internal val gboardClipboardOrderIndexMarkerPatch = createClipboardSubFeaturePatch("Order Index", "order_index")
internal val gboardClipboardPreviewLinesMarkerPatch = createClipboardSubFeaturePatch("Preview Lines", "preview_lines")
internal val gboardClipboardColumnCountMarkerPatch = createClipboardSubFeaturePatch("Column Count", "column_count")
