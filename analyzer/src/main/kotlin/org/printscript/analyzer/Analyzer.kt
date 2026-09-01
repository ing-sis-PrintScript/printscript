package org.printscript.analyzer

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result

/**
 * Recorre un programa ya parseado y reporta violaciones de reglas de estilo
 * por DiagnosticEmitter, a medida que las encuentra.
 *
 * El input es la misma forma que ya produce Parser.parse(): una secuencia
 * perezosa de Result<ASTNode, PrintScriptError>. No depende de :parser para
 * usar ese tipo — Result, PrintScriptError y ASTNode viven en :common/:ast,
 * que son las únicas dos dependencias de este módulo.
 *
 * Un Result.Failure en el medio es un error de sintaxis que el parser ya
 * reportó por su cuenta: no es un problema de estilo, así que se saltea sin
 * cortar el análisis. Un linter que se detiene en el primer error de sintaxis
 * deja de avisar sobre el resto del archivo, que es justo lo que no
 * queremos — la gramática de 1.0 recupera después de un ";" (ver
 * SkipToSemicolon en el parser), así que normalmente sigue habiendo
 * statements válidos después de uno roto.
 */
interface Analyzer {
    fun analyze(
        program: Sequence<Result<ASTNode, PrintScriptError>>,
        emit: DiagnosticEmitter,
    )
}
