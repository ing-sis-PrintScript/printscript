package org.printscript.parser.token

import org.printscript.token.TokenType


internal fun TokenType.describe(): String = when (this) {
    TokenType.LET -> "'let'"
    TokenType.PRINTLN -> "'println'"
    TokenType.TYPE_NUMBER -> "el tipo 'number'"
    TokenType.TYPE_STRING -> "el tipo 'string'"
    TokenType.IDENTIFIER -> "un identificador"
    TokenType.NUMBER_LITERAL -> "un numero"
    TokenType.STRING_LITERAL -> "un string"
    TokenType.COLON -> "':'"
    TokenType.ASSIGN -> "'='"
    TokenType.SEMICOLON -> "';'"
    TokenType.LPAREN -> "'('"
    TokenType.RPAREN -> "')'"
    TokenType.PLUS -> "'+'"
    TokenType.MINUS -> "'-'"
    TokenType.STAR -> "'*'"
    TokenType.SLASH -> "'/'"
    TokenType.EOF -> "el fin del archivo"
}
