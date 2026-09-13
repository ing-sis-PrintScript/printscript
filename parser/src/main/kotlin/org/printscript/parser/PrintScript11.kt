package org.printscript.parser

import org.printscript.parser.statements.BlockParser
import org.printscript.parser.statements.IfParser
import org.printscript.parser.statements.StatementParser
import org.printscript.parser.statements.StatementParsers

// Que statements se saben parsear en PrintScript 1.1: los de 1.0 mas el if.
//
// El const no aparece aca porque no necesito un parser nuevo: lo maneja el
// mismo DeclarationParser, que ya acepta LET y CONST. Lo que separa las
// versiones es el lexer, que en 1.0 ni siquiera produce el token CONST.
object PrintScript11 {
    // La lista se referencia a si misma: el IfParser tiene que poder parsear
    // cualquier statement para el cuerpo del if, y el IfParser esta adentro de
    // esa misma lista. 'lazy' es lo que rompe el ciclo --el lambda corre recien
    // cuando alguien parsea, y para entonces la lista ya esta armada--.
    private val parsers: List<StatementParser> by lazy {
        val expressions = PrintScript10ExpressionParser()
        val blocks = BlockParser(StatementParsers { parsers })
        PrintScript10.statementParsers() + IfParser(expressions, blocks)
    }

    fun statementParsers(): List<StatementParser> = parsers

    fun parser(): Parser = Parser(StatementParsers { parsers }, SkipToSemicolon)
}
