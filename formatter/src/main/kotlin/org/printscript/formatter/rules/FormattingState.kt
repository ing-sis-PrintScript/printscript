package org.printscript.formatter.rules

import org.printscript.token.TokenType

// Lo poco que una regla necesita del recorrido y no entra en dos tokens.
//
// Por ahora una sola cosa: con que token arranco la sentencia ANTERIOR. La usa
// line-breaks-after-println, que tiene que reconocer "vengo de un println" estando
// parada en el primer token de la sentencia que sigue.
//
// blockDepth entra cuando llegue indent-inside-if (1.1): va a ser otro campo con default.
data class FormattingState(val lastStatementHead: TokenType? = null)
