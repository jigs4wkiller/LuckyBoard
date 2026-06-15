package dev.jason.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.signaturebypass.gboardSignatureBypassPatch
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

@Suppress("unused")
val gboardIncognitoEnhancementsPatch = resourcePatch(
    name = "Incognito Enhancements",
    description = "Enable clipboard and voice typing in incognito mode. Also force Gboard to always open in incognito mode to disable typing history collection and personalization. Depends on signature bypass.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardSignatureBypassPatch,
        gboardIncognitoEnhancementsBytecodePatch
    )
}
