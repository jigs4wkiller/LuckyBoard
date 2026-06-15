package dev.jason.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import dev.jason.gboardpatches.patches.gboard.features.signaturebypass.gboardSignatureBypassBytecodePatch
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

@Suppress("unused")
internal val gboardIncognitoEnhancementsBytecodePatch = bytecodePatch(
    description = "Enable clipboard and voice typing in incognito mode, and force always-incognito mode."
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(gboardSignatureBypassBytecodePatch)

    execute {
        // Always incognito
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
                    smaliInstruction = "const/4 v$isIncognitoModeRegister, 0x1"
                )
            }
            else -> {
                // returnEarly(true) equivalent
                method.replaceInstruction(0, "const/4 v0, 0x1")
                if (method.returnType == "Z") {
                    method.replaceInstruction(1, "return v0")
                }
            }
        }

        // Enable clipboard in incognito
        OnPrimaryClipChangedFingerprint.method.apply {
            val patternMatch = OnPrimaryClipChangedFingerprint.instructionMatches
            val isIncognitoModeIndex = patternMatch.first().index
            val returnVoidIndex = patternMatch.last().index
            val instructionsToRemoveCount = (returnVoidIndex - isIncognitoModeIndex) + 1

            removeInstructions(
                index = isIncognitoModeIndex,
                count = instructionsToRemoveCount
            )
        }

        // Enable voice typing in incognito
        EnableVoiceTypingFingerprint.method.apply {
            val isIncognitoEnabledIndex =
                EnableVoiceTypingFingerprint.instructionMatches.last().index
            val isIncognitoEnabledInstruction =
                getInstruction<OneRegisterInstruction>(isIncognitoEnabledIndex)
            val isIncognitoEnabledRegister = isIncognitoEnabledInstruction.registerA

            addInstruction(
                index = isIncognitoEnabledIndex,
                smaliInstructions = "const/4 v$isIncognitoEnabledRegister, 0x0"
            )
        }
    }
}
