package org.printscript.parser

import org.printscript.parser.statements.StatementParser

// Que statements se saben parsear en PrintScript 1.1.
//
// Por ahora son los mismos que 1.0: los tokens de 1.1 ya se lexean, pero todavia no hay
// quien los arme en un Statement. El if, el else y el const entran en los pasos que
// siguen, y van a ser parsers nuevos sumados a esta lista --sin tocar los de 1.0--.
object PrintScript11 {
    fun statementParsers(): List<StatementParser> = PrintScript10.statementParsers()

    fun parser(): Parser = Parser(statementParsers(), SkipToSemicolon)
}
