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
//
// pendingWhitespace: el espacio que el fuente traia delante del token que se esta
// escribiendo. Llega como un token WHITESPACE que el formatter no emite: lo guarda
// aca y lo usa como respaldo cuando ninguna regla opina. Es la unica pieza de
// estado que existe porque el espacio es un token y no un campo de Token.
data class FormattingState(
    val lastStatementHead: TokenType? = null,
    val blockDepth: Int = 0,
    val pendingWhitespace: String = "",
)
