package dev.lucky.gboardpatches.patches.gboard.registry

import app.morphe.patcher.patch.resourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.about.gboardAboutPageResourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.addsymbols.gboardCustomSymbolsFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.clipboard.gboardClipboardFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.clipboard.gboardClipboardAdapterTrimPatch
import dev.lucky.gboardpatches.patches.gboard.features.clipboard.gboardClipboardColumnCountPatch
import dev.lucky.gboardpatches.patches.gboard.features.clipboard.gboardClipboardItemBindPatch
import dev.lucky.gboardpatches.patches.gboard.features.clipboard.gboardClipboardLoaderPatch
import dev.lucky.gboardpatches.patches.gboard.features.clipboard.gboardClipboardPrunePatch
import dev.lucky.gboardpatches.patches.gboard.features.webclipboard.gboardWebClipboardAssetsPatch
import dev.lucky.gboardpatches.patches.gboard.features.webclipboard.gboardWebClipboardCapturePatch
import dev.lucky.gboardpatches.patches.gboard.features.webclipboard.gboardWebClipboardFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.webclipboard.gboardWebClipboardManifestPatch
import dev.lucky.gboardpatches.patches.gboard.features.englishqwerty.gboardEnglishQwertySlideResourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.englishqwerty.gboardEnglishQwertySoftKeyPatch
import dev.lucky.gboardpatches.patches.gboard.features.brandrename.gboardBrandRenameResourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.incognito.gboardIncognitoEnhancementsBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardClipboardEntityExtractionFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardClipboardItemEditFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardFeatureFlagsBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardGrammarCheckerFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardInlineSuggestionsFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardKeyShapeCustomizationFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardKeyShapeSelectionFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardFeatureFlagsPatch
import dev.lucky.gboardpatches.patches.gboard.features.packagerename.gboardPackageRenameResourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.settingshomepage.gboardSettingsHomepageBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.settingshomepage.gboardSettingsHomepageFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.settingscleanup.gboardSettingsCleanUpResourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.signaturebypass.gboardSignatureBypassBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.symbolfooter.gboardSymbolFooterOrderBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.symbolfooter.gboardSymbolFooterOrderFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.features.latinglobe.gboardLatinGlobeKeyIgnoreIntervalBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.latinglobe.gboardLatinGlobeKeyIgnoreIntervalFeatureMarkerPatch
import dev.lucky.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.lucky.gboardpatches.patches.gboard.shared.gboardPatchesSettingsPatch
import dev.lucky.gboardpatches.patches.gboard.features.undoredoaccesspoint.gboardUndoRedoAccessPointBytecodePatch
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

@Suppress("unused")
val gboardEnglishQwertySlideSymbolsPatch = resourcePatch(
    name = "QWERTY Slide Symbols",
    description = "Enable slide-up and slide-down symbol input on QWERTY keyboards (English and other Latin layouts where supported).",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardAboutPageResourcePatch,
        gboardEnglishQwertySlideResourcePatch,
        gboardEnglishQwertySoftKeyPatch
    )
}

@Suppress("unused")
val gboardCustomSymbolsPatch = resourcePatch(
    name = "Custom Symbols",
    description = "Add a dedicated custom symbols tab and replace the long-press comma entry with a heart shortcut.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardAboutPageResourcePatch,
        gboardCustomSymbolsFeatureMarkerPatch
    )
}

@Suppress("unused")
val gboardSymbolsFooterOrderPatch = resourcePatch(
    name = "Emojis, stickers & GIFs Tab Order",
    description = "Customize the bottom tab order in Gboard's Emojis, stickers & GIFs panel with drag-and-drop reordering.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesSettingsPatch,
        gboardSymbolFooterOrderFeatureMarkerPatch,
        gboardSymbolFooterOrderBytecodePatch
    )
}

@Suppress("unused")
val gboardUndoRedoAccessPointPatch = resourcePatch(
    name = "Enable Undo/Redo feature",
    description = "Enable the Undo and Redo entry points in Gboard.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardUndoRedoAccessPointBytecodePatch
    )
}

@Suppress("unused")
val gboardClipboardEnhancementsPatch = resourcePatch(
    name = "Clipboard Enhancements",
    description = "Enhance clipboard retention time, item count limits, preview lines, countdown and creation-time labels, order index, and grid columns. Use the gear for per-feature options.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardAboutPageResourcePatch,
        gboardPatchesSettingsPatch,
        gboardClipboardFeatureMarkerPatch,
        gboardClipboardLoaderPatch,
        gboardClipboardPrunePatch,
        gboardClipboardColumnCountPatch,
        gboardClipboardAdapterTrimPatch,
        gboardClipboardItemBindPatch
    )
}

@Suppress("unused")
val gboardWebClipboardPatch = resourcePatch(
    name = "Web Clipboard",
    description = "Add the phone-hosted Web Clipboard with browser sync, pairing, and a Quick Settings Tile.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesSettingsPatch,
        gboardWebClipboardFeatureMarkerPatch,
        gboardWebClipboardManifestPatch,
        gboardWebClipboardAssetsPatch,
        gboardWebClipboardCapturePatch
    )
}

