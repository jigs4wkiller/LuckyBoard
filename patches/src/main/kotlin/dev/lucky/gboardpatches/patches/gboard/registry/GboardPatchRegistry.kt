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
import dev.lucky.gboardpatches.patches.gboard.features.incognito.gboardIncognitoEnhancementsBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.debug.gboardDebugPatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardFeatureFlagsBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardFeatureFlagsPatch
import dev.lucky.gboardpatches.patches.gboard.features.luckify.gboardLuckifyResourcePatch
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

// QWERTY Slide Symbols — disabled in beta: needs reverse engineering for new APK
// (obfuscated types Loaa;→Lkwf;, ~40 resource IDs changed, method signatures changed)
// @Suppress("unused")
// val gboardEnglishQwertySlideSymbolsPatch = resourcePatch(
//     name = "QWERTY Slide Symbols",
//     description = "Enable slide-up and slide-down symbol input on QWERTY keyboards (English and other Latin layouts where supported).",
//     default = true
// ) {
//     compatibleWith(COMPATIBILITY_GBOARD)
//
//     dependsOn(
//         gboardAboutPageResourcePatch,
//         gboardEnglishQwertySlideResourcePatch,
//         gboardEnglishQwertySoftKeyPatch
//     )
// }

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
val gboardLuckifyPatch = dev.lucky.gboardpatches.patches.gboard.features.luckify.gboardLuckifyResourcePatch

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

// NOTE: gboardFeatureFlagsPatch and gboardIncognitoEnhancementsPatch are
// discovered via @Suppress("unused") in their own feature subpackages.
// Rebind aliases here trigger a NullPointerException in PatchLoader.getPatchMethods.

@Suppress("unused")
val gboardDebugPatchAlias = gboardDebugPatch

// Universal Resource Cleaner — handles both density and language cleaning.
@Suppress("unused")
val universalResourceCleanerPatch = dev.lucky.gboardpatches.patches.universal.universalResourceCleanerPatch
