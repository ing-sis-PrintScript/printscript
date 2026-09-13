package org.printscript.runner

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.lexer.versions.lexerFor
import org.printscript.parser.versions.parserFor
import org.printscript.runner.progress.Progress

fun statements(
    source: SourceFactory,
    version: Version,
    progress: Progress = Progress.NONE,
): Sequence<Result<Statement, PrintScriptError>> =
    parserFor(version).parse(lexerFor(version).tokenize(source.open())).onEach { progress.parsed() }
