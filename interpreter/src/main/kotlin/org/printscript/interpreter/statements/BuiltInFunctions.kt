package org.printscript.interpreter.statements

import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.PrintScriptValue
import org.printscript.interpreter.io.PrintScriptIO

/**
 * Una función del lenguaje que no se declara en PrintScript sino que provee el
 * intérprete: println en 1.0, readInput y readEnv en 1.1.
 *
 * El valor que devuelve es nullable porque no todas producen uno: println
 * escribe y no deja nada. Es la misma convención que usa todo el proyecto —
 * null es "no hay", no un error. Así "let x: string = println(...);" falla con
 * un mensaje que dice la verdad, en vez de devolver un valor inventado.
 *
 * Recibe el Range de la llamada porque ahora puede fallar, y un
 * InterpreterError sin posición no sirve para nada.
 */
fun interface BuiltInFunction {
    fun call(
        argument: PrintScriptValue,
        range: Range,
        io: PrintScriptIO,
    ): Result<PrintScriptValue?, InterpreterError>
}

/**
 * Las funciones built-in que existen, una por una. Cuáles conoce cada versión
 * del lenguaje se arma en PrintScript10 / PrintScript11, igual que las
 * KEYWORDS del lexer: la función es la misma, lo que cambia por versión es qué
 * nombres están disponibles.
 */
object BuiltInFunctions {
    val PRINTLN =
        BuiltInFunction { argument, _, io ->
            io.print(argument.toString())
            Result.Success(null)
        }

    // El prompt lo imprime readInput, no el PrintScriptIO: que el prompt aparezca
    // en la salida del programa es una regla del lenguaje, no de por dónde entra
    // y sale el texto.
    val READ_INPUT =
        BuiltInFunction { argument, range, io ->
            text(argument, "readInput", range).map { prompt ->
                io.print(prompt)
                PrintScriptValue.StringValue(io.read(prompt))
            }
        }

    // Si la variable no existe falla, no devuelve string vacío: un programa que
    // lee una variable que nadie definió está roto y tiene que enterarse.
    val READ_ENV =
        BuiltInFunction { argument, range, io ->
            text(argument, "readEnv", range).flatMap { name ->
                io.env(name)?.let { Result.Success(PrintScriptValue.StringValue(it)) }
                    ?: Result.Failure(
                        InterpreterError("La variable de entorno '$name' no está definida.", range),
                    )
            }
        }

    // Las dos funciones de 1.1 esperan un string. Que el argumento lo sea se
    // sabe recién acá, con el valor ya evaluado.
    private fun text(
        argument: PrintScriptValue,
        function: String,
        range: Range,
    ): Result<String, InterpreterError> =
        if (argument is PrintScriptValue.StringValue) {
            Result.Success(argument.value)
        } else {
            Result.Failure(InterpreterError("'$function' necesita un string y recibió '$argument'.", range))
        }
}
