package org.printscript.formatter.rules

import org.printscript.token.TokenType

// Lo poco que el recorrido necesita recordar y no entra en dos tokens.
//
// lastStatementHead: con que token arranco la sentencia ANTERIOR. La usa
// line-breaks-after-println, que tiene que reconocer "vengo de un println" estando
// parada en el primer token de la sentencia que sigue.
//
// blockDepth: cuantos bloques abiertos hay. La usa la sangria, que no es una regla
// sino un paso del formatter.
data class FormattingState(
    val lastStatementHead: TokenType? = null,
    val blockDepth: Int = 0,
)
