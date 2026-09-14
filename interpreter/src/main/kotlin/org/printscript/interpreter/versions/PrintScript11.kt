package org.printscript.interpreter.versions

import org.printscript.interpreter.ExpressionEvaluator
import org.printscript.interpreter.statements.BuiltInFunction
import org.printscript.interpreter.statements.BuiltInFunctions
import org.printscript.interpreter.statements.IfExecutor
import org.printscript.interpreter.statements.StatementExecutor
import org.printscript.interpreter.statements.StatementExecutors

object PrintScript11 {
    val BUILT_INS: Map<String, BuiltInFunction> =
        PrintScript10.BUILT_INS +
            mapOf(
                "readInput" to BuiltInFunctions.READ_INPUT,
                "readEnv" to BuiltInFunctions.READ_ENV,
            )

    private val all: List<StatementExecutor> by lazy {
        val evaluator = ExpressionEvaluator(BUILT_INS)
        PrintScript10.statementExecutors(evaluator) + IfExecutor(evaluator, StatementExecutors { all })
    }

    fun statementExecutors(): List<StatementExecutor> = all

    fun executors(): StatementExecutors = StatementExecutors { all }
}
