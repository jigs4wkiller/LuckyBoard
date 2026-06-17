package dev.lucky.gboardpatches.patches.gboard.features.luckify

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import dev.lucky.gboardpatches.patches.gboard.shared.elements
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD
import dev.lucky.gboardpatches.patches.shared.Constants.GBOARD_PACKAGE_NAME
import dev.lucky.gboardpatches.patches.shared.Constants.GBOARD_PATCHED_PACKAGE_NAME
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File

@Suppress("unused")
val gboardLuckifyResourcePatch = resourcePatch(
    name = "Luckify Gboard",
    description = "Rename the package and replace all Gboard branding with a custom app name (default: LuckyBoard). Allows side-by-side installation with the official Gboard.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    val appName = stringOption(
        key = "app_name",
        title = "App name",
        description = "The display name for the patched app (launcher, settings, all UI strings). Default: LuckyBoard",
        default = "LuckyBoard"
    )

    finalize {
        val name = appName.value?.trim()?.ifEmpty { "LuckyBoard" } ?: "LuckyBoard"

        applyManifestPackageOverride(
            originalPackageName = GBOARD_PACKAGE_NAME,
            packageNameOverride = GBOARD_PATCHED_PACKAGE_NAME
        )
        applyAppNameLabel(name)
        applyBrandRename(name)
    }
}

context(context: ResourcePatchContext)
private fun applyManifestPackageOverride(
    originalPackageName: String,
    packageNameOverride: String
) = with(context) {
    document("AndroidManifest.xml").use { document ->
        val manifest = document.documentElement
        if (manifest.getAttribute("package") == originalPackageName) {
            manifest.setAttribute("package", packageNameOverride)
        }

        document.getElementsByTagName("*").elements().forEach { element ->
            val attributes = element.attributes
            for (index in 0 until attributes.length) {
                val attribute = attributes.item(index)
                if (element === manifest && attribute.nodeName == "package") continue

                val updatedValue = attribute.nodeValue?.replace(originalPackageName, packageNameOverride)
                if (updatedValue != null && updatedValue != attribute.nodeValue) {
                    attribute.nodeValue = updatedValue
                }
            }
        }
    }
}

context(context: ResourcePatchContext)
private fun applyAppNameLabel(appName: String) = with(context) {
    document("AndroidManifest.xml").use { document ->
        document.getElementsByTagName("application").elements().forEach { element ->
            element.setAttribute("android:label", appName)
        }
    }
}

context(context: ResourcePatchContext)
private fun applyBrandRename(appName: String) = with(context) {
    document("AndroidManifest.xml").use { doc ->
        replaceGboardBrand(doc, appName)
    }

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
                replaceGboardBrand(doc, appName)
            }
        }
}

private fun replaceGboardBrand(document: Document, appName: String) {
    document.getElementsByTagName("*").elements().forEach { element ->
        val attributes = element.attributes
        for (i in 0 until attributes.length) {
            val attr = attributes.item(i)
            val value = attr.nodeValue ?: continue
            if ("Gboard" !in value) continue

            val attrName = attr.localName?.lowercase() ?: attr.nodeName.lowercase()

            if (attrName == "name" && value.contains(".")) continue
            if (attrName == "authorities" && value.contains(".")) continue
            if (attrName == "targetClass" && value.contains(".")) continue
            if (attrName == "fragment" && value.contains(".")) continue
            if (attrName == "backupAgent" && value.contains(".")) continue

            attr.nodeValue = value.replace("Gboard", appName)
        }
    }

    replaceTextInNode(document.documentElement, appName)
}

private fun replaceTextInNode(node: Node?, appName: String) {
    if (node == null) return
    if (node.nodeType == Node.TEXT_NODE) {
        val text = node.nodeValue
        if (text != null && "Gboard" in text) {
            node.nodeValue = text.replace("Gboard", appName)
        }
    } else if (node.nodeType == Node.ELEMENT_NODE || node.nodeType == Node.DOCUMENT_NODE) {
        val children = node.childNodes
        for (i in 0 until children.length) {
            replaceTextInNode(children.item(i), appName)
        }
    }
}
