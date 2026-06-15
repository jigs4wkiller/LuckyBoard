package dev.lucky.gboardpatches.patches.gboard.features.addsymbols

import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardCustomSymbolsFeatureMarkerPatch = resourcePatch(
    description = "標記 custom symbols feature 已被打入 target APK，共用 settings UI 過濾"
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(CUSTOM_SYMBOLS_FEATURE_MARKER_NAME)
    }
}

private const val CUSTOM_SYMBOLS_FEATURE_MARKER_NAME =
    "dev.lucky.gboardpatches.feature.custom_symbols"
