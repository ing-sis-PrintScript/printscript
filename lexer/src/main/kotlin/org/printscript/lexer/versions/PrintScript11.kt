package org.printscript.lexer.versions

import org.printscript.lexer.rules.NumberRule
import org.printscript.lexer.rules.StringRule
import org.printscript.lexer.rules.SymbolRule
import org.printscript.lexer.rules.TokenRule
import org.printscript.lexer.rules.WordRule
import org.printscript.token.TokenType

object PrintScript11 {
    val KEYWORDS: Map<String, TokenType> =
        PrintScript10.KEYWORDS +
            mapOf(
                "const" to TokenType.CONST,
                "if" to TokenType.IF,
                "else" to TokenType.ELSE,
                "boolean" to TokenType.TYPE_BOOLEAN,
                "true" to TokenType.BOOLEAN_LITERAL,
                "false" to TokenType.BOOLEAN_LITERAL,
                "readInput" to TokenType.READ_INPUT,
                "readEnv" to TokenType.READ_ENV,
            )

    val SYMBOLS: Map<String, TokenType> =
        PrintScript10.SYMBOLS +
            mapOf(
                "{" to TokenType.LBRACE,
                "}" to TokenType.RBRACE,
            )

    val RULES: List<TokenRule> =
        listOf(
            NumberRule,
            WordRule(KEYWORDS),
            StringRule,
            SymbolRule(SYMBOLS),
        )
}
