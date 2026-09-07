package org.printscript.cli.runners

import org.printscript.ast.Statement
import org.printscript.cli.progress.Progress
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.lexer.Lexer
import org.printscript.lexer.source.SourceReader
import org.printscript.parser.PrintScript10

// Lexer y despues parser: la parte que comparten los cuatro comandos.
// El aviso de progreso sale de aca porque este es el momento en que se parseo una
// sentencia, y es el mismo para los cuatro. Quien cuenta y quien muestra es el CLI.
internal fun statements(
    source: SourceReader,
    progress: Progress = Progress.NONE,
): Sequence<Result<Statement, PrintScriptError>> =
    PrintScript10.parser().parse(Lexer().tokenize(source)).onEach { progress.parsed() }
