package org.printscript.lexer

import org.printscript.lexer.rules.NumberRule
import org.printscript.lexer.rules.StringRule
import org.printscript.lexer.rules.SymbolRule
import org.printscript.lexer.rules.TokenRule
import org.printscript.lexer.rules.WordRule
import org.printscript.token.TokenType

/**
 * La definición léxica de PrintScript 1.0: qué reglas existen y en qué orden.
 *
 * Cuando salga la 1.1 se agrega un PrintScript11 con estas reglas más las nuevas.
 * Ni Lexer ni TokenMatcher ni ninguna regla existente se tocan.
 */
object PrintScript10 {
    val KEYWORDS: Map<String, TokenType> =
        mapOf(
            "let" to TokenType.LET,
            "println" to TokenType.PRINTLN,
            "number" to TokenType.TYPE_NUMBER,
            "string" to TokenType.TYPE_STRING,
        )

    val SYMBOLS: Map<Char, TokenType> =
        mapOf(
            ':' to TokenType.COLON,
            ';' to TokenType.SEMICOLON,
            '=' to TokenType.ASSIGN,
            '(' to TokenType.LPAREN,
            ')' to TokenType.RPAREN,
            '+' to TokenType.PLUS,
            '-' to TokenType.MINUS,
            '*' to TokenType.STAR,
            '/' to TokenType.SLASH,
        )

    /**
     * EL ORDEN IMPORTA: gana la primera regla que contesta algo distinto de null.
     * Acá las cuatro son excluyentes (un caracter no puede ser dígito y comilla a
     * la vez), pero en cuanto agregues operadores de dos caracteres como "==",
     * esa regla va a tener que ir ANTES que SymbolRule.
     */
    val RULES: List<TokenRule> =
        listOf(
            NumberRule,
            WordRule(KEYWORDS),
            StringRule,
            SymbolRule(SYMBOLS),
        )
}
