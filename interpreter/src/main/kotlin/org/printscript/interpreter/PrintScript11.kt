package org.printscript.interpreter

import org.printscript.interpreter.statements.BuiltInFunction
import org.printscript.interpreter.statements.BuiltInFunctions
import org.printscript.interpreter.statements.IfExecutor
import org.printscript.interpreter.statements.StatementExecutor
import org.printscript.interpreter.statements.StatementExecutors

/**
 * Qué statements y qué funciones built-in existen en PrintScript 1.1.
 *
 * Las dos listas se arman sumando a las de 1.0, igual que PrintScript11.KEYWORDS
 * en el lexer: lo de antes sigue valiendo y lo nuevo se agrega.
 */
object PrintScript11 {
    val BUILT_INS: Map<String, BuiltInFunction> =
        PrintScript10.BUILT_INS +
            mapOf(
                "readInput" to BuiltInFunctions.READ_INPUT,
                "readEnv" to BuiltInFunctions.READ_ENV,
            )

    // La lista se referencia a si misma: el IfExecutor tiene que poder ejecutar
    // cualquier statement del cuerpo del if, y el IfExecutor esta adentro de esa
    // misma lista. 'lazy' es lo que rompe el ciclo --el lambda corre recien
    // cuando alguien ejecuta, y para entonces la lista ya esta armada--.
    private val all: List<StatementExecutor> by lazy {
        val evaluator = ExpressionEvaluator(BUILT_INS)
        PrintScript10.statementExecutors(evaluator) + IfExecutor(evaluator, StatementExecutors { all })
    }

    fun statementExecutors(): List<StatementExecutor> = all

    fun executors(): StatementExecutors = StatementExecutors { all }
}
