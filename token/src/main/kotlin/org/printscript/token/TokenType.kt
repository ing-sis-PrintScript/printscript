package org.printscript.token

// El vocabulario de PrintScript, para TODAS las versiones.
//
// El enum no se parte por version: lo que cambia entre 1.0 y 1.1 no son los tipos sino
// QUE PALABRAS mapean a ellos, y eso vive en los mapas de lexer/PrintScript10 y
// lexer/PrintScript11.
//
// De ahi sale gratis lo que pide la consigna: con las reglas de 1.0, "if" no esta en el
// mapa de keywords, asi que WordRule lo lexea como IDENTIFIER y el parser reporta un
// error de sintaxis. Usar 1.1 con --version 1.0 falla solo, sin un chequeo especial.
enum class TokenType {
    // keywords
    LET, PRINTLN, TYPE_NUMBER, TYPE_STRING,

    // keywords de 1.1
    CONST, IF, ELSE, TYPE_BOOLEAN, READ_INPUT, READ_ENV,

    // identificadores
    IDENTIFIER,

    // literales
    NUMBER_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL,

    // simbolos
    COLON, ASSIGN, SEMICOLON, LPAREN, RPAREN,
    PLUS, MINUS, STAR, SLASH,

    // simbolos de 1.1
    LBRACE, RBRACE,

    // control
    EOF,
}
