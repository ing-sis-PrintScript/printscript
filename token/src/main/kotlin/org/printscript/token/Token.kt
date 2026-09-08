package org.printscript.token

import org.printscript.common.Range

data class Token(
    val type: TokenType,
    val value: String,
    val range: Range,
    // Va ultimo y con default para que los Token(type, value, range) que ya
    // existen sigan compilando: el campo es aditivo.
    val leadingTrivia: Trivia = Trivia.EMPTY,
)
