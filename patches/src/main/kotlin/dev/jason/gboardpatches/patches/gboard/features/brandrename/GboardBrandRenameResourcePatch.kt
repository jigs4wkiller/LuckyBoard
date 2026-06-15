package dev.jason.gboardpatches.patches.gboard.features.brandrename

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.shared.elements
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File

internal val gboardBrandRenameResourcePatch = resourcePatch(
    description = "Replace occurrences of the Gboard brand name with LuckyBoard in the app resources."
) {
    finalize {
        applyBrandRename()
    }
}

context(context: ResourcePatchContext)
private fun applyBrandRename() = with(context) {
    // Process manifest (catches any labels, activity names etc. that still reference Gboard branding).
    // Package rename already forces the main app label, but this ensures full coverage.
    document("AndroidManifest.xml").use { doc ->
        replaceGboardBrand(doc)
    }

    // Walk all locale strings.xml files (res/values/strings.xml + res/values-*/strings.xml).
    // This ensures "Gboard" is replaced with "LuckyBoard" in *all* UI strings and in every language.
    val resDir: File = get("res")
    resDir.walkTopDown()
        .filter { f ->
            f.isFile &&
                f.name == "strings.xml" &&
                (f.parentFile?.name?.startsWith("values") == true)
        }
        .forEach { stringsFile ->
            val rel = stringsFile.relativeTo(resDir).invariantSeparatorsPath
            val docPath = "res/$rel"
            document(docPath).use { doc ->
                replaceGboardBrand(doc)
            }
        }
}

private fun replaceGboardBrand(document: Document) {
    // Replace "Gboard" inside every attribute value (e.g. android:label, android:title, android:summary etc.)
    document.getElementsByTagName("*").elements().forEach { element ->
        val attributes = element.attributes
        for (i in 0 until attributes.length) {
            val attr = attributes.item(i)
            val value = attr.nodeValue
            if (value != null && "Gboard" in value) {
                attr.nodeValue = value.replace("Gboard", "LuckyBoard")
            }
        }
    }

    // Recursively replace inside all text nodes (the actual string resource bodies).
    replaceTextInNode(document.documentElement)
}

private fun replaceTextInNode(node: Node?) {
    if (node == null) return
    if (node.nodeType == Node.TEXT_NODE) {
        val text = node.nodeValue
        if (text != null && "Gboard" in text) {
            node.nodeValue = text.replace("Gboard", "LuckyBoard")
        }
    } else if (node.nodeType == Node.ELEMENT_NODE || node.nodeType == Node.DOCUMENT_NODE) {
        val children = node.childNodes
        for (i in 0 until children.length) {
            replaceTextInNode(children.item(i))
        }
    }
}
