package org.printscript.formatter.rules

import org.printscript.token.Token
import org.printscript.token.TokenType

// El primer token de la sentencia que sigue a un ';'. Es el unico lugar donde se puede
// escribir el espacio que queda DESPUES de una sentencia, y lo comparten las dos reglas
// que hacen eso: mandatory-line-break-after-statement y line-breaks-after-println.
//
// El EOF queda afuera a proposito: si el archivo termina o no con salto no lo decide
// ninguna de las dos.
internal fun startsStatementAfterSemicolon(
    prev: Token?,
    current: Token,
): Boolean = prev?.type == TokenType.SEMICOLON && current.type != TokenType.EOF
