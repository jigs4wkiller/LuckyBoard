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
        // Force incognito: replace AbstractIme.jC(EditorInfo)->boolean with hardcoded true
        try {
            println("[LuckyBoard] Incognito patch: trying to find AbstractIme.jC method")
            val incognitoMethod = findMutableMethodOrThrow(
                classType = "Lcom/google/android/libraries/inputmethod/ime/AbstractIme;",
                name = "jC",
                returnType = "Z",
                parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;")
            )
            println("[LuckyBoard] Incognito patch: found method, clearing exception handlers")
            incognitoMethod.clearExceptionHandlers()
            val instructionCount = incognitoMethod.implementation?.instructions?.size ?: 0
            println("[LuckyBoard] Incognito patch: removing $instructionCount instructions")
            incognitoMethod.removeInstructions(0, instructionCount)
            incognitoMethod.addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """.trimIndent())
            println("[LuckyBoard] Incognito patch: method replaced successfully")
        } catch (e: Exception) {
            System.err.println("[LuckyBoard] Incognito patch FAILED: " + e.message)
            e.printStackTrace()
        }

        // Enable clipboard in incognito - remove the incognito check entirely
        try {
            println("[LuckyBoard] Clipboard patch: trying to find OnPrimaryClipChangedFingerprint")
            OnPrimaryClipChangedFingerprint.method.apply {
                clearExceptionHandlers()
                val patternMatch = OnPrimaryClipChangedFingerprint.instructionMatches
                val isIncognitoModeIndex = patternMatch.first().index
                val returnVoidIndex = patternMatch.last().index
                val instructionsToRemoveCount = (returnVoidIndex - isIncognitoModeIndex) + 1
                println("[LuckyBoard] Clipboard patch: removing $instructionsToRemoveCount instructions")
                removeInstructions(
                    index = isIncognitoModeIndex,
                    count = instructionsToRemoveCount
                )
                println("[LuckyBoard] Clipboard patch: done")
            }
        } catch (e: Exception) {
            System.err.println("[LuckyBoard] Clipboard patch FAILED: " + e.message)
            e.printStackTrace()
        }

        // Enable voice typing in incognito
        try {
            println("[LuckyBoard] Voice patch: trying to find EnableVoiceTypingFingerprint")
            EnableVoiceTypingFingerprint.method.apply {
                val isIncognitoEnabledIndex =
                    EnableVoiceTypingFingerprint.instructionMatches.last().index
                val isIncognitoEnabledInstruction =
                    getInstruction<OneRegisterInstruction>(isIncognitoEnabledIndex)
                val isIncognitoEnabledRegister = isIncognitoEnabledInstruction.registerA
                println("[LuckyBoard] Voice patch: adding const/4 v$isIncognitoEnabledRegister, 0x0")
                addInstructions(
                    index = isIncognitoEnabledIndex,
                    smaliInstructions = "const/4 v$isIncognitoEnabledRegister, 0x0"
                )
                println("[LuckyBoard] Voice patch: done")
            }
        } catch (e: Exception) {
            System.err.println("[LuckyBoard] Voice patch FAILED: " + e.message)
            e.printStackTrace()
        }
    }
}
