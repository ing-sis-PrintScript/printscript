package org.printscript.formatter.statements

import org.printscript.ast.DeclaredType

internal fun DeclaredType.symbol(): String =
    when (this) {
        DeclaredType.NUMBER -> "number"
        DeclaredType.STRING -> "string"
    }
