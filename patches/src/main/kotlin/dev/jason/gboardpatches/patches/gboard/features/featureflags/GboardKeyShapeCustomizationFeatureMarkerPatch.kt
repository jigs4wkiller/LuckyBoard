package dev.jason.gboardpatches.patches.gboard.features.featureflags

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

/**
 * Enables extended key shape customization / additional gradations in the theme key shape picker.
 * This unlocks less-rounded options and horizontal-line style keys (as steps between flat and the
 * rounded/pill shapes).
 */
internal val gboardKeyShapeCustomizationFeatureMarkerPatch = resourcePatch(
    description = "Mark key shape customization / extended gradations as present."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(KEY_SHAPE_CUSTOMIZATION_FEATURE_MARKER_NAME)
    }
}

private const val KEY_SHAPE_CUSTOMIZATION_FEATURE_MARKER_NAME =
    "dev.jason.gboardpatches.feature.key_shape_customization"
