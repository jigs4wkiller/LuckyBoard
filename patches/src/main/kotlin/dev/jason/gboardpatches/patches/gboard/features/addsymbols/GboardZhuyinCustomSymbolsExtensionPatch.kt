package dev.jason.gboardpatches.patches.gboard.features.addsymbols

import app.morphe.patcher.patch.bytecodePatch

internal val gboardZhuyinCustomSymbolsExtensionPatch = bytecodePatch(
    description = "將 add-symbols runtime helper extension 併入 target APK。"
) {
    extendWith("extensions/gboard-patches.rve")
}
