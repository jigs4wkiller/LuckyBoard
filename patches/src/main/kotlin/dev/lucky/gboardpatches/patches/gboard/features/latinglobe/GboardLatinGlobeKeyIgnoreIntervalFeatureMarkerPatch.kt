package dev.lucky.gboardpatches.patches.gboard.features.latinglobe

import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardLatinGlobeKeyIgnoreIntervalFeatureMarkerPatch = resourcePatch(
    description = "Marker for the Latin globe key ignore interval feature being applied; used for settings UI availability filtering."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(LATIN_GLOBE_KEY_IGNORE_INTERVAL_FEATURE_MARKER_NAME)
    }
}

private const val LATIN_GLOBE_KEY_IGNORE_INTERVAL_FEATURE_MARKER_NAME =
    "dev.lucky.gboardpatches.feature.latin_globe_key_ignore_interval"
