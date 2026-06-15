package dev.lucky.gboardpatches.patches.gboard.registry

import dev.lucky.gboardpatches.patches.gboard.features.addsymbols.gboardCustomSymbolsPatch
import dev.lucky.gboardpatches.patches.gboard.features.brandrename.gboardBrandRenamePatch
import dev.lucky.gboardpatches.patches.gboard.features.clipboard.gboardClipboardPatch
import dev.lucky.gboardpatches.patches.gboard.features.englishqwerty.gboardEnglishQwertySlideSymbolsPatch
import dev.lucky.gboardpatches.patches.gboard.features.featureflags.gboardFeatureFlagsPatch
import dev.lucky.gboardpatches.patches.gboard.features.incognito.gboardIncognitoEnhancementsPatch
import dev.lucky.gboardpatches.patches.gboard.features.latinglobe.gboardLatinGlobeKeyIgnoreIntervalPatch
import dev.lucky.gboardpatches.patches.gboard.features.packagerename.gboardPackageRenamePatch
import dev.lucky.gboardpatches.patches.gboard.features.settingscleanup.gboardSettingsCleanUpResourcePatch
import dev.lucky.gboardpatches.patches.gboard.features.settingshomepage.gboardSettingsHomepageBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.features.signaturebypass.gboardSignatureBypassPatch
import dev.lucky.gboardpatches.patches.gboard.features.symbolfooter.gboardSymbolFooterOrderPatch
import dev.lucky.gboardpatches.patches.gboard.features.undoredoaccesspoint.gboardUndoRedoAccessPointPatch
import dev.lucky.gboardpatches.patches.gboard.features.webclipboard.gboardWebClipboardPatch
import dev.lucky.gboardpatches.patches.gboard.features.zhuyinslide.gboardZhuyinSlideSymbolsPatch
import dev.lucky.gboardpatches.patches.gboard.features.addsymbols.gboardZhuyinCustomSymbolsPatches
import dev.lucky.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.lucky.gboardpatches.patches.gboard.shared.gboardPatchesSettingsPatch
import dev.lucky.gboardpatches.patches.universal.universalDrawableCleanerPatch
import dev.lucky.gboardpatches.patches.universal.universalLanguageCleanerPatch
import dev.lucky.gboardpatches.patches.universal.universalPngOptimizerPatch

// This is the central registry for all Gboard/LuckyBoard patches.
// For the beta branch, we include the PNG optimizer for further development of a new implementation
// that avoids the ant class loading issue in Morphe Manager.

val allPatches = listOf(
    // Core patches
    gboardSignatureBypassPatch,
    gboardIncognitoEnhancementsPatch,
    gboardCustomSymbolsPatch,
    gboardEnglishQwertySlideSymbolsPatch,
    gboardSymbolFooterOrderPatch,
    gboardUndoRedoAccessPointPatch,
    gboardZhuyinSlideSymbolsPatch,
    gboardZhuyinCustomSymbolsPatches,
    gboardLatinGlobeKeyIgnoreIntervalPatch,
    gboardPackageRenamePatch,
    gboardBrandRenamePatch,
    gboardWebClipboardPatch,
    gboardClipboardPatch,
    gboardFeatureFlagsPatch,
    gboardSettingsHomepageBytecodePatch,
    gboardSettingsCleanUpResourcePatch,
    gboardPatchesSettingsPatch,
    gboardPatchesExtensionCarrierPatch,

    // Universal patches (for any app)
    universalDrawableCleanerPatch,
    universalLanguageCleanerPatch,
    universalPngOptimizerPatch,  // included on beta for new impl work
)
