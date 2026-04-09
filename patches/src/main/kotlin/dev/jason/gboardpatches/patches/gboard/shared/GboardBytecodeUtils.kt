package dev.jason.gboardpatches.patches.gboard.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

internal fun BytecodePatchContext.mutableClass(type: String) = mutableClassDefBy(type)

internal fun BytecodePatchContext.addFieldIfMissing(
    classType: String,
    fieldName: String,
    fieldType: String,
    accessFlags: Int
) {
    val mutableClass = mutableClass(classType)
    if (mutableClass.fields.any { it.name == fieldName && it.type == fieldType }) return

    mutableClass.fields.add(
        ImmutableField(
            classType,
            fieldName,
            fieldType,
            accessFlags,
            null,
            null,
            null
        ).toMutable()
    )
}

internal fun BytecodePatchContext.findMutableMethodOrThrow(
    classType: String,
    name: String,
    returnType: String,
    parameterTypes: List<String>
): MutableMethod {
    val mutableClass = mutableClass(classType)
    return mutableClass.methods.firstOrNull {
        it.name == name && it.returnType == returnType && it.parameterTypes == parameterTypes
    } ?: error("Could not find $classType->$name(${parameterTypes.joinToString("")})$returnType")
}

internal fun BytecodePatchContext.addHelperMethodIfMissing(
    classType: String,
    name: String,
    parameterTypes: List<String>,
    returnType: String,
    accessFlags: Int,
    registerCount: Int,
    body: String
) {
    val mutableClass = mutableClass(classType)
    if (mutableClass.methods.any {
            it.name == name && it.returnType == returnType && it.parameterTypes == parameterTypes
        }) {
        return
    }

    mutableClass.methods.add(
        ImmutableMethod(
            classType,
            name,
            parameterTypes.map { ImmutableMethodParameter(it, null, null) },
            returnType,
            accessFlags,
            null,
            null,
            MutableMethodImplementation(registerCount)
        ).toMutable().apply {
            addInstructions(0, body.trimIndent())
        }
    )
}

internal fun MutableMethod.indexOfFirstMethodCall(
    definingClass: String,
    name: String,
    returnType: String? = null,
    parameterTypes: List<String>? = null
): Int {
    val instructions = implementation?.instructions ?: return -1
    return instructions.indexOfFirst { instruction ->
        val reference = (instruction as? ReferenceInstruction)?.reference as? MethodReference ?: return@indexOfFirst false
        reference.definingClass == definingClass &&
            reference.name == name &&
            (returnType == null || reference.returnType == returnType) &&
            (parameterTypes == null || reference.parameterTypes.toList() == parameterTypes)
    }
}

internal fun MutableMethod.indexOfFirstMoveResultAfter(instructionIndex: Int): Int {
    val instructions = implementation?.instructions ?: return -1
    for (index in (instructionIndex + 1) until instructions.size) {
        val opcodeName = instructions[index].opcode.name
        if (opcodeName.startsWith("MOVE_RESULT")) return index
    }
    return -1
}

internal fun MutableMethod.indexOfFirstConst4LiteralFollowedByIfEqz(literal: Int): Int {
    val instructions = implementation?.instructions ?: return -1
    for (index in 0 until instructions.lastIndex) {
        val instruction = instructions[index]
        if (instruction.opcode.name == "CONST_4" &&
            (instruction as? NarrowLiteralInstruction)?.narrowLiteral == literal &&
            instructions[index + 1].opcode.name == "IF_EQZ"
        ) {
            return index
        }
    }
    return -1
}

internal fun MutableMethod.indexOfFirstInstructionWritingRegister(register: Int): Int {
    val instructions = implementation?.instructions ?: return -1
    return instructions.indexOfFirst { instruction ->
        (instruction as? OneRegisterInstruction)?.registerA == register
    }
}
