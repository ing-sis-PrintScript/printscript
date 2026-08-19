package org.printscript.parser

import org.printscript.ast.Expression
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.token.TokenStream


interface ExpressionParser {

    fun parse(stream: TokenStream): Result<Expression, PrintScriptError>
}
