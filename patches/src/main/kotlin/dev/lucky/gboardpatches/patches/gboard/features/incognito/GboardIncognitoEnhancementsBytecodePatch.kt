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
import dev.lucky.gboardpatches.patches.gboard.shared.findMutableMethodOrThrow
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
        // Always incognito - replace jak.Z(EditorInfo)->boolean with runtime call
        try {
            val incognitoMethod = findMutableMethodOrThrow(
                classType = "Ljak;",
                name = "Z",
                returnType = "Z",
                parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;")
            )
            incognitoMethod.clearExceptionHandlers()
            val instructionCount = incognitoMethod.implementation?.instructions?.size ?: 0
            incognitoMethod.removeInstructions(0, instructionCount)
            incognitoMethod.addInstructions(0, """
                invoke-static {}, $INCOGNITO_RUNTIME_CLASS->shouldForceIncognito()Z
                move-result v0
                return v0
            """.trimIndent())
        } catch (e: Exception) {
            // Fallback: try old fingerprint approach
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
                    method.replaceInstruction(0, "const/4 v0, 0x1")
                    if (method.returnType == "Z") {
                        method.replaceInstruction(1, "return v0")
                    }
                }
            }
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
            // Fingerprint didn't match in new APK - try to find and patch dch.onPrimaryClipChanged
            try {
                val clipMethod = findMutableMethodOrThrow(
                    classType = "Ldch;",
                    name = "onPrimaryClipChanged",
                    returnType = "V",
                    parameterTypes = listOf()
                )
                clipMethod.clearExceptionHandlers()
                val instructionCount = clipMethod.implementation?.instructions?.size ?: 0
                clipMethod.removeInstructions(0, instructionCount)
                clipMethod.addInstructions(0, "return-void")
            } catch (e2: Exception) {
                // Ignore - clipboard check might not exist in this version
            }
        }

        // Enable voice typing in incognito - check preference at runtime
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
            // Fingerprint didn't match - voice typing check might not exist in this version
        }
    }
}
