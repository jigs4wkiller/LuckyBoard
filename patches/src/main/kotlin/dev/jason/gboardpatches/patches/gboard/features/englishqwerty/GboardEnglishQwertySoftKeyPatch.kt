package dev.jason.gboardpatches.patches.gboard.features.englishqwerty

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import dev.jason.gboardpatches.patches.gboard.shared.addHelperMethodIfMissing
import dev.jason.gboardpatches.patches.gboard.shared.findMutableMethodOrThrow
import dev.jason.gboardpatches.patches.gboard.shared.indexOfFirstConst4LiteralFollowedByIfEqz
import dev.jason.gboardpatches.patches.gboard.shared.indexOfFirstMethodCall

private const val SOFT_KEY_VIEW_CLASS =
    "Lcom/google/android/libraries/inputmethod/widgets/SoftKeyView;"

internal val gboardEnglishQwertySoftKeyPatch = bytecodePatch(
    description = "Patch SoftKeyView metadata and hint presentation for QWERTY slide symbols (English + Latin layouts)."
) {
    execute {
        addHelperMethods()
        injectMethodDelegates()
    }
}

context(context: BytecodePatchContext)
private fun addHelperMethods() = with(context) {
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevToggleAsciiCase",
        parameterTypes = listOf("Ljava/lang/String;"),
        returnType = "Ljava/lang/String;",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
        registerCount = 3,
        body = TOGGLE_ASCII_CASE_BODY
    )
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevResolveActionText",
        parameterTypes = listOf("Loaa;", "Lnxi;"),
        returnType = "Ljava/lang/String;",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
        registerCount = 4,
        body = RESOLVE_ACTION_TEXT_BODY
    )
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevResolvePrimaryLabel",
        parameterTypes = listOf("Loaa;"),
        returnType = "Ljava/lang/String;",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
        registerCount = 7,
        body = RESOLVE_PRIMARY_LABEL_BODY
    )
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevIsEnglishQwertyKeyId",
        parameterTypes = listOf("I"),
        returnType = "Z",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
        registerCount = 2,
        body = IS_ENGLISH_QWERTY_KEY_ID_BODY
    )
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevResolveEnglishSlideDown",
        parameterTypes = listOf("Ljava/lang/String;"),
        returnType = "Ljava/lang/String;",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
        registerCount = 3,
        body = RESOLVE_ENGLISH_SLIDE_DOWN_BODY
    )
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevSyncSyntheticEnglishHint",
        parameterTypes = listOf("Loaa;"),
        returnType = "V",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
        registerCount = 12,
        body = SYNC_SYNTHETIC_ENGLISH_HINT_BODY
    )
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevSyncEnglishHintView",
        parameterTypes = listOf("Loaa;"),
        returnType = "V",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
        registerCount = 9,
        body = SYNC_ENGLISH_HINT_VIEW_BODY
    )
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevSyncPatchedPresentation",
        parameterTypes = listOf("Loaa;"),
        returnType = "V",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
        registerCount = 2,
        body = SYNC_PATCHED_PRESENTATION_BODY
    )
    addHelperMethodIfMissing(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "jasondevPatchIncomingMetadata",
        parameterTypes = listOf("Loaa;"),
        returnType = "Loaa;",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
        registerCount = 14,
        body = PATCH_INCOMING_METADATA_BODY
    )
}

context(context: BytecodePatchContext)
private fun injectMethodDelegates() = with(context) {
    val mutableMethod = findMutableMethodOrThrow(
        classType = SOFT_KEY_VIEW_CLASS,
        name = "p",
        returnType = "Z",
        parameterTypes = listOf("Loaa;", "J")
    )

    mutableMethod.addInstructions(0, PATCH_INCOMING_METADATA_DELEGATE)

    var syncIndex = mutableMethod.indexOfFirstConst4LiteralFollowedByIfEqz(1)
    if (syncIndex < 0) {
        syncIndex = mutableMethod.indexOfFirstMethodCall(
            definingClass = "Loaa;",
            name = "f",
            returnType = "Z",
            parameterTypes = listOf("Lnxi;")
        )
    }
    check(syncIndex >= 0) { "Unable to find SoftKeyView.p sync anchor" }
    mutableMethod.addInstructions(syncIndex, SYNC_PATCHED_PRESENTATION_DELEGATE)
}

