package org.printscript.parser.versions

import org.printscript.parser.Parser
import org.printscript.parser.PrecedenceExpressionParser
import org.printscript.parser.SkipToSemicolon
import org.printscript.parser.statements.BlockParser
import org.printscript.parser.statements.IfParser
import org.printscript.parser.statements.StatementParser
import org.printscript.parser.statements.StatementParsers
import org.printscript.token.TokenType

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
        // readInput y readEnv devuelven un valor, asi que se pueden usar adentro
        // de una expresion. Es lo unico que 1.1 le agrega a la gramatica de
        // expresiones; el resto --precedencia, unarios, parentesis-- es igual.
        val expressions = PrecedenceExpressionParser(setOf(TokenType.READ_INPUT, TokenType.READ_ENV))
        val blocks = BlockParser(StatementParsers { parsers })
        PrintScript10.statementParsers(expressions) + IfParser(expressions, blocks)
    }

    fun statementParsers(): List<StatementParser> = parsers

    fun parser(): Parser = Parser(StatementParsers { parsers }, SkipToSemicolon)
}
