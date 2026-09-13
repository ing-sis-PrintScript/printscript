package org.printscript.interpreter.versions

import org.printscript.interpreter.ExpressionEvaluator
import org.printscript.interpreter.statements.AssignmentExecutor
import org.printscript.interpreter.statements.BuiltInFunction
import org.printscript.interpreter.statements.BuiltInFunctions
import org.printscript.interpreter.statements.DeclarationExecutor
import org.printscript.interpreter.statements.ExpressionStatementExecutor
import org.printscript.interpreter.statements.StatementExecutor
import org.printscript.interpreter.statements.StatementExecutors

object PrintScript10 {
    val BUILT_INS: Map<String, BuiltInFunction> = mapOf("println" to BuiltInFunctions.PRINTLN)

    fun statementExecutors(): List<StatementExecutor> = statementExecutors(ExpressionEvaluator(BUILT_INS))

    internal fun statementExecutors(evaluator: ExpressionEvaluator): List<StatementExecutor> =
        listOf(
            DeclarationExecutor(evaluator),
            AssignmentExecutor(evaluator),
            ExpressionStatementExecutor(evaluator),
        )

    fun executors(): StatementExecutors {
        val all = statementExecutors()
        return StatementExecutors { all }
    }
}
