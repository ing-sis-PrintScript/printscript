package org.printscript.interpreter

import org.printscript.interpreter.statements.StatementExecutor

// Que statements se saben ejecutar en PrintScript 1.1.
//
// Por ahora los mismos que 1.0. Cuando entre el if, se suma un IfExecutor a esta lista y
// ni Interpreter ni los executors existentes se tocan.
object PrintScript11 {
    fun statementExecutors(): List<StatementExecutor> = PrintScript10.statementExecutors()
}
