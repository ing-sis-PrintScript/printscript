package org.printscript.token

enum class TokenType {
    // keywords
    LET, PRINTLN, TYPE_NUMBER, TYPE_STRING,

    // identificadores
    IDENTIFIER,

    // literales
    NUMBER_LITERAL, STRING_LITERAL,

    // simbolos
    COLON, ASSIGN, SEMICOLON, LPAREN, RPAREN,
    PLUS, MINUS, STAR, SLASH,

    // control
    EOF,
}
