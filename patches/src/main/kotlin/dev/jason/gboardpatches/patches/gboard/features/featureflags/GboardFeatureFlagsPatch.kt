package dev.jason.gboardpatches.patches.gboard.features.featureflags

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.shared.ANDROID_NS
import dev.jason.gboardpatches.patches.gboard.shared.childElements
import dev.jason.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

// gboardFeatureFlagsBytecodePatch (and applyFeatureMarker) are in the same package and directly visible.

/**
 * Comprehensive patch exposing many internal rollout/experimental feature flags
 * as individual boolean options (gear icon in Morphe).
 *
 * This allows fine-grained control over hidden features for the supported version
 * (17.0.10.880768217-release-arm64-v8a and similar).
 *
 * Flags are forced at runtime via the existing feature flag hook when their corresponding
 * marker meta-data is present in the patched APK.
 */
@Suppress("unused")
val gboardFeatureFlagsPatch = resourcePatch(
    name = "Feature Flags",
    description = "Toggle many hidden/experimental rollout flags individually (use the gear icon). Includes key shapes, accessory/voice widgets, OCR scan text, dictation redesign, settings UI improvements, language search, collapsible keyboard, and more. Great for power users.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesExtensionCarrierPatch,
        gboardFeatureFlagsBytecodePatch
    )

    // Top-level option: if enabled, injects a "Feature Flags" entry into LuckyBoard's Patches settings
    // where the user can toggle each flag at runtime (with descriptions).
    // Important: Flags start DISABLED (not forced) even if selected below. The user must explicitly
    // turn them ON in the in-app Lucky Settings UI for them to become active.
    val addFlagsToLuckySettings = booleanOption(
        key = "add_flags_to_lucky_settings",
        title = "Add flags to Lucky Settings",
        description = "Insert a Feature Flags section into the settings (under Patches/LuckyBoard). There you can toggle every flag on/off at runtime. Each flag has a short description. The individual options below decide which flags appear in the UI (they start off by default and must be manually enabled in settings to activate).",
        default = true
    )

    // Key / already known
    val enableMorePillKeys = booleanOption(
        key = "more_pill_keys",
        title = "Key Shape Selection (more_pill_keys)",
        description = "Enable the Key shape picker in Theme details and additional rounded gradations (and our custom less-rounded + horizontal lines when combined with the Key Shape patch).",
        default = true
    )

    // Cool new ones from research (Rboard / GMS-Flags / mod communities for ~v17)
    val enableAccessoryKeyboard = booleanOption(
        key = "support_accessory_keyboard",
        title = "Accessory / Floating Keyboard",
        description = "Support for accessory keyboard features, floating keyboard elements, and related toolbar widgets.",
        default = true
    )

    val enableVoiceWidget = booleanOption(
        key = "enable_voice_widget",
        title = "Voice Widget / Toolbar",
        description = "Enable enhanced voice dictation widget and toolbar integration (pairs well with accessory keyboard flags).",
        default = true
    )

    val enableNewLanguageSearch = booleanOption(
        key = "enable_new_language_search_bar",
        title = "New Language Search Bar",
        description = "Improved language / input method search bar in the settings.",
        default = true
    )

    val enableTwoPaneSettings = booleanOption(
        key = "enable_settings_two_pane",
        title = "Two-Pane Settings UI",
        description = "Enable two-pane / split view layout for settings on larger screens or tablets.",
        default = true
    )

    val enableOcr = booleanOption(
        key = "enable_ocr",
        title = "OCR / Scan Text",
        description = "Add a 'Scan text' tool/tile (camera OCR) to the toolbar for recognizing and inserting text from photos. May need additional language tags in some setups.",
        default = true
    )

    val enableDictationRedesign = booleanOption(
        key = "enable_regular_dictation_redesign",
        title = "Voice Dictation Redesign",
        description = "Updated voice dictation UI and background treatment for the microphone/voice input.",
        default = true
    )

    val enableCollapseButton = booleanOption(
        key = "show_collapse_button",
        title = "Collapse Keyboard Button",
        description = "Show a button to collapse/minimize the keyboard (useful with floating or accessory modes).",
        default = true
    )

    // Additional good flags discovered by inspecting the decompiled Gboard APK (mainly pdk.smali flag registrations
    // + other smali). Sourced from Rboard/GboardThemes community patterns for useful experimental/power-user features
    // (keyboard sizing, modern Silk/Material3 theming, AI, redesigns, etc.). Keep in sync with runtime map and settings.
    val enableAdjustKeyboardHeight = booleanOption(
        key = "enable_adjust_default_keyboard_height",
        title = "Adjust Default Keyboard Height",
        description = "Allow fine-grained adjustment of the default keyboard height (useful for different screen sizes or preferences).",
        default = true
    )

    val enableSilkTheme = booleanOption(
        key = "silk_theme",
        title = "Silk Theme",
        description = "Enable Silk theme effects and smooth animations (Material You related).",
        default = true
    )

    val enableMaterial3Theme = booleanOption(
        key = "material3_theme",
        title = "Material 3 Theme",
        description = "Enable Material 3 theming support for Gboard.",
        default = true
    )

    val enableSilkPopup = booleanOption(
        key = "silk_popup",
        title = "Silk Popups",
        description = "Use Silk style for suggestion popups and UI elements.",
        default = true
    )

    val enableAiSmartReply = booleanOption(
        key = "enable_ai_core_smart_reply",
        title = "AI Core Smart Reply",
        description = "Enable AI-powered smart reply suggestions.",
        default = true
    )

    val enableKeyboardRedesignGoogleSans = booleanOption(
        key = "keyboard_redesign_google_sans",
        title = "Keyboard Redesign (Google Sans)",
        description = "Use Google Sans font in keyboard redesign / modern layouts.",
        default = true
    )

    val enableBrightKeyOnDynamicColor = booleanOption(
        key = "bright_key_on_dynamic_color_dark_theme",
        title = "Bright Keys on Dynamic Color Dark Theme",
        description = "Use brighter key colors on dynamic color dark themes.",
        default = true
    )

    // Additional from existing project for consolidation (grammar, suggestions, clipboard entity etc.)
    val enableGrammarChecker = booleanOption(
        key = "enable_grammar_checker",
        title = "Grammar Checker",
        description = "Enable Text correction > Grammar check (existing flag).",
        default = true
    )

    val enableInlineSuggestions = booleanOption(
        key = "enable_inline_suggestions_on_client_side",
        title = "Inline Suggestions",
        description = "Enable Text correction > Smart Compose / inline suggestions (existing flag).",
        default = true
    )

    val enableClipboardEntity = booleanOption(
        key = "enable_clipboard_entity_extraction",
        title = "Clipboard Entity Extraction",
        description = "Enable Clipboard > Show addresses, phones etc. from copied text (existing flag).",
        default = true
    )

    val enableClipboardItemEdit = booleanOption(
        key = "enable_clipboard_text_editor",
        title = "Clipboard Item Edit",
        description = "Enable Edit when long-pressing a clipboard item (existing flag).",
        default = true
    )

    finalize {
        // Set the marker meta-data ONLY for enabled options.
        // The bytecode patch + Runtime will then force the corresponding internal flag to true.
        if (java.lang.Boolean.TRUE == addFlagsToLuckySettings.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.feature_flags_ui")
        }
        if (java.lang.Boolean.TRUE == enableMorePillKeys.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.key_shape_selection")
            applyFeatureMarker("dev.jason.gboardpatches.feature.key_shape_customization")
        }
        if (java.lang.Boolean.TRUE == enableAccessoryKeyboard.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_support_accessory_keyboard")
        }
        if (java.lang.Boolean.TRUE == enableVoiceWidget.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_enable_voice_widget")
        }
        if (java.lang.Boolean.TRUE == enableNewLanguageSearch.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_enable_new_language_search_bar")
        }
        if (java.lang.Boolean.TRUE == enableTwoPaneSettings.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_enable_settings_two_pane")
        }
        if (java.lang.Boolean.TRUE == enableOcr.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_enable_ocr")
        }
        if (java.lang.Boolean.TRUE == enableDictationRedesign.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_enable_dictation_redesign")
        }
        if (java.lang.Boolean.TRUE == enableCollapseButton.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_show_collapse_button")
        }
        if (java.lang.Boolean.TRUE == enableAdjustKeyboardHeight.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_enable_adjust_default_keyboard_height")
        }
        if (java.lang.Boolean.TRUE == enableSilkTheme.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_silk_theme")
        }
        if (java.lang.Boolean.TRUE == enableMaterial3Theme.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_material3_theme")
        }
        if (java.lang.Boolean.TRUE == enableSilkPopup.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_silk_popup")
        }
        if (java.lang.Boolean.TRUE == enableAiSmartReply.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_enable_ai_core_smart_reply")
        }
        if (java.lang.Boolean.TRUE == enableKeyboardRedesignGoogleSans.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_keyboard_redesign_google_sans")
        }
        if (java.lang.Boolean.TRUE == enableBrightKeyOnDynamicColor.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.flag_bright_key_on_dynamic_color_dark_theme")
        }
        if (java.lang.Boolean.TRUE == enableGrammarChecker.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.grammar_checker")
        }
        if (java.lang.Boolean.TRUE == enableInlineSuggestions.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.inline_suggestions")
        }
        if (java.lang.Boolean.TRUE == enableClipboardEntity.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.clipboard_entity_extraction")
        }
        if (java.lang.Boolean.TRUE == enableClipboardItemEdit.value) {
            applyFeatureMarker("dev.jason.gboardpatches.feature.clipboard_item_edit")
        }
    }
}

// The applyFeatureMarker and supporting extensions are provided by GboardFeatureFlagMarkerPatchUtils (same package).