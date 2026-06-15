package dev.lucky.gboardpatches.patches.gboard.features.packagerename

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.gboard.shared.elements
import dev.lucky.gboardpatches.patches.shared.Constants.GBOARD_PACKAGE_NAME
import dev.lucky.gboardpatches.patches.shared.Constants.GBOARD_PATCHED_PACKAGE_NAME
import dev.lucky.gboardpatches.patches.shared.Constants.LUCKYBOARD_APP_NAME

internal val gboardPackageRenameResourcePatch = resourcePatch(
    description = "Rename the package to allow side-by-side installation with the official Gboard and rename the app to \"LuckyBoard\"."
) {
    finalize {
        applyManifestPackageOverride(
            originalPackageName = GBOARD_PACKAGE_NAME,
            packageNameOverride = GBOARD_PATCHED_PACKAGE_NAME
        )
        // Additionally rename the visible app name (launcher/settings) to LuckyBoard.
        applyAppNameRename(LUCKYBOARD_APP_NAME)
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
private fun applyAppNameRename(appName: String) = with(context) {
    document("AndroidManifest.xml").use { document ->
        // Set the app label on all <application> elements (usually one).
        // This renames the app to "LuckyBoard" in the launcher and settings.
        document.getElementsByTagName("application").elements().forEach { element ->
            element.setAttribute("android:label", appName)
        }
    }
}
