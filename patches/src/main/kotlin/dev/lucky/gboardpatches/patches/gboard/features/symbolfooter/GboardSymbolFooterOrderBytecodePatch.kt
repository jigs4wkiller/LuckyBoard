package dev.lucky.gboardpatches.patches.gboard.features.symbolfooter

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import dev.lucky.gboardpatches.patches.gboard.shared.findMutableMethodOrThrow
import dev.lucky.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.lucky.gboardpatches.patches.gboard.shared.returnInstructionIndices

private const val EXPRESSION_CORPUS_MANAGER_CLASS = "Leej;"

internal val gboardSymbolFooterOrderBytecodePatch = bytecodePatch(
    description = "Emoji tab reorder patch"
) {
    dependsOn(gboardPatchesExtensionCarrierPatch)

    execute {
        try {
            println("[LuckyBoard] Emoji reorder: patching ExpressionCorpusManager")
            patchExpressionCorpusManager()
            println("[LuckyBoard] Emoji reorder: patch applied successfully")
        } catch (e: Exception) {
            System.err.println("[LuckyBoard] Emoji reorder patch FAILED: " + e.message)
            e.printStackTrace()
        }
    }
}

context(context: BytecodePatchContext)
private fun patchExpressionCorpusManager() = with(context) {
    val mutableMethod = findMutableMethodOrThrow(
        classType = EXPRESSION_CORPUS_MANAGER_CLASS,
        name = "a",
        returnType = "Lpuv;",
        parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;", "Z")
    )
    val instructions = mutableMethod.implementation?.instructions
        ?: error("No instructions available in Leej.a")
    val returnIndices = mutableMethod.returnInstructionIndices()
        .filter { index ->
            instructions[index].opcode.name.uppercase().replace('-', '_') == "RETURN_OBJECT"
        }
    check(returnIndices.isNotEmpty()) { "Could not resolve RETURN_OBJECT in Leej.a" }

    returnIndices.asReversed().forEach { returnIndex ->
        val resultRegister = (instructions[returnIndex] as? OneRegisterInstruction)?.registerA
            ?: error("RETURN_OBJECT at $returnIndex does not expose registerA")
        mutableMethod.addInstructions(returnIndex, buildReorderDelegate(resultRegister))
    }
}

private fun buildReorderDelegate(register: Int): String = """
    invoke-static {p0, v$register}, Ldev/lucky/gboardpatches/extension/symbolfooter/GboardSymbolFooterOrderRuntime;->reorderExpressionCorpusList(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v$register

    check-cast v$register, Lpuv;
""".trimIndent()
