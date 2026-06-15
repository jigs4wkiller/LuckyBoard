package dev.jason.gboardpatches.patches.gboard.features.featureflags

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.shared.ANDROID_NS
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD
import org.w3c.dom.Element

/**
 * Enables extended key shape customization / additional gradations in the theme key shape picker.
 * This unlocks less-rounded options and horizontal-line style keys (as steps between flat and the
 * rounded/pill shapes).
 */
internal val gboardKeyShapeCustomizationFeatureMarkerPatch = resourcePatch(
    description = "Mark key shape customization / extended gradations as present."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(KEY_SHAPE_CUSTOMIZATION_FEATURE_MARKER_NAME)
        addExtendedKeyShapes()
    }
}

private const val KEY_SHAPE_CUSTOMIZATION_FEATURE_MARKER_NAME =
    "dev.jason.gboardpatches.feature.key_shape_customization"

context(context: ResourcePatchContext)
private fun addExtendedKeyShapes() = with(context) {
    val resDir = get("res")
    resDir.walkTopDown()
        .filter { f -> f.isFile && f.name.endsWith(".xml") && (f.name.contains("shape") || f.name.contains("key") || f.name.contains("theme")) }
        .forEach { xmlFile ->
            val rel = xmlFile.relativeTo(resDir).invariantSeparatorsPath
            val docPath = "res/$rel"
            document(docPath).use { doc ->
                addShapeOptions(doc)
            }
        }
}

private fun addShapeOptions(doc: org.w3c.dom.Document) {
    val root = doc.documentElement
    // Find the last preference or category in key shape screen and insert custom ones after
    var lastPref: Element? = null
    walkElementNodes(root) { elem ->
        val tag = elem.tagName.lowercase()
        if (tag.contains("preference")) {
            lastPref = elem
        }
    }
    if (lastPref != null && lastPref.parentNode != null) {
        // Add less rounded
        val less = doc.createElement("com.google.android.libraries.inputmethod.settings.widget.ExtendedPreference")
        less.setAttributeNS(ANDROID_NS, "key", "less_rounded")
        less.setAttributeNS(ANDROID_NS, "title", "Weniger gerundet")
        less.setAttributeNS(ANDROID_NS, "summary", "Subtile Rundung")
        lastPref.parentNode.insertBefore(less, lastPref.nextSibling)

        // Add horizontal lines
        val horiz = doc.createElement("com.google.android.libraries.inputmethod.settings.widget.ExtendedPreference")
        horiz.setAttributeNS(ANDROID_NS, "key", "horizontal_lines")
        horiz.setAttributeNS(ANDROID_NS, "title", "Horizontale Linien")
        horiz.setAttributeNS(ANDROID_NS, "summary", "Flach mit Linien")
        lastPref.parentNode.insertBefore(horiz, lastPref.nextSibling)
    }
}

private fun walkElementNodes(node: org.w3c.dom.Node, action: (Element) -> Unit) {
    if (node is Element) action(node)
    val children = node.childNodes
    for (i in 0 until children.length) {
        walkElementNodes(children.item(i), action)
    }
}
