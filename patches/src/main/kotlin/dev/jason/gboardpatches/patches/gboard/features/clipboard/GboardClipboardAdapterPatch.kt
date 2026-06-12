package dev.jason.gboardpatches.patches.gboard.features.clipboard

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import dev.jason.gboardpatches.patches.gboard.shared.findMutableMethodOrThrow
import dev.jason.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.jason.gboardpatches.patches.gboard.shared.returnInstructionIndices

// Nur noch Preview Lines + Order Index + Column Count (keine Timer)
internal val gboardClipboardItemBindPatch = bytecodePatch(
    description = "移植 clipboard preview lines / order index (ohne Creation Time & Countdown)"
) {
    dependsOn(gboardPatchesExtensionCarrierPatch)

    execute {
        val mutableMethod = findMutableMethodOrThrow(
            classType = CLIPBOARD_ADAPTER_CLASS,
            name = "p",
            returnType = "V",
            parameterTypes = listOf(RECYCLER_VIEW_HOLDER_CLASS, "I")
        )
        val returnIndices = mutableMethod.returnInstructionIndices()
        if (returnIndices.isEmpty()) {
            error("Could not resolve return instructions in Lemk.p(Lkm;I)")
        }

        for (returnIndex in returnIndices.sortedDescending()) {
            // Nur noch die nicht-Zeit-Hooks aufrufen
            mutableMethod.addInstructions(returnIndex, """
                invoke-static/range {p0 .. p2}, ${CLIPBOARD_RUNTIME_CLASS}->afterItemBind(Ljava/lang/Object;Ljava/lang/Object;I)V
            """.trimIndent())
        }
    }
}