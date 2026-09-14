package org.printscript.lexer.versions

import org.printscript.common.Version
import org.printscript.lexer.Lexer
import org.printscript.lexer.TokenMatcher

fun lexerFor(version: Version): Lexer =
    when (version) {
        Version.V10 -> Lexer(TokenMatcher(PrintScript10.RULES))
        Version.V11 -> Lexer(TokenMatcher(PrintScript11.RULES))
    }
