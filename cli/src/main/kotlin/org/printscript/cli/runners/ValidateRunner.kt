package org.printscript.cli.runners

import org.printscript.cli.progress.Progress
import org.printscript.common.PrintScriptError
import org.printscript.common.errorOrNull
import org.printscript.lexer.source.SourceReader

// El progress va en el constructor, igual que el io de ExecuteRunner: es un
// colaborador. Lo que cambia en cada llamada es el archivo, y eso va en el metodo.
internal class ValidateRunner(private val progress: Progress = Progress.NONE) {
    // El toList() consume el archivo entero a proposito: validar es juntar TODOS los
    // errores, no cortar en el primero.
    fun validate(source: SourceReader): List<PrintScriptError> =
        statements(source, progress).mapNotNull { it.errorOrNull() }.toList()
}
