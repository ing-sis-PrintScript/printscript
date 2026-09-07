package org.printscript.interpreter.statements

import org.printscript.common.Result
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.PrintScriptValue
import org.printscript.interpreter.io.PrintScriptIO

/**
 * Una función del lenguaje que no se declara en PrintScript sino que provee el
 * intérprete (hoy solo println; en 1.1 se suman readInput/readEnv).
 */
fun interface BuiltInFunction {
    fun call(
        argument: PrintScriptValue,
        io: PrintScriptIO,
    ): Result<Unit, InterpreterError>
}

/**
 * Qué funciones built-in existen en PrintScript 1.0 y qué hace cada una.
 *
 * Antes de esto, reconocer una llamada era un `if (name == "println")` metido
 * adentro de Interpreter — un string mágico comparado a mano, sin relación
 * con el resto del pipeline, que usa TokenType/enums para todo lo demás.
 *
 * Con el registro, reconocer una función es una búsqueda en un mapa, igual que
 * PrintScript10.KEYWORDS en el lexer. Sumar una función nueva en 1.1 es
 * agregar una entrada acá, no tocar el executor que la despacha.
 */
object BuiltInFunctions {
    val REGISTRY: Map<String, BuiltInFunction> =
        mapOf(
            "println" to
                BuiltInFunction { argument, io ->
                    io.print(argument.toString())
                    Result.Success(Unit)
                },
        )
}
