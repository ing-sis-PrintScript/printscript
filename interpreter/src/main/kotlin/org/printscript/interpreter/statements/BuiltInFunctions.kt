package org.printscript.interpreter.statements

import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.flatMap
import org.printscript.common.map
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.PrintScriptValue
import org.printscript.interpreter.io.PrintScriptIO

fun interface BuiltInFunction {
    fun call(
        argument: PrintScriptValue,
        range: Range,
        io: PrintScriptIO,
    ): Result<PrintScriptValue?, InterpreterError>
}

object BuiltInFunctions {
    val PRINTLN =
        BuiltInFunction { argument, _, io ->
            io.print(argument.toString())
            Result.Success(null)
        }

    val READ_INPUT =
        BuiltInFunction { argument, range, io ->
            text(argument, "readInput", range).map { prompt ->
                io.print(prompt)
                PrintScriptValue.StringValue(io.read(prompt))
            }
        }

    val READ_ENV =
        BuiltInFunction { argument, range, io ->
            text(argument, "readEnv", range).flatMap { name ->
                io.env(name)?.let { Result.Success(PrintScriptValue.StringValue(it)) }
                    ?: Result.Failure(
                        InterpreterError("La variable de entorno '$name' no está definida.", range),
                    )
            }
        }

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
