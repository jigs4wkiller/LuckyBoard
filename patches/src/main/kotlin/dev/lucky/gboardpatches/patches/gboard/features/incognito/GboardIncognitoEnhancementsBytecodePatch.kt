package dev.lucky.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import dev.lucky.gboardpatches.patches.gboard.features.signaturebypass.gboardSignatureBypassBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.shared.addHelperMethodIfMissing
import dev.lucky.gboardpatches.patches.gboard.shared.addFieldIfMissing
import dev.lucky.gboardpatches.patches.gboard.shared.clearExceptionHandlers
import dev.lucky.gboardpatches.patches.gboard.shared.findMutableMethodOrThrow
import dev.lucky.gboardpatches.patches.gboard.shared.indexOfFirstMethodCall
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

@Suppress("unused")
internal val gboardIncognitoEnhancementsBytecodePatch = bytecodePatch(
    description = "Enable clipboard and voice typing in incognito mode, and force always-incognito mode."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(gboardSignatureBypassBytecodePatch)

    execute {
        // 1. Add static boolean field to jak class for extension↔smali bridge
        addFieldIfMissing(
            classType = "Ljak;",
            fieldName = "sForceIncognito",
            fieldType = "Z",
            accessFlags = AccessFlags.PUBLIC.value or AccessFlags.STATIC.value
        )

        // 2. Helper: jak.forceIncognitoFlag(EditorInfo)
        //    Reads sForceIncognito static field. If true, sets IME_FLAG_NO_PERSONALIZED_LEARNING.
        //    No hidden APIs, no Context, no SharedPreferences — just sget-boolean.
        addHelperMethodIfMissing(
            classType = "Ljak;",
            name = "forceIncognitoFlag",
            parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;"),
            returnType = "V",
            accessFlags = AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
            registerCount = 4,
            body = """
                sget-boolean v0, Ljak;->sForceIncognito:Z
                if-eqz v0, :skip
                iget v0, p0, Landroid/view/inputmethod/EditorInfo;->imeOptions:I
                const/high16 v1, 0x1000000
                or-int v0, v0, v1
                iput v0, p0, Landroid/view/inputmethod/EditorInfo;->imeOptions:I
                :skip
                return-void
            """.trimIndent()
        )

        // 3. jak.ab() -> true (always report incognito check as true)
        val m = findMutableMethodOrThrow("Ljak;", "ab", "Z", listOf("Landroid/view/inputmethod/EditorInfo;"))
        m.clearExceptionHandlers()
        val count = m.implementation?.instructions?.size ?: 0
        m.removeInstructions(0, count)
        m.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // 4. Inject forceIncognitoFlag call into onStartInput at index 3
        //    First 3 instructions: move-object/from16 v0,p0 / v1,p1 / v2,p2
        //    At index 3, v1 = EditorInfo (from p1).
        val helperRef = "Ljak;->forceIncognitoFlag(Landroid/view/inputmethod/EditorInfo;)V"
        val siMethod = findMutableMethodOrThrow(
            "Lkbp;", "onStartInput", "V",
            listOf("Landroid/view/inputmethod/EditorInfo;", "Z")
        )
        siMethod.addInstructions(3, "invoke-static {v1}, $helperRef")

        // 5. Enable clipboard in incognito mode
        //    Removes the incognito check in dch.onPrimaryClipChanged()
        //    that blocks clipboard capture when incognito is active.
        try {
            OnPrimaryClipChangedFingerprint.method.apply {
                clearExceptionHandlers()
                val start = OnPrimaryClipChangedFingerprint.instructionMatches.first().index
                val end = OnPrimaryClipChangedFingerprint.instructionMatches.last().index
                removeInstructions(start, (end - start) + 1)
            }
        } catch (_: Exception) {}

        // 6. Enable voice typing in incognito mode
        //    Bypasses the incognito check that disables voice input.
        try {
            EnableVoiceTypingFingerprint.method.apply {
                val idx = EnableVoiceTypingFingerprint.instructionMatches.last().index
                val insn = getInstruction<OneRegisterInstruction>(idx)
                addInstructions(idx, "const/4 v${insn.registerA}, 0x0")
            }
        } catch (_: Exception) {}
    }
}
