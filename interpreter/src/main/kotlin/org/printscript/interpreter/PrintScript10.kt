package org.printscript.interpreter

import org.printscript.interpreter.statements.AssignmentExecutor
import org.printscript.interpreter.statements.BuiltInFunction
import org.printscript.interpreter.statements.BuiltInFunctions
import org.printscript.interpreter.statements.DeclarationExecutor
import org.printscript.interpreter.statements.ExpressionStatementExecutor
import org.printscript.interpreter.statements.StatementExecutor
import org.printscript.interpreter.statements.StatementExecutors

/**
 * Qué statements y qué funciones built-in existen en PrintScript 1.0.
 *
 * Espejo del PrintScript10 del lexer y del parser, que hacen lo mismo con
 * TokenRule y StatementParser respectivamente.
 */
object PrintScript10 {
    val BUILT_INS: Map<String, BuiltInFunction> = mapOf("println" to BuiltInFunctions.PRINTLN)

    fun statementExecutors(): List<StatementExecutor> = statementExecutors(ExpressionEvaluator(BUILT_INS))

    /**
     * Los executors que las dos versiones comparten, armados sobre el evaluator
     * que se les dé. Lo único que cambia entre versiones es qué built-ins conoce
     * ese evaluator, así que 1.1 reusa esta misma lista en vez de copiarla.
     */
    internal fun statementExecutors(evaluator: ExpressionEvaluator): List<StatementExecutor> =
        listOf(
            DeclarationExecutor(evaluator),
            AssignmentExecutor(evaluator),
            ExpressionStatementExecutor(evaluator),
        )

    // La lista se arma una vez por interpreter y despues se consulta, no se
    // rearma por cada statement ejecutado.
    fun executors(): StatementExecutors {
        val all = statementExecutors()
        return StatementExecutors { all }
    }
}