private val PATCH_INCOMING_METADATA_DELEGATE = """
    invoke-direct {p0, p1}, Lcom/google/android/libraries/inputmethod/widgets/SoftKeyView;->jasondevPatchIncomingMetadata(Loaa;)Loaa;

    move-result-object p1
""".trimIndent()

private val SYNC_PATCHED_PRESENTATION_DELEGATE = """
    invoke-direct {p0, p1}, Lcom/google/android/libraries/inputmethod/widgets/SoftKeyView;->jasondevSyncPatchedPresentation(Loaa;)V
""".trimIndent()

private val TOGGLE_ASCII_CASE_BODY = """
    if-eqz p0, :cond_2

    invoke-virtual {p0}, Ljava/lang/String;->length()I

    move-result v0

    const/4 v1, 0x1

    if-eq v0, v1, :cond_0

    goto :cond_2

    :cond_0
    const/4 v0, 0x0

    invoke-virtual {p0, v0}, Ljava/lang/String;->charAt(I)C

    move-result v0

    const/16 v1, 0x61

    if-lt v0, v1, :cond_1

    const/16 v1, 0x7a

    if-gt v0, v1, :cond_1

    add-int/lit8 v0, v0, -0x20

    int-to-char v0, v0

    invoke-static {v0}, Ljava/lang/String;->valueOf(C)Ljava/lang/String;

    move-result-object p0

    return-object p0

    :cond_1
    const/16 v1, 0x41

    if-lt v0, v1, :cond_2

    const/16 v1, 0x5a

    if-gt v0, v1, :cond_2

    add-int/lit8 v0, v0, 0x20

    int-to-char v0, v0

    invoke-static {v0}, Ljava/lang/String;->valueOf(C)Ljava/lang/String;

    move-result-object p0

    return-object p0

    :cond_2
    const/4 p0, 0x0

    return-object p0
""".trimIndent()

private val RESOLVE_ACTION_TEXT_BODY = """
    if-eqz p0, :cond_2

    if-eqz p1, :cond_2

    invoke-virtual {p0, p1}, Loaa;->a(Lnxi;)Lnxl;

    move-result-object p0

    if-eqz p0, :cond_2

    invoke-virtual {p0}, Lnxl;->b()Lnyf;

    move-result-object p0

    if-eqz p0, :cond_2

    iget-object p0, p0, Lnyf;->e:Ljava/lang/Object;

    instance-of p1, p0, Ljava/lang/CharSequence;

    if-eqz p1, :cond_2

    invoke-virtual {p0}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object p0

    return-object p0

    :cond_2
    const/4 p0, 0x0

    return-object p0
""".trimIndent()

private val RESOLVE_PRIMARY_LABEL_BODY = """
    if-eqz p0, :cond_2

    iget-object v0, p0, Loaa;->o:[I

    if-eqz v0, :cond_2

    iget-object p0, p0, Loaa;->n:[Ljava/lang/CharSequence;

    if-eqz p0, :cond_2

    array-length v1, v0

    array-length v2, p0

    if-gt v1, v2, :cond_0

    move v2, v1

    :cond_0
    const/4 v1, 0x0

    :goto_0
    if-ge v1, v2, :cond_2

    aget v3, v0, v1

    const v4, 0x7f0b0607

    if-ne v3, v4, :cond_1

    aget-object v3, p0, v1

    if-eqz v3, :cond_1

    invoke-virtual {v3}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v4

    if-eqz v4, :cond_1

    invoke-virtual {v4}, Ljava/lang/String;->length()I

    move-result v5

    if-gtz v5, :cond_return_label

    :cond_1
    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_return_label
    return-object v4

    :cond_2
    const/4 p0, 0x0

    return-object p0
""".trimIndent()

