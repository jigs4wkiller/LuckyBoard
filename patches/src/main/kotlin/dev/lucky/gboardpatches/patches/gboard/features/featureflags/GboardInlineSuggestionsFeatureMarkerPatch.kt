package dev.lucky.gboardpatches.patches.gboard.features.featureflags

import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardInlineSuggestionsFeatureMarkerPatch = resourcePatch(
    description = "標記 inline suggestions rollout flag patch 已打入 target APK"
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(INLINE_SUGGESTIONS_FEATURE_MARKER_NAME)
    }
}

private const val INLINE_SUGGESTIONS_FEATURE_MARKER_NAME =
    "dev.lucky.gboardpatches.feature.inline_suggestions"
