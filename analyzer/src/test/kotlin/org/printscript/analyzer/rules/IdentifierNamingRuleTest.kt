package org.printscript.analyzer.rules

import org.printscript.analyzer.Diagnostic
import org.printscript.analyzer.DiagnosticEmitter
import org.printscript.analyzer.assignment
import org.printscript.analyzer.config.CamelCase
import org.printscript.analyzer.config.SnakeCase
import org.printscript.analyzer.declaration
import org.printscript.analyzer.number
import org.printscript.analyzer.rangeAt
import org.printscript.ast.ASTNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IdentifierNamingRuleTest {
    private fun checkedBy(rule: IdentifierNamingRule): (ASTNode) -> Diagnostic? {
        var found: Diagnostic? = null
        return { node ->
            found = null
            rule.check(node, DiagnosticEmitter { found = it })
            found
        }
    }

    @Test
    fun `con CamelCase un identificador camelCase no reporta nada`() {
        val rule = IdentifierNamingRule(CamelCase)
        val check = checkedBy(rule)

        assertNull(check(declaration("miVariable")))
    }

    @Test
    fun `con CamelCase un identificador snake_case reporta un problema`() {
        val rule = IdentifierNamingRule(CamelCase)
        val check = checkedBy(rule)

        val diagnostic = check(declaration("mi_variable"))

        assertEquals("identifier-naming", diagnostic?.rule)
        assertEquals(
            "El identificador 'mi_variable' no sigue la convención de nombres configurada.",
            diagnostic?.message,
        )
    }

    @Test
    fun `con SnakeCase un identificador snake_case no reporta nada`() {
        val rule = IdentifierNamingRule(SnakeCase)
        val check = checkedBy(rule)

        assertNull(check(declaration("mi_variable")))
    }

    @Test
    fun `con SnakeCase un identificador camelCase reporta un problema`() {
        val rule = IdentifierNamingRule(SnakeCase)
        val check = checkedBy(rule)

        assertEquals("identifier-naming", check(declaration("miVariable"))?.rule)
    }

    @Test
    fun `no revisa un AssignmentStatement, solo declaraciones`() {
        val rule = IdentifierNamingRule(CamelCase)
        val check = checkedBy(rule)

        // "mal_nombrado" ya se hubiera reportado en su declaracion; asignarle
        // un valor de nuevo no es una segunda oportunidad de nombrarlo.
        assertNull(check(assignment("mal_nombrado", number(1.0))))
    }

    @Test
    fun `reporta el range exacto del identificador, no el de la declaracion entera`() {
        val rule = IdentifierNamingRule(CamelCase)
        val check = checkedBy(rule)

        val identifierRange = rangeAt(line = 3, column = 5, length = 11)
        val diagnostic = check(declaration("mi_variable", identifierRange = identifierRange))

        assertEquals(identifierRange, diagnostic?.range)
    }
}
