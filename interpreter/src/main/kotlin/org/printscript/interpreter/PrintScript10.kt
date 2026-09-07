package org.printscript.interpreter

import org.printscript.interpreter.statements.AssignmentExecutor
import org.printscript.interpreter.statements.DeclarationExecutor
import org.printscript.interpreter.statements.ExpressionStatementExecutor
import org.printscript.interpreter.statements.StatementExecutor

/**
 * Qué statements se saben ejecutar en PrintScript 1.0.
 *
 * Espejo del PrintScript10 del lexer y del parser, que hacen lo mismo con
 * TokenRule y StatementParser respectivamente. Cuando salga la 1.1 se agrega
 * un PrintScript11 con estos executors más los nuevos (IfExecutor...) y ni
 * Interpreter ni los executors existentes se tocan: eso es Open/Closed.
 *
 * Los tres comparten el mismo ExpressionEvaluator porque no tiene estado.
 */
object PrintScript10 {
    fun statementExecutors(): List<StatementExecutor> {
        val evaluator = ExpressionEvaluator()
        return listOf(
            DeclarationExecutor(evaluator),
            AssignmentExecutor(evaluator),
            ExpressionStatementExecutor(evaluator),
        )
    }
}