@Suppress("unused")
val gboardClipboardEntityExtractionFlagPatch = resourcePatch(
    name = "Clipboard Entity Extraction",
    description = "Enable Clipboard > Show addresses, phone numbers, and other items pulled from recently copied text.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesExtensionCarrierPatch,
        gboardFeatureFlagsBytecodePatch,
        gboardClipboardEntityExtractionFeatureMarkerPatch
    )
}

@Suppress("unused")
val gboardClipboardItemEditFlagPatch = resourcePatch(
    name = "Clipboard Item Edit",
    description = "Enable Edit when long-pressing a clipboard item.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesExtensionCarrierPatch,
        gboardFeatureFlagsBytecodePatch,
        gboardClipboardItemEditFeatureMarkerPatch
    )
}

@Suppress("unused")
val gboardGrammarCheckerFlagPatch = resourcePatch(
    name = "Grammar Checker",
    description = "Enable Text correction > Grammar check.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesExtensionCarrierPatch,
        gboardFeatureFlagsBytecodePatch,
        gboardGrammarCheckerFeatureMarkerPatch
    )
}

@Suppress("unused")
val gboardInlineSuggestionsFlagPatch = resourcePatch(
    name = "Inline Suggestions",
    description = "Enable Text correction > Smart Compose.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesExtensionCarrierPatch,
        gboardFeatureFlagsBytecodePatch,
        gboardInlineSuggestionsFeatureMarkerPatch
    )
}

@Suppress("unused")
val gboardKeyShapeSelectionFlagPatch = resourcePatch(
    name = "Key Shape Selection",
    description = "Enable Key shape selection in Theme details + add more rounded shape gradations (light/medium/strong/very strong/pill) and less rounded + horizontal lines options.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesExtensionCarrierPatch,
        gboardFeatureFlagsBytecodePatch,
        gboardKeyShapeSelectionFeatureMarkerPatch,
        gboardKeyShapeCustomizationFeatureMarkerPatch
    )
}

@Suppress("unused")
val gboardSettingsHomepagePatch = resourcePatch(
    name = "Settings Homepage Override",
    description = "Allow switching between the new and legacy Gboard settings pages.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesSettingsPatch,
        gboardSettingsHomepageFeatureMarkerPatch,
        gboardSettingsHomepageBytecodePatch
    )
}

@Suppress("unused")
val gboardSettingsCleanUpPatch = resourcePatch(
    name = "Settings Clean-Up",
    description = "Removes Help & Feedback, Info, Rate and Privacy subcategories from Gboard's settings. For Privacy, all usage/diagnostics statistics sharing sub-options are disabled before the category is hidden.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesSettingsPatch,
        gboardAboutPageResourcePatch,
        gboardSettingsCleanUpResourcePatch
    )
}

@Suppress("unused")
val gboardLatinGlobeKeyIgnoreIntervalPatch = resourcePatch(
    name = "Latin Globe Key Ignore Interval",
    description = "Add an independent globe key ignore interval override for post-typing language-switch delay (Latin layouts).",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPatchesSettingsPatch,
        gboardLatinGlobeKeyIgnoreIntervalFeatureMarkerPatch,
        gboardLatinGlobeKeyIgnoreIntervalBytecodePatch
    )
}

@Suppress("unused")
val gboardPackageRenamePatch = resourcePatch(
    name = "Package Rename",
    description = "Rename the package to dev.lucky.com.google.android.inputmethod.latin (and the app to \"LuckyBoard\") so it can be installed alongside the official Gboard.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPackageRenameResourcePatch
    )
}

@Suppress("unused")
val gboardBrandRenamePatch = resourcePatch(
    name = "Replace Gboard with LuckyBoard",
    description = "Replace occurrences of \"Gboard\" with \"LuckyBoard\" in resources, strings and manifest during patching for complete UI rebranding (beyond the app label set by Package Rename).",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPackageRenamePatch,
        gboardBrandRenameResourcePatch
    )
}

@Suppress("unused")
val gboardSignatureBypassPatch = resourcePatch(
    name = "Add Gboard Signature Bypass",
    description = "Bypass Gboard signature whitelist checks and force them to pass.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardSignatureBypassBytecodePatch
    )
}

// NOTE: The rebind aliases below are commented out because they trigger a
// NullPointerException in PatchLoader.getPatchMethods (null cast to Patch<*>) 
// when the pure buildAndroid .mpp is loaded by Morphe CLI / Manager / URV.
// The patches are still discovered via their @Suppress("unused") definitions
// in the feature subpackages, so the full 18 patches load correctly in the
// pure build output (the recommended format for the apps).
// See history for the loader crash details.

// @Suppress("unused")
// val gboardFeatureFlagsPatch = dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardFeatureFlagsPatch

// @Suppress("unused")
// val gboardIncognitoEnhancementsPatch = dev.lucky.gboardpatches.patches.gboard.features.incognito.gboardIncognitoEnhancementsPatch

// Universal PNG optimizer - converted from mpatcher script.
// Can be applied to any app in Morphe (no app-specific compatibility).
@Suppress("unused")
val universalPngOptimizerPatch = dev.lucky.gboardpatches.patches.universal.universalPngOptimizerPatch

// Universal Drawable Density Cleaner (Drawableclean) - converted from mpatcher script.
// User selects via patch options (gear) which DPI density to keep.
// No auto-detect. Universal for any app in Morphe.
@Suppress("unused")
val universalDrawableCleanerPatch = dev.lucky.gboardpatches.patches.universal.universalDrawableCleanerPatch
