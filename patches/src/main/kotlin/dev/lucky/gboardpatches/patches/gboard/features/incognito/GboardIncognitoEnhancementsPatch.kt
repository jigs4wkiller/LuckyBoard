package dev.lucky.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.gboard.registry.gboardSignatureBypassPatch
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD
import java.io.File

private const val INCOGNITO_ASSET_ROOT = "luckify-assets"
private const val INCOGNITO_DRAWABLE_ID = "0x7f080691"

@Suppress("unused")
val gboardIncognitoEnhancementsPatch = resourcePatch(
    name = "Incognito Enhancements",
    description = "Enable clipboard and voice typing in incognito mode. Also force Gboard to always open in incognito mode to disable typing history collection and personalization. (Ported from Adobo patches by jkennethcarino)",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardSignatureBypassPatch,
        gboardIncognitoEnhancementsBytecodePatch
    )

    finalize {
        replaceIncognitoDrawable()
    }
}

context(context: ResourcePatchContext)
private fun replaceIncognitoDrawable() = with(context) {
    val targetFile = get("res/BVL.xml")
    if (!targetFile.exists()) {
        println("  [Incognito] res/BVL.xml not found, skipping replacement")
        return@with
    }

    val resourceClassLoader = object {}.javaClass.classLoader
    val assetPath = "$INCOGNITO_ASSET_ROOT/drawable_$INCOGNITO_DRAWABLE_ID.xml"
    val assetBytes = resourceClassLoader.getResourceAsStream(assetPath)?.use { it.readBytes() }
    if (assetBytes != null) {
        println("  [Incognito] Replacing res/BVL.xml with custom incognito drawable")
        targetFile.writeBytes(assetBytes)
    }
}
