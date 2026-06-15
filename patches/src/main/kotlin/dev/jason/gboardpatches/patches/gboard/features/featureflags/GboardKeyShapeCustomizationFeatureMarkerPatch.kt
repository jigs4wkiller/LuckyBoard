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
    // Restrict to a small whitelist of known preference/settings files (like Settings Clean-Up does).
    // Broad walks over every "setting|preference|theme|key" XML caused bloat and risked parse problems
    // in resource re-encoding (similar to previous SAX issues). The actual key shape list in the
    // Theme details picker is primarily driven by the forced flags (more_pill_keys + the two
    // belka_rounded / enable_rounded ones) + marker. This XML part is best-effort for any static
    // shape preference lists that exist.
    listOf(
        "res/xml/setting_preferences.xml",
        "res/xml/settings.xml",
        "res/xml/settings_legacy.xml",
        "res/xml/setting_physical_keyboard.xml",
        "res/xml/setting_theme.xml",
        "res/xml/xml_0x7f170002.xml",  // from decompile contained belka_rounded references
        // Previously successful insertion targets (from decompile of prior patched APKs) to ensure
        // the custom less_rounded / horizontal_lines ExtendedPreferences get declared where Gboard
        // shape/keyboard prefs are defined. The flag forcing is the main enabler; this helps UI.
        "res/xml/setting_japanese_12keys.xml",
        "res/xml/setting_japanese_softwarekeyboard.xml"
    ).forEach { docPath ->
        if (get(docPath).exists()) {
            document(docPath).use { doc ->
                addShapeOptions(doc)
            }
        }
    }
}

private fun addShapeOptions(doc: org.w3c.dom.Document) {
    val root = doc.documentElement
    // Target key shape related screens (from decompile analysis of shape/theme XMLs and flags).
    // Insert custom less-rounded and horizontal-lines shapes after existing ones.
    // This complements the more_pill_keys flag (which adds stock rounded gradations) + customization marker.
    var lastRelevantPref: Element? = null
    walkElementNodes(root) { elem ->
        val tag = elem.tagName.lowercase()
        if (tag.contains("preference") && (
            (getAttr(elem, "key")?.contains("rounded") == true) ||
            (getAttr(elem, "key")?.contains("shape") == true) ||
            (getAttr(elem, "title")?.lowercase()?.contains("shape") == true) ||
            (getAttr(elem, "title")?.lowercase()?.contains("rounded") == true)
        )) {
            lastRelevantPref = elem
        }
    }
    if (lastRelevantPref != null && lastRelevantPref.parentNode != null) {
        // Only add our extensions if we found a real rounded/shape preference (no blind fallback to random last pref).
        // Extended shapes: less rounded (subtle curve) and horizontal lines (step to flat).
        val less = doc.createElement("com.google.android.libraries.inputmethod.settings.widget.ExtendedPreference")
        less.setAttributeNS(ANDROID_NS, "key", "less_rounded")
        less.setAttributeNS(ANDROID_NS, "title", "Less rounded")
        less.setAttributeNS(ANDROID_NS, "summary", "Subtle curve")
        lastRelevantPref.parentNode.insertBefore(less, lastRelevantPref.nextSibling)

        val horiz = doc.createElement("com.google.android.libraries.inputmethod.settings.widget.ExtendedPreference")
        horiz.setAttributeNS(ANDROID_NS, "key", "horizontal_lines")
        horiz.setAttributeNS(ANDROID_NS, "title", "Horizontal lines")
        horiz.setAttributeNS(ANDROID_NS, "summary", "Flat with lines")
        lastRelevantPref.parentNode.insertBefore(horiz, lastRelevantPref.nextSibling)
    }
}

private fun walkElementNodes(node: org.w3c.dom.Node, action: (Element) -> Unit) {
    if (node is Element) action(node)
    val children = node.childNodes
    for (i in 0 until children.length) {
        walkElementNodes(children.item(i), action)
    }
}

private fun getAttr(elem: Element, name: String): String? {
    var v = elem.getAttributeNS(ANDROID_NS, name)
    if (v.isEmpty()) v = elem.getAttribute("android:$name")
    if (v.isEmpty()) v = elem.getAttribute(name)
    return if (v.isEmpty()) null else v
}
