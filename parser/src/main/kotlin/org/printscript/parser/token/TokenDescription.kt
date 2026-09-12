package org.printscript.parser.token

import org.printscript.token.TokenType

internal fun TokenType.describe(): String =
    when (this) {
        TokenType.LET -> "'let'"
        TokenType.PRINTLN -> "'println'"
        TokenType.TYPE_NUMBER -> "el tipo 'number'"
        TokenType.TYPE_STRING -> "el tipo 'string'"
        TokenType.CONST -> "'const'"
        TokenType.IF -> "'if'"
        TokenType.ELSE -> "'else'"
        TokenType.TYPE_BOOLEAN -> "el tipo 'boolean'"
        TokenType.READ_INPUT -> "'readInput'"
        TokenType.READ_ENV -> "'readEnv'"
        TokenType.IDENTIFIER -> "un identificador"
        TokenType.NUMBER_LITERAL -> "un numero"
        TokenType.STRING_LITERAL -> "un string"
        TokenType.BOOLEAN_LITERAL -> "'true' o 'false'"
        TokenType.COLON -> "':'"
        TokenType.ASSIGN -> "'='"
        TokenType.SEMICOLON -> "';'"
        TokenType.LPAREN -> "'('"
        TokenType.RPAREN -> "')'"
        TokenType.PLUS -> "'+'"
        TokenType.MINUS -> "'-'"
        TokenType.STAR -> "'*'"
        TokenType.SLASH -> "'/'"
        TokenType.LBRACE -> "'{'"
        TokenType.RBRACE -> "'}'"
        TokenType.EOF -> "el fin del archivo"
    }
