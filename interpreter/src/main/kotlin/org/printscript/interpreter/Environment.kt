package org.printscript.interpreter

import org.printscript.ast.DeclarationKind
import org.printscript.ast.DeclaredType
import org.printscript.common.Range
import org.printscript.common.Result

private data class VariableSymbol(
    val value: PrintScriptValue?,
    val type: DeclaredType,
    val kind: DeclarationKind,
)

class Environment private constructor(
    private val memory: Map<String, VariableSymbol>,
) {
    constructor() : this(emptyMap())

    fun declare(
        name: String,
        type: DeclaredType,
        value: PrintScriptValue?,
        range: Range,
        kind: DeclarationKind,
    ): Result<Environment, InterpreterError> {
        if (memory.containsKey(name)) {
            return Result.Failure(InterpreterError("La variable '$name' ya fue declarada.", range))
        }

        if (value != null) {
            val typeCheck = checkType(type, value, range)
            if (typeCheck is Result.Failure) return typeCheck
        }

        val newMemory = memory + (name to VariableSymbol(value, type, kind))
        return Result.Success(Environment(newMemory))
    }

    fun assign(
        name: String,
        value: PrintScriptValue,
        range: Range,
    ): Result<Environment, InterpreterError> {
        val existing =
            memory[name]
                ?: return Result.Failure(InterpreterError("La variable '$name' no ha sido declarada.", range))

        if (existing.kind == DeclarationKind.CONST) {
            return Result.Failure(InterpreterError("La constante '$name' no se puede reasignar.", range))
        }

        val typeCheck = checkType(existing.type, value, range)
        if (typeCheck is Result.Failure) return typeCheck

        val newMemory = memory + (name to VariableSymbol(value, existing.type, existing.kind))
        return Result.Success(Environment(newMemory))
    }

    fun get(
        name: String,
        range: Range,
    ): Result<PrintScriptValue, InterpreterError> {
        val symbol =
            memory[name]
                ?: return Result.Failure(InterpreterError("La variable '$name' no ha sido declarada.", range))

        return symbol.value?.let { Result.Success(it) }
            ?: Result.Failure(InterpreterError("La variable '$name' no ha sido inicializada.", range))
    }

    private fun checkType(
        expectedType: DeclaredType,
        value: PrintScriptValue,
        range: Range,
    ): Result<Unit, InterpreterError> {
        val isValid =
            when (expectedType) {
                DeclaredType.NUMBER -> value is PrintScriptValue.NumberValue
                DeclaredType.STRING -> value is PrintScriptValue.StringValue
                DeclaredType.BOOLEAN -> value is PrintScriptValue.BooleanValue
            }
        return if (!isValid) {
            Result.Failure(
                InterpreterError("Se esperaba un tipo '$expectedType' pero se obtuvo un valor distinto.", range),
            )
        } else {
            Result.Success(Unit)
        }
    }
}
