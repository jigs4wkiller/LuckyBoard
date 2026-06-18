package dev.lucky.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import dev.lucky.gboardpatches.patches.gboard.features.signaturebypass.gboardSignatureBypassBytecodePatch
import dev.lucky.gboardpatches.patches.gboard.shared.clearExceptionHandlers
import dev.lucky.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

private const val INCOGNITO_RUNTIME_CLASS =
    "Ldev/lucky/gboardpatches/extension/incognito/GboardIncognitoRuntime;"

@Suppress("unused")
internal val gboardIncognitoEnhancementsBytecodePatch = bytecodePatch(
    description = "Enable clipboard and voice typing in incognito mode, and force always-incognito mode."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(gboardSignatureBypassBytecodePatch)

    execute {
        // Always incognito - check preference at runtime
        val method = IsIncognitoModeFingerprint.methodOrNull
            ?: IsIncognitoModeV2Fingerprint.methodOrNull
            ?: IsIncognitoModeInlinedFingerprint.methodOrNull
            ?: throw PatchException("Failed to force-enable incognito mode.")

        when (method) {
            IsIncognitoModeInlinedFingerprint.methodOrNull -> {
                val patternResult = IsIncognitoModeInlinedFingerprint.instructionMatches
                val requestIncognitoModeIndex = patternResult.last().index
                val isIncognitoModeIndex = requestIncognitoModeIndex - 1

                val requestIncognitoModeInstruction =
                    method.getInstruction<FiveRegisterInstruction>(requestIncognitoModeIndex)
                val isIncognitoModeRegister = requestIncognitoModeInstruction.registerD

                method.replaceInstruction(
                    index = isIncognitoModeIndex,
                    smaliInstruction = "invoke-static {}, $INCOGNITO_RUNTIME_CLASS->shouldForceIncognito()Z"
                )
                method.addInstructions(
                    index = isIncognitoModeIndex + 1,
                    smaliInstructions = "move-result v$isIncognitoModeRegister"
                )
            }
            else -> {
                val returnInsn = if (method.returnType == "Z") "return v0" else "return-void"
                val instructionCount = method.implementation?.instructions?.size ?: 0
                method.clearExceptionHandlers()
                method.removeInstructions(0, instructionCount)
                method.addInstructions(0, """
                    invoke-static {}, $INCOGNITO_RUNTIME_CLASS->shouldForceIncognito()Z
                    move-result v0
                    $returnInsn
                """.trimIndent())
            }
        }

        // Enable clipboard in incognito - use replaceInstruction to avoid exception handler issues
        OnPrimaryClipChangedFingerprint.method.apply {
            clearExceptionHandlers()
            val patternMatch = OnPrimaryClipChangedFingerprint.instructionMatches
            val isIncognitoModeIndex = patternMatch.first().index
            val returnVoidIndex = patternMatch.last().index

            // Replace the incognito check (first instruction) with our runtime check
            replaceInstruction(
                index = isIncognitoModeIndex,
                smaliInstruction = "invoke-static {}, $INCOGNITO_RUNTIME_CLASS->shouldAllowClipboard()Z"
            )
            // Replace the next instruction (move-result) with our move-result
            replaceInstruction(
                index = isIncognitoModeIndex + 1,
                smaliInstruction = "move-result v0"
            )
            // Replace IF_EQZ with our conditional check
            replaceInstruction(
                index = isIncognitoModeIndex + 2,
                smaliInstruction = "if-eqz v0, :allow_clipboard"
            )
            // Add the skip label after return-void
            addInstructions(
                index = returnVoidIndex + 1,
                smaliInstructions = ":allow_clipboard"
            )
        }

        // Enable voice typing in incognito - check preference at runtime
        EnableVoiceTypingFingerprint.method.apply {
            val isIncognitoEnabledIndex =
                EnableVoiceTypingFingerprint.instructionMatches.last().index
            val isIncognitoEnabledInstruction =
                getInstruction<OneRegisterInstruction>(isIncognitoEnabledIndex)
            val isIncognitoEnabledRegister = isIncognitoEnabledInstruction.registerA

            addInstructions(
                index = isIncognitoEnabledIndex,
                smaliInstructions = """
                    invoke-static {}, $INCOGNITO_RUNTIME_CLASS->shouldAllowVoice()Z
                    move-result v$isIncognitoEnabledRegister
                """.trimIndent()
            )
        }
    }
}
