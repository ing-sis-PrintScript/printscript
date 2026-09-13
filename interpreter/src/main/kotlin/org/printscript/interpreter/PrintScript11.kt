package org.printscript.interpreter

import org.printscript.interpreter.statements.IfExecutor
import org.printscript.interpreter.statements.StatementExecutor
import org.printscript.interpreter.statements.StatementExecutors

// Que statements se saben ejecutar en PrintScript 1.1: los de 1.0 mas el if.
//
// El const no aparece aca porque no necesita un executor nuevo: lo ejecuta el
// mismo DeclarationExecutor, y que no se pueda reasignar es cosa de Environment.
object PrintScript11 {
    // La lista se referencia a si misma: el IfExecutor tiene que poder ejecutar
    // cualquier statement del cuerpo del if, y el IfExecutor esta adentro de esa
    // misma lista. 'lazy' es lo que rompe el ciclo --el lambda corre recien
    // cuando alguien ejecuta, y para entonces la lista ya esta armada--.
    private val all: List<StatementExecutor> by lazy {
        PrintScript10.statementExecutors() + IfExecutor(ExpressionEvaluator(), StatementExecutors { all })
    }

    fun statementExecutors(): List<StatementExecutor> = all

    fun executors(): StatementExecutors = StatementExecutors { all }
}
