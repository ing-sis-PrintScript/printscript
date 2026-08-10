package org.printscript.lexer

import org.printscript.token.TokenType

/**
 * El vocabulario de PrintScript 1.0: qué palabras son keywords y qué símbolos existen.
 *
 * Está separado del algoritmo a propósito. Cuando salga la 1.1 se agrega un
 * PrintScript11 con estas mismas entradas más las nuevas, y ni Lexer ni
 * TokenMatcher se tocan.
 */
object PrintScript10 {

    val KEYWORDS: Map<String, TokenType> = mapOf(
        "let" to TokenType.LET,
        "println" to TokenType.PRINTLN,
        "number" to TokenType.TYPE_NUMBER,
        "string" to TokenType.TYPE_STRING,
    )

    val SYMBOLS: Map<Char, TokenType> = mapOf(
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
}
