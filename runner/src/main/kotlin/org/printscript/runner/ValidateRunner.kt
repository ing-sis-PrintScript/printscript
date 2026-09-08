package org.printscript.runner

import org.printscript.common.PrintScriptError
import org.printscript.common.errorOrNull
import org.printscript.runner.progress.Progress

// El progress va en el constructor, igual que el io de ExecuteRunner: es un
// colaborador. Lo que cambia en cada llamada es el archivo, y eso va en el metodo.
class ValidateRunner(private val progress: Progress = Progress.NONE) {
    // El toList() consume el archivo entero a proposito: validar es juntar TODOS los
    // errores, no cortar en el primero.
    fun validate(source: SourceFactory): List<PrintScriptError> =
        statements(source, progress).mapNotNull { it.errorOrNull() }.toList()
}
