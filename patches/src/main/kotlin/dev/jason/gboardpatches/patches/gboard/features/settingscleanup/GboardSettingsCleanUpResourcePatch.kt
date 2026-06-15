package dev.jason.gboardpatches.patches.gboard.features.settingscleanup

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.shared.ANDROID_NS
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD
import org.w3c.dom.Element
import org.w3c.dom.Node

internal val gboardSettingsCleanUpResourcePatch = resourcePatch(
    description = "Removes Help & Feedback, Info, Rate and Privacy categories from Gboard's main settings. " +
            "For the Privacy entry, all usage statistics / diagnostics sharing sub-options are first disabled (set to false) " +
            "before the parent category is hidden."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applySettingsCleanUp()
    }
}

context(context: ResourcePatchContext)
private fun applySettingsCleanUp() = with(context) {
    val resDir = get("res")
    // Only target the actual Gboard settings XML files identified from decompiled APK.
    // We saw the bottom category (with our injected Patches entry) in settings.xml and settings_legacy.xml.
    // Privacy subs live in setting_privacy.xml.
    // This avoids touching random xml_0x* files (which are layouts, drawables, etc.), which was causing
    // "Unexpected end of document" SAX errors in ResourceIdProcessor during Morphe's resource re-encoding.
    listOf("res/xml/settings.xml", "res/xml/settings_legacy.xml", "res/xml/setting_privacy.xml", "res/xml/setting_about.xml")
        .forEach { docPath ->
            if (get(docPath).exists()) {
                document(docPath).use { doc ->
                    cleanDocument(doc, docPath)
                }
            }
        }
}

