package org.printscript.cli.runners

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.lexer.Lexer
import org.printscript.lexer.source.SourceReader
import org.printscript.parser.PrintScript10

internal fun statements(source: SourceReader): Sequence<Result<Statement, PrintScriptError>> =
    PrintScript10.parser().parse(Lexer().tokenize(source))
