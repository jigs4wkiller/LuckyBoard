package dev.lucky.gboardpatches.patches.gboard.features.debug

import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal const val DEBUG_FEATURE_MARKER =
    "dev.lucky.gboardpatches.feature.debug_patches"

@Suppress("unused")
val gboardDebugPatch = resourcePatch(
    name = "Debug Patches",
    description = "Adds a Debug helper menu item to Patches settings for copying app logs.",
    default = false
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(DEBUG_FEATURE_MARKER)
    }
}