private fun cleanDocument(doc: org.w3c.dom.Document, docPath: String) {
    val root = doc.documentElement

    if (docPath.endsWith("settings.xml") || docPath.endsWith("settings_legacy.xml")) {
        // Precise removal based on decompiled APK structure (from provided tar.xz):
        // In the bottom PreferenceCategory (the one with our "gboard_patches_entry"),
        // remove the native entries for Privacy/Datenschutz, Hilfe&Feedback, Rate/Bewerten, Info/About.
        // Keep our Patches entry and footer.
        // Use stable class names (HeaderPreference, RateUsPreference) for robustness across Gboard versions.
        walkElementNodes(root) { elem ->
            if (elem.tagName == "androidx.preference.PreferenceCategory") {
                var hasPatches = false
                val childrenToRemove = mutableListOf<Element>()
                val children = elem.childNodes
                for (i in 0 until children.length) {
                    val child = children.item(i)
                    if (child is Element && isPreferenceLike(child)) {
                        val key = getAttr(child, "key")
                        if (key == "gboard_patches_entry") {
                            hasPatches = true
                        } else {
                            val tag = child.tagName
                            if (tag.contains("HeaderPreference") || tag.contains("RateUsPreference")) {
                                // These are the native ones to remove (Privacy, Hilfe, Rate, Info etc. in this category)
                                childrenToRemove.add(child)
                            }
                        }
                    }
                }
                if (hasPatches) {
                    childrenToRemove.forEach { elem.removeChild(it) }
                }
            }
        }
    }

    // Also remove by exact decompiled keys in case the class based misses some (for robustness)
    val keysToRemove = setOf("0x7f140aca", "0x7f140acc", "0x7f140acb", "0x7f140abb", "0x7f140ac4")
    walkElementNodes(root) { elem ->
        if (isPreferenceLike(elem)) {
            val key = getAttr(elem, "key") ?: ""
            if (keysToRemove.any { key.contains(it) }) {
                elem.parentNode?.removeChild(elem)
            }
        }
    }

    if (docPath.endsWith("setting_privacy.xml")) {
        // Disable all usage/stats sharing related switches in the privacy subs (before parent hidden).
        // Keys from decompiled setting_privacy.xml.
        walkElementNodes(root) { elem ->
            if (isPreferenceLike(elem)) {
                val k = getAttr(elem, "key") ?: ""
                if (k.contains("0x7f14097b") || k.contains("0x7f140aa2") || k.contains("0x7f140b02") ||
                    k.contains("0x7f14097d") || k.contains("usage") || k.contains("stat") || k.contains("metric") ||
                    k.contains("share") || k.contains("diagnos") || k.contains("crash") || k.contains("telemetry")) {
                    elem.setAttributeNS(ANDROID_NS, "defaultValue", "false")
                    elem.setAttributeNS(ANDROID_NS, "enabled", "false")
                }
            }
        }
    }

    if (docPath.endsWith("setting_about.xml")) {
        // Clean any redundant Help/Rate/Info links if present in about screen.
        val toRemove = mutableListOf<Element>()
        walkElementNodes(root) { elem ->
            if (isPreferenceLike(elem)) {
                val t = (getAttr(elem, "title") ?: "").lowercase()
                val k = (getAttr(elem, "key") ?: "").lowercase()
                if (t.contains("hilfe") || t.contains("feedback") || t.contains("bewert") || t.contains("rate") ||
                    t.contains("info") || k.contains("about") || k.contains("help")) {
                    toRemove.add(elem)
                }
            }
        }
        toRemove.forEach { it.parentNode?.removeChild(it) }
    }

    // Fallback general removal for any remaining XMLs.
    val nodesToProcess = mutableListOf<Element>()
    val privacyNodes = mutableListOf<Element>()
    walkElementNodes(root) { elem ->
        if (isPreferenceLike(elem)) {
            val title = getAttr(elem, "title") ?: ""
            val key = getAttr(elem, "key") ?: ""
            val tl = title.lowercase()
            val kl = key.lowercase()
            if (tl.contains("datenschutz") || tl.contains("privacy") || kl.contains("privacy") || kl.contains("datenschutz")) {
                privacyNodes.add(elem)
            }
            if (tl.contains("hilfe") || tl.contains("feedback") || tl.contains("help") || tl.contains("info") ||
                tl.contains("bewerten") || tl.contains("rate") || tl.contains("datenschutz") || tl.contains("privacy") ||
                kl.contains("feedback") || kl.contains("help") || kl.contains("about") || kl.contains("rate") || kl.contains("privacy")) {
                nodesToProcess.add(elem)
            }
        }
    }
    for (p in privacyNodes) disableUsageStatsInSubtree(p)
    for (n in nodesToProcess) n.parentNode?.removeChild(n)
}

private fun disableUsageStatsInSubtree(root: Element) {
    walkElementNodes(root) { elem ->
        if (isPreferenceLike(elem)) {
            val key = getAttr(elem, "key")?.lowercase() ?: ""
            if (key.contains("usage") || key.contains("stat") || key.contains("metric") ||
                key.contains("share") || key.contains("diagnos") || key.contains("crash") ||
                key.contains("telemetry") || key.contains("feedback") || key.contains("collect")) {
                elem.setAttributeNS(ANDROID_NS, "defaultValue", "false")
                // Keep visible in sub but since parent hidden, or to be sure also disable
                // elem.setAttributeNS(ANDROID_NS, "enabled", "false")
            }
        }
    }
}

private fun isPreferenceLike(elem: Element): Boolean {
    val tag = elem.tagName.lowercase()
    return tag.contains("preference") || tag.contains("category") || tag.contains("extended")
}

private fun getAttr(elem: Element, name: String): String? {
    var v = elem.getAttributeNS(ANDROID_NS, name)
    if (v.isEmpty()) v = elem.getAttribute("android:$name")
    if (v.isEmpty()) v = elem.getAttribute(name)
    return if (v.isEmpty()) null else v
}

private fun walkElementNodes(node: Node, action: (Element) -> Unit) {
    if (node is Element) action(node)
    val children = node.childNodes
    for (i in 0 until children.length) {
        walkElementNodes(children.item(i), action)
    }
}
