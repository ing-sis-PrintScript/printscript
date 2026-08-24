package org.printscript.parser.statements

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.token.TokenStream
import org.printscript.token.TokenType

interface StatementParser {
    fun canHandle(type: TokenType): Boolean

    fun parse(stream: TokenStream): Result<Statement, PrintScriptError>
}
