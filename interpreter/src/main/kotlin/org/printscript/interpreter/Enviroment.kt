package org.printscript.interpreter

import org.printscript.ast.DeclaredType
import org.printscript.common.Range

private data class VariableSymbol(val value: Any?, val type: DeclaredType)

class Environment {
    private val memory = mutableMapOf<String, VariableSymbol>()

    fun declare(name: String, type: DeclaredType, value: Any?, range: Range) {
        if (memory.containsKey(name)) {
            throw RuntimeError("La variable '$name' ya fue declarada.", range)
        }
        if (value != null) {
            checkType(type, value, range)
        }
        memory[name] = VariableSymbol(value, type)
    }

    fun assign(name: String, value: Any, range: Range) {
        val existing = memory[name]
            ?: throw RuntimeError("La variable '$name' no ha sido declarada.", range)

        checkType(existing.type, value, range)
        memory[name] = VariableSymbol(value, existing.type)
    }

    fun get(name: String, range: Range): Any {
        val symbol = memory[name]
            ?: throw RuntimeError("La variable '$name' no ha sido declarada.", range)

        return symbol.value
            ?: throw RuntimeError("La variable '$name' no ha sido inicializada.", range)
    }

    private fun checkType(expectedType: DeclaredType, value: Any, range: Range) {
        val isValid = when (expectedType) {
            DeclaredType.NUMBER -> value is Number
            DeclaredType.STRING -> value is String
        }
        if (!isValid) {
            throw RuntimeError("Se esperaba un tipo '$expectedType' pero se obtuvo un valor distinto.", range)
        }
    }
}