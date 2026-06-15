package dev.jason.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.literal
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * See: https://developer.android.com/reference/android/view/inputmethod/EditorInfo#IME_FLAG_NO_PERSONALIZED_LEARNING.
 */
private const val IME_FLAG_NO_PERSONALIZED_LEARNING: Long = 0x1000000 // Constant value: 16777216

private val imeNoPersonalizedLearningFilters =
    listOf(
        opcode(Opcode.IF_EQZ),
        fieldAccess(
            definingClass = "Landroid/view/inputmethod/EditorInfo;",
            name = "imeOptions",
            type = "I",
            location = app.morphe.patcher.InstructionLocation.MatchAfterImmediately()
        ),
        literal(
            literal = IME_FLAG_NO_PERSONALIZED_LEARNING,
            location = app.morphe.patcher.InstructionLocation.MatchAfterImmediately()
        ),
        opcode(Opcode.AND_INT_2ADDR, app.morphe.patcher.InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.IF_EQZ, app.morphe.patcher.InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.CONST_4, app.morphe.patcher.InstructionLocation.MatchAfterImmediately()),
        opcode(Opcode.RETURN, app.morphe.patcher.InstructionLocation.MatchAfterImmediately()),
    )

internal object IsIncognitoModeFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf("Landroid/view/inputmethod/EditorInfo;"),
    filters = imeNoPersonalizedLearningFilters
)

/**
 * Introduced in Gboard 14.9.06.696880419 beta.
 */
internal object IsIncognitoModeV2Fingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    filters = imeNoPersonalizedLearningFilters
)

/**
 * Introduced in Gboard 14.4.06.646482735 beta.
 */
internal object IsIncognitoModeInlinedFingerprint : Fingerprint(
    name = "onStartInput",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/view/inputmethod/EditorInfo;", "Z"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IF_EQZ,
        Opcode.IGET,
        Opcode.CONST_HIGH16,
        Opcode.AND_INT_2ADDR,
        Opcode.IF_EQZ,
        Opcode.MOVE,
        Opcode.GOTO,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
    )
)

internal object OnPrimaryClipChangedFingerprint : Fingerprint(
    name = "onPrimaryClipChanged",
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.RETURN_VOID,
    ),
    strings = listOf("clipboard_primary_uri", "")
)

internal object EnableVoiceTypingFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(
        "Landroid/content/Context;",
        "Landroid/view/inputmethod/EditorInfo;",
        "Z",
    ),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.RETURN,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.IF_EQZ,
    )
)
