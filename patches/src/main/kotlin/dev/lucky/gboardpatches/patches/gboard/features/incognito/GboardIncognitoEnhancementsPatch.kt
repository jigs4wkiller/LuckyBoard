package dev.lucky.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.gboard.registry.gboardSignatureBypassPatch
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

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
}
