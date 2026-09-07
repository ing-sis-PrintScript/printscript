package org.printscript.lexer

import org.printscript.lexer.rules.NumberRule
import org.printscript.lexer.rules.StringRule
import org.printscript.lexer.rules.SymbolRule
import org.printscript.lexer.rules.TokenRule
import org.printscript.lexer.rules.WordRule
import org.printscript.token.TokenType

object PrintScript10 {
    val KEYWORDS: Map<String, TokenType> =
        mapOf(
            "let" to TokenType.LET,
            "println" to TokenType.PRINTLN,
            "number" to TokenType.TYPE_NUMBER,
            "string" to TokenType.TYPE_STRING,
        )

    val SYMBOLS: Map<String, TokenType> =
        mapOf(
            ":" to TokenType.COLON,
            ";" to TokenType.SEMICOLON,
            "=" to TokenType.ASSIGN,
            "(" to TokenType.LPAREN,
            ")" to TokenType.RPAREN,
            "+" to TokenType.PLUS,
            "-" to TokenType.MINUS,
            "*" to TokenType.STAR,
            "/" to TokenType.SLASH,
        )

    val RULES: List<TokenRule> =
        listOf(
            NumberRule,
            WordRule(KEYWORDS),
            StringRule,
            SymbolRule(SYMBOLS),
        )
}
