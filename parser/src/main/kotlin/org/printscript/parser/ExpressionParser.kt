package org.printscript.parser

import org.printscript.ast.Expression
import org.printscript.common.PrintScriptError
import org.printscript.common.Result

/**
 * Punto de extensión para la gramática de expresiones.
 *
 * El core no sabe qué es una expresión válida: sabe que hay alguien que, dado un
 * TokenStream, devuelve una Expression o un error. Qué operadores existen, con
 * qué precedencia y qué literales se aceptan lo decide la implementación que
 * inyecta cada versión del lenguaje.
 *
 * Es el espejo de StatementParser: sin esta interfaz los statements eran
 * extensibles y las expresiones no, así que agregar booleanos o comparaciones en
 * 1.1 obligaba a modificar una clase concreta del core.
 */
interface ExpressionParser {

    fun parse(stream: TokenStream): Result<Expression, PrintScriptError>
}
