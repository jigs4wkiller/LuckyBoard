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
    resDir.walkTopDown()
        .filter { f -> f.isFile && f.name.endsWith(".xml") }
        .forEach { xmlFile ->
            val rel = xmlFile.relativeTo(resDir).invariantSeparatorsPath
            val docPath = "res/$rel"
            document(docPath).use { doc ->
                cleanDocument(doc)
            }
        }
}

private fun cleanDocument(doc: org.w3c.dom.Document) {
    val root = doc.documentElement

    // Special handling for main settings.xml and legacy: find the category containing our Patches entry
    // and remove the other native bottom entries (Privacy, Hilfe&Feedback, Rate, Info/About) 
    // while keeping Patches and Footer.
    // Keys taken from decompiled patched APK (settings.xml).
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
                    } else if (key != null && (key.contains("0x7f140aca") || key.contains("0x7f140acb") || 
                            key.contains("0x7f140abb") || key.contains("0x7f140acc") || key.contains("0x7f140ac4") ||
                            key.contains("0x7f140ad1") /* extra hilfe/info if present */ )) {
                        // Privacy (datenschutz), Rate (bewerten), About/Info, Hilfe&Feedback ones (from decompile)
                        childrenToRemove.add(child)
                    }
                }
            }
            if (hasPatches) {
                for (child in childrenToRemove) {
                    elem.removeChild(child)
                }
            }
        }
    }

    // General walk for other settings files and privacy subs
    val nodesToProcess = mutableListOf<Element>()
    val privacyNodes = mutableListOf<Element>()

    walkElementNodes(root) { elem ->
        if (isPreferenceLike(elem)) {
            val title = getAttr(elem, "title")
            val key = getAttr(elem, "key")
            val titleL = title?.lowercase() ?: ""
            val keyL = key?.lowercase() ?: ""

            if (titleL.contains("datenschutz") || titleL.contains("privacy") ||
                keyL.contains("privacy") || keyL.contains("datenschutz") ||
                keyL.contains("usage") || keyL.contains("diagnostics")) {
                privacyNodes.add(elem)
            }

            if (titleL.contains("hilfe") || titleL.contains("feedback") || titleL.contains("help") ||
                titleL.contains("info") ||
                titleL.contains("bewerten") || titleL.contains("rate") || titleL.contains("bewert") ||
                titleL.contains("datenschutz") || titleL.contains("privacy") ||
                keyL.contains("feedback") || keyL.contains("help") || keyL.contains("about") ||
                keyL.contains("info") || keyL.contains("rate") || keyL.contains("privacy") || keyL.contains("datenschutz") ||
                keyL.contains("usage") || keyL.contains("diagnostics") || keyL.contains("help_and_feedback")) {
                nodesToProcess.add(elem)
            }
        }
    }

    // First disable sub-options in privacy categories/screens
    for (p in privacyNodes) {
        disableUsageStatsInSubtree(p)
    }

    // Remove the matching categories/entries (for other files)
    for (node in nodesToProcess) {
        if (node.parentNode != null) {
            node.parentNode.removeChild(node)
        }
    }
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
