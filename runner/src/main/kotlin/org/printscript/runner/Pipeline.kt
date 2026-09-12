package org.printscript.runner

import org.printscript.ast.Statement
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.common.Version
import org.printscript.lexer.lexerFor
import org.printscript.parser.parserFor
import org.printscript.runner.progress.Progress

// Lexer y despues parser: la parte que comparten los cuatro comandos.
//
// La version elige con que reglas se lexea y se parsea. Un programa de 1.1 corrido como
// 1.0 falla solo: sus palabras no estan en el mapa de keywords de 1.0, asi que salen
// IDENTIFIER y el parser reporta un error de sintaxis. No hay un chequeo de version
// aparte, y no hace falta.
//
// El aviso de progreso sale de aca porque este es el momento en que se parseo una
// sentencia, y es el mismo para los cuatro. Quien cuenta y quien muestra es el CLI.
fun statements(
    source: SourceFactory,
    version: Version,
    progress: Progress = Progress.NONE,
): Sequence<Result<Statement, PrintScriptError>> =
    parserFor(version).parse(lexerFor(version).tokenize(source.open())).onEach { progress.parsed() }
