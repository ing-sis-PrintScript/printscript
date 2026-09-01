package org.printscript.analyzer.engine

import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.ast.ASTNode

/**
 * Revisa UN nodo del AST y, si encuentra un problema, lo reporta por emit.
 *
 * Mismo espíritu que TokenRule en el lexer: cada regla mira el nodo que
 * recibe y decide por su cuenta, con `is`, si le interesa — no hay un
 * canHandle aparte. La diferencia con TokenRule es que ahí gana la primera
 * regla que contesta; acá pueden matchear varias reglas sobre el mismo nodo
 * a la vez, porque eso es justamente lo que hace un linter: reglas
 * independientes que conviven sobre el mismo código sin conocerse entre sí.
 *
 * Agregar una regla nueva es una clase que implementa esto y una línea en
 * PrintScript10.rules() — ni el motor de recorrido ni las reglas existentes
 * se tocan.
 */
internal interface Rule {
    fun check(
        node: ASTNode,
        emitter: DiagnosticEmitter,
    )
}
