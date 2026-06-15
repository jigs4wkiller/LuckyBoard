package dev.lucky.gboardpatches.patches.gboard.features.featureflags

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.gboard.shared.ANDROID_NS
import dev.lucky.gboardpatches.patches.gboard.shared.childElements
import dev.lucky.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

// gboardFeatureFlagsBytecodePatch (and applyFeatureMarker) are in the same package and directly visible.

/**
 * Comprehensive patch that enables the in-app Feature Flags menu inside LuckyBoard settings.
 * This gives runtime control (gear icon) over many internal rollout/experimental feature flags
 * for the supported Gboard version (17.0.10.880768217-release-arm64-v8a and similar).
 *
 * All supported flags get their markers written when this patch is active.
 * The in-app menu then lets the user individually enable/disable each flag at runtime.
 * (No per-flag options at patch time anymore – the in-app menu replaces that.)
 *
 * Flags are forced at runtime via the existing feature flag hook when their corresponding
 * marker meta-data is present in the patched APK.
 */
@Suppress("unused")
val gboardFeatureFlagsPatch = resourcePatch(
    name = "Feature Flags",
    description = "Enable the in-app Feature Flags menu (under Patches/LuckyBoard settings) for runtime toggling of many hidden/experimental rollout flags. Includes key shapes, accessory/voice widgets, OCR, dictation redesign, modern themes (Silk/Material 3), keyboard height, AI smart reply, and more. Great for power users. All flags start disabled and must be explicitly enabled inside the app.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesExtensionCarrierPatch,
        gboardFeatureFlagsBytecodePatch
    )

    // No per-flag options at patch time anymore.
    // With the in-app Flags menu (LuckyBoard Settings), all flags can be toggled at runtime.
    // When this patch is active we always enable the in-app UI section and make every flag's marker present
    // so they appear in the menu (and can be individually enabled/disabled there).
    // Flags start DISABLED by default in the menu; the user explicitly turns them on inside the app.

    finalize {
        // Always insert the Feature Flags section into LuckyBoard's Patches settings when this patch is selected.
        applyFeatureMarker("dev.lucky.gboardpatches.feature.feature_flags_ui")

        // Always make all the individual flags available (markers) so the in-app menu shows them all.
        // The runtime will respect the per-flag toggles from the in-app prefs.
        applyFeatureMarker("dev.lucky.gboardpatches.feature.key_shape_selection")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.key_shape_customization")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_support_accessory_keyboard")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_enable_voice_widget")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_enable_new_language_search_bar")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_enable_settings_two_pane")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_enable_ocr")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_enable_dictation_redesign")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_show_collapse_button")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_enable_adjust_default_keyboard_height")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_silk_theme")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_material3_theme")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_silk_popup")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_enable_ai_core_smart_reply")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_keyboard_redesign_google_sans")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.flag_bright_key_on_dynamic_color_dark_theme")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.grammar_checker")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.inline_suggestions")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.clipboard_entity_extraction")
        applyFeatureMarker("dev.lucky.gboardpatches.feature.clipboard_item_edit")
    }
}

// The applyFeatureMarker and supporting extensions are provided by GboardFeatureFlagMarkerPatchUtils (same package).