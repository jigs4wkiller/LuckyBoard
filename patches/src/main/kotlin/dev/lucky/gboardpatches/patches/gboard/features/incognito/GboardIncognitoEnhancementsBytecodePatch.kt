package dev.lucky.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import dev.lucky.gboardpatches.patches.gboard.features.signaturebypass.gboardSignatureBypassBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.shared.clearExceptionHandlers
import dev.lucky.gboardpatches.patches.gboard.shared.findMutableMethodOrThrow
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

@Suppress("unused")
internal val gboardIncognitoEnhancementsBytecodePatch = bytecodePatch(
    description = "Enable clipboard and voice typing in incognito mode, and force always-incognito mode."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(gboardSignatureBypassBytecodePatch)

    execute {
        // Force incognito: set IME_FLAG_NO_PERSONALIZED_LEARNING in EditorInfo.imeOptions
        try {
            val incognitoMethod = findMutableMethodOrThrow(
                classType = "Lcom/google/android/libraries/inputmethod/ime/AbstractIme;",
                name = "jC",
                returnType = "Z",
                parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;")
            )
            incognitoMethod.clearExceptionHandlers()
            val instructionCount = incognitoMethod.implementation?.instructions?.size ?: 0
            incognitoMethod.removeInstructions(0, instructionCount)
            incognitoMethod.addInstructions(0, """
                iget v0, p1, Landroid/view/inputmethod/EditorInfo;->imeOptions:I
                or-int/lit16 v0, v0, 0x1000000
                iput v0, p1, Landroid/view/inputmethod/EditorInfo;->imeOptions:I
                const/4 v0, 0x1
                return v0
            """.trimIndent())
        } catch (e: Exception) {
            System.err.println("[LuckyBoard] Incognito patch FAILED: " + e.message)
            e.printStackTrace()
        }

        // Enable clipboard in incognito - remove the incognito check entirely
        try {
            OnPrimaryClipChangedFingerprint.method.apply {
                clearExceptionHandlers()
                val patternMatch = OnPrimaryClipChangedFingerprint.instructionMatches
                val isIncognitoModeIndex = patternMatch.first().index
                val returnVoidIndex = patternMatch.last().index
                val instructionsToRemoveCount = (returnVoidIndex - isIncognitoModeIndex) + 1
                removeInstructions(
                    index = isIncognitoModeIndex,
                    count = instructionsToRemoveCount
                )
            }
        } catch (e: Exception) {
            System.err.println("[LuckyBoard] Clipboard patch FAILED: " + e.message)
        }

        // Enable voice typing in incognito
        try {
            EnableVoiceTypingFingerprint.method.apply {
                val isIncognitoEnabledIndex =
                    EnableVoiceTypingFingerprint.instructionMatches.last().index
                val isIncognitoEnabledInstruction =
                    getInstruction<OneRegisterInstruction>(isIncognitoEnabledIndex)
                val isIncognitoEnabledRegister = isIncognitoEnabledInstruction.registerA
                addInstructions(
                    index = isIncognitoEnabledIndex,
                    smaliInstructions = "const/4 v$isIncognitoEnabledRegister, 0x0"
                )
            }
        } catch (e: Exception) {
            System.err.println("[LuckyBoard] Voice patch FAILED: " + e.message)
        }
    }
}
