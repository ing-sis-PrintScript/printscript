package org.printscript.parser.versions

import org.printscript.parser.Parser
import org.printscript.parser.SkipToSemicolon
import org.printscript.parser.expressions.PrecedenceExpressionParser
import org.printscript.parser.statements.BlockParser
import org.printscript.parser.statements.IfParser
import org.printscript.parser.statements.StatementParser
import org.printscript.parser.statements.StatementParsers
import org.printscript.token.TokenType

object PrintScript11 {
    private val parsers: List<StatementParser> by lazy {
        val expressions = PrecedenceExpressionParser(setOf(TokenType.READ_INPUT, TokenType.READ_ENV))
        val blocks = BlockParser(StatementParsers { parsers })
        PrintScript10.statementParsers(expressions) + IfParser(expressions, blocks)
    }

    fun statementParsers(): List<StatementParser> = parsers

    fun parser(): Parser = Parser(StatementParsers { parsers }, SkipToSemicolon)
}