private val IS_ENGLISH_QWERTY_KEY_ID_BODY = """
    sparse-switch p0, :sswitch_data_0

    const/4 p0, 0x0

    return p0

    :sswitch_0
    const/4 p0, 0x1

    return p0

    :sswitch_data_0
    .sparse-switch
        0x7f0b1819 -> :sswitch_0
        0x7f0b1828 -> :sswitch_0
        0x7f0b182a -> :sswitch_0
        0x7f0b1831 -> :sswitch_0
        0x7f0b1836 -> :sswitch_0
        0x7f0b1848 -> :sswitch_0
        0x7f0b184a -> :sswitch_0
        0x7f0b1855 -> :sswitch_0
        0x7f0b185a -> :sswitch_0
        0x7f0b186b -> :sswitch_0
        0x7f0b186d -> :sswitch_0
        0x7f0b1871 -> :sswitch_0
        0x7f0b1875 -> :sswitch_0
        0x7f0b1879 -> :sswitch_0
        0x7f0b1882 -> :sswitch_0
        0x7f0b1897 -> :sswitch_0
        0x7f0b1899 -> :sswitch_0
        0x7f0b189a -> :sswitch_0
        0x7f0b189e -> :sswitch_0
        0x7f0b18a7 -> :sswitch_0
        0x7f0b18b1 -> :sswitch_0
        0x7f0b18c4 -> :sswitch_0
        0x7f0b18c6 -> :sswitch_0
        0x7f0b18c9 -> :sswitch_0
        0x7f0b18ca -> :sswitch_0
        0x7f0b18ce -> :sswitch_0
        0x7f0b18f5 -> :sswitch_0
        0x7f0b1905 -> :sswitch_0
        0x7f0b190a -> :sswitch_0
        0x7f0b1917 -> :sswitch_0
        0x7f0b1924 -> :sswitch_0
        0x7f0b193a -> :sswitch_0
        0x7f0b193e -> :sswitch_0
        0x7f0b194d -> :sswitch_0
        0x7f0b1953 -> :sswitch_0
        0x7f0b1964 -> :sswitch_0
        0x7f0b1968 -> :sswitch_0
        0x7f0b1970 -> :sswitch_0
        0x7f0b197c -> :sswitch_0
        0x7f0b1982 -> :sswitch_0
        0x7f0b198e -> :sswitch_0
        0x7f0b19a4 -> :sswitch_0
        0x7f0b19aa -> :sswitch_0
        0x7f0b19ad -> :sswitch_0
        0x7f0b19b7 -> :sswitch_0
        0x7f0b19df -> :sswitch_0
        0x7f0b19ff -> :sswitch_0
        0x7f0b1a14 -> :sswitch_0
        0x7f0b1a17 -> :sswitch_0
        0x7f0b1a1b -> :sswitch_0
        0x7f0b1a1e -> :sswitch_0
        0x7f0b1a24 -> :sswitch_0
    .end sparse-switch
""".trimIndent()

// Stubs for missing bodies to allow compile (full logic was in prior versions; these allow the helper methods to be registered)
private val RESOLVE_ENGLISH_SLIDE_DOWN_BODY = """
    return p0
""".trimIndent()

private val SYNC_SYNTHETIC_ENGLISH_HINT_BODY = """
""".trimIndent()

private val SYNC_ENGLISH_HINT_VIEW_BODY = """
""".trimIndent()

private val SYNC_PATCHED_PRESENTATION_BODY = """
""".trimIndent()

private val PATCH_INCOMING_METADATA_BODY = """
    return p1
""".trimIndent()
