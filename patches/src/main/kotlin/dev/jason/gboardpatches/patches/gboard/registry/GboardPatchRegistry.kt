package dev.jason.gboardpatches.patches.gboard.registry

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.about.gboardAboutPageResourcePatch
import dev.jason.gboardpatches.patches.gboard.features.addsymbols.gboardZhuyinCustomSymbolsCorpusPatch
import dev.jason.gboardpatches.patches.gboard.features.addsymbols.gboardZhuyinCustomSymbolsEmoticonStatePatch
import dev.jason.gboardpatches.patches.gboard.features.addsymbols.gboardZhuyinCustomSymbolsEntryPatch
import dev.jason.gboardpatches.patches.gboard.features.addsymbols.gboardZhuyinCustomSymbolsHistoryPatch
import dev.jason.gboardpatches.patches.gboard.features.addsymbols.gboardZhuyinCustomSymbolsRecyclerPatch
import dev.jason.gboardpatches.patches.gboard.features.addsymbols.gboardZhuyinCustomSymbolsRoutingPatch
import dev.jason.gboardpatches.patches.gboard.features.chinesevoice.gboardChineseOnlineVoiceBytecodePatch
import dev.jason.gboardpatches.patches.gboard.features.chinesevoice.gboardChineseOnlineVoiceResourcePatch
import dev.jason.gboardpatches.patches.gboard.features.englishqwerty.gboardEnglishQwertySlideResourcePatch
import dev.jason.gboardpatches.patches.gboard.features.englishqwerty.gboardEnglishQwertySoftKeyPatch
import dev.jason.gboardpatches.patches.gboard.features.packagerename.gboardPackageRenameResourcePatch
import dev.jason.gboardpatches.patches.gboard.features.zhuyinslide.gboardZhuyinSlidePointerAnchorPatch
import dev.jason.gboardpatches.patches.gboard.features.zhuyinslide.gboardZhuyinSlideResourcePatch
import dev.jason.gboardpatches.patches.gboard.features.zhuyintraditionalsimplifiedtoggle.gboardZhuyinTraditionalSimplifiedToggleRuntimePatch
import dev.jason.gboardpatches.patches.gboard.features.zhuyintraditionalsimplifiedtoggle.gboardZhuyinTraditionalSimplifiedToggleSoftKeyPatch
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

@Suppress("unused")
val gboardZhuyinSlideInputPatch = resourcePatch(
    name = "Zhuyin Slide Input",
    description = "注音鍵盤支持上下滑輸入",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardAboutPageResourcePatch,
        gboardZhuyinSlideResourcePatch,
        gboardZhuyinSlidePointerAnchorPatch
    )
}

@Suppress("unused")
val gboardEnglishQwertySlideSymbolsPatch = resourcePatch(
    name = "English QWERTY Slide Symbols",
    description = "英文 QWERTY 鍵盤支持上下滑符號輸入",
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
val gboardZhuyinQuickTraditionalSimplifiedTogglePatch = resourcePatch(
    name = "Zhuyin Quick Traditional/Simplified Toggle",
    description = "注音 ㄥ 上滑快速切換繁簡",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardZhuyinSlideInputPatch,
        gboardZhuyinTraditionalSimplifiedToggleSoftKeyPatch,
        gboardZhuyinTraditionalSimplifiedToggleRuntimePatch
    )
}

@Suppress("unused")
val gboardCustomSymbolsPatch = resourcePatch(
    name = "Custom Symbols",
    description = "新增獨立的特殊符號分頁，長按逗號->愛心",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardAboutPageResourcePatch,
        gboardZhuyinCustomSymbolsEntryPatch,
        gboardZhuyinCustomSymbolsCorpusPatch,
        gboardZhuyinCustomSymbolsRoutingPatch,
        gboardZhuyinCustomSymbolsEmoticonStatePatch,
        gboardZhuyinCustomSymbolsHistoryPatch,
        gboardZhuyinCustomSymbolsRecyclerPatch
    )
}

@Suppress("unused")
val gboardChineseOnlineVoiceInputPatch = resourcePatch(
    name = "Chinese Online Voice Input",
    description = "強制啟用中文語音",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardAboutPageResourcePatch,
        gboardChineseOnlineVoiceResourcePatch,
        gboardChineseOnlineVoiceBytecodePatch
    )
}

@Suppress("unused")
val gboardPackageRenamePatch = resourcePatch(
    name = "Package Rename",
    description = "將套件名稱改成 dev.jason.com.google.android.inputmethod.latin 以便共存安裝",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPackageRenameResourcePatch
    )
}
