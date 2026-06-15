package dev.jason.gboardpatches.patches.gboard.features.incognito

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod

internal fun MutableMethod.returnEarly(value: Boolean) {
    val returnInstruction = if (returnType == "V") {
        "return-void"
    } else {
        "return $value"
    }
    replaceInstruction(0, returnInstruction)
}
