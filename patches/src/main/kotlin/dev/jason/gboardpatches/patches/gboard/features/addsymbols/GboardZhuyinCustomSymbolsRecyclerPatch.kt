package dev.jason.gboardpatches.patches.gboard.features.addsymbols

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import dev.jason.gboardpatches.patches.gboard.shared.findMutableMethodOrThrow

private const val BASE_RECYCLER_ADAPTER_CLASS = "Ljp;"
private const val EMOTICON_RECYCLER_ADAPTER_CLASS = "Lhvx;"

internal val gboardZhuyinCustomSymbolsRecyclerPatch = bytecodePatch(
    description = "移植 add-symbols 的 custom recycler bind rendering。"
) {
    dependsOn(gboardZhuyinCustomSymbolsExtensionPatch)

    execute {
        patchConstructor()
        patchBindViewHolder()
    }
}

context(BytecodePatchContext)
private fun patchConstructor() {
    val mutableMethod = findMutableMethodOrThrow(
        classType = EMOTICON_RECYCLER_ADAPTER_CLASS,
        name = "<init>",
        returnType = "V",
        parameterTypes = listOf(
            "Landroid/content/Context;",
            "Lhwe;",
            "Ljava/util/function/Consumer;",
            "I",
            "I"
        )
    )
    val superConstructorIndex = mutableMethod.indexOfMethodCallOrThrow(
        definingClass = BASE_RECYCLER_ADAPTER_CLASS,
        name = "<init>",
        returnType = "V",
        parameterTypes = emptyList()
    )
    mutableMethod.addInstructions(superConstructorIndex + 1, CONSTRUCTOR_DELEGATE)
}

context(BytecodePatchContext)
private fun patchBindViewHolder() {
    val mutableMethod = findMutableMethodOrThrow(
        classType = EMOTICON_RECYCLER_ADAPTER_CLASS,
        name = "p",
        returnType = "V",
        parameterTypes = listOf("Lkm;", "I")
    )
    mutableMethod.addInstructions(0, BIND_VIEW_HOLDER_DELEGATE)
}

private val CONSTRUCTOR_DELEGATE = """
    invoke-static {p0}, Ldev/jason/gboardpatches/extension/addsymbols/GboardAddSymbolsRuntime;->onEmoticonRecyclerAdapterConstructed(Ljava/lang/Object;)V
""".trimIndent()

private val BIND_VIEW_HOLDER_DELEGATE = """
    invoke-static {p0, p1, p2}, Ldev/jason/gboardpatches/extension/addsymbols/GboardAddSymbolsRuntime;->bindCustomViewHolder(Ljava/lang/Object;Ljava/lang/Object;I)Z

    move-result v0

    if-eqz v0, :jasondev_continue

    return-void

    :jasondev_continue
""".trimIndent()
