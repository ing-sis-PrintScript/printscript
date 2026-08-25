package org.printscript.interpreter

import org.printscript.ast.AssignmentStatement
import org.printscript.ast.DeclaredType
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.Statement
import org.printscript.ast.VariableDeclaration
import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.interpreter.io.PrintScriptIO
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Statement es sealed: antes, un `when` exhaustivo en Interpreter obligaba al
 * compilador a cubrir cada subtipo. Con executors registrados en una lista esa
 * garantía se perdió — si 1.1 agrega un Statement nuevo y nadie registra su
 * executor, compila igual y explota recién en tiempo de ejecución con "No se
 * sabe cómo ejecutar este statement".
 *
 * Este test repone esa garantía como puede reponerla un test, no un compilador:
 * prueba una instancia real de cada Statement de 1.0 contra la lista completa
 * de PrintScript10.statementExecutors() y exige que algún executor la
 * reconozca. No detecta un Statement que ni siquiera está en esta lista — para
 * eso sí hace falta acordarse de sumarlo acá también — pero si alguien agrega
 * un executor a medias o rompe uno existente, este test lo nota.
 */
class PrintScript10Test {
    private val dummyRange = Range(Position(1, 1), Position(1, 10))

    private object NoOpIO : PrintScriptIO {
        override fun print(message: String) = Unit

        override fun read(prompt: String): String = ""
    }

    @Test
    fun `todo Statement de 1_0 tiene un executor que lo reconoce`() {
        val executors = PrintScript10.statementExecutors()
        val declaration =
            VariableDeclaration(
                Identifier("x", dummyRange),
                DeclaredType.NUMBER,
                NumberLiteral(1.0, dummyRange),
                dummyRange,
            )
        val statements: List<Statement> =
            listOf(
                declaration,
                AssignmentStatement(Identifier("x", dummyRange), NumberLiteral(1.0, dummyRange), dummyRange),
                ExpressionStatement(NumberLiteral(1.0, dummyRange), dummyRange),
            )

        statements.forEach { statement ->
            val handled = executors.any { it.execute(statement, Environment(), NoOpIO) != null }
            assertTrue(handled, "sin executor para ${statement::class.simpleName}")
        }
    }
}
