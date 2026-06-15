package dev.lucky.gboardpatches.patches.gboard.features.featureflags

import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardKeyShapeSelectionFeatureMarkerPatch = resourcePatch(
    description = "Mark key shape selection feature as present (enables picker in Theme details)."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(KEY_SHAPE_SELECTION_FEATURE_MARKER_NAME)
    }
}

private const val KEY_SHAPE_SELECTION_FEATURE_MARKER_NAME =
    "dev.lucky.gboardpatches.feature.key_shape_selection"
