package org.printscript.parser

import org.printscript.ast.ASTNode
import org.printscript.common.Position
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.token.ListTokenSource
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorMessageTest {
    private val parser = PrintScript10.parser()

    private var column = 1

    private fun token(
        type: TokenType,
        text: String,
    ): Token {
        val start = Position(1, column)
        val end = Position(1, column + text.length - 1)
        column += text.length + 1
        val value = if (type == TokenType.STRING_LITERAL) text.trim('"') else text
        return Token(type, value, Range(start, end))
    }

    private fun eof() = Token(TokenType.EOF, "", Range(Position(1, column), Position(1, column)))

    private fun messageOf(vararg tokens: Token): String {
        val results: List<Result<ASTNode, PrintScriptError>> =
            parser.parse(ListTokenSource(tokens.toList() + eof())).toList()
        val failure =
            results.filterIsInstance<Result.Failure<PrintScriptError>>().firstOrNull()
                ?: error("esperaba un error y no hubo ninguno")
        return failure.error.message
    }

    private fun let() = token(TokenType.LET, "let")

    private fun id(name: String) = token(TokenType.IDENTIFIER, name)

    private fun colon() = token(TokenType.COLON, ":")

    private fun assign() = token(TokenType.ASSIGN, "=")

    private fun semi() = token(TokenType.SEMICOLON, ";")

    private fun num(text: String) = token(TokenType.NUMBER_LITERAL, text)

    private fun typeNumber() = token(TokenType.TYPE_NUMBER, "number")

    private fun println_() = token(TokenType.PRINTLN, "println")

    private fun lparen() = token(TokenType.LPAREN, "(")

    private fun rparen() = token(TokenType.RPAREN, ")")

    private fun plus() = token(TokenType.PLUS, "+")

    @Test
    fun `falta el nombre en la declaracion`() {
        assertEquals(
            "Se esperaba un identificador como nombre de la variable",
            messageOf(let(), colon(), typeNumber(), semi()),
        )
    }

    @Test
    fun `falta los dos puntos antes del tipo`() {
        assertEquals(
            "Se esperaba ':' antes del tipo",
            messageOf(let(), id("a"), typeNumber(), semi()),
        )
    }

    @Test
    fun `el tipo declarado no es valido`() {
        assertEquals(
            "Se esperaba 'number' o 'string'",
            messageOf(let(), id("a"), colon(), id("entero"), semi()),
        )
    }

    @Test
    fun `falta el punto y coma al final de la declaracion`() {
        assertEquals(
            "Se esperaba ';' al final de la declaración",
            messageOf(let(), id("a"), colon(), typeNumber(), assign(), num("12")),
        )
    }

    @Test
    fun `falta el igual en la asignacion`() {
        assertEquals(
            "Se esperaba '=' en la asignación",
            messageOf(id("a"), num("5"), semi()),
        )
    }

    @Test
    fun `falta el punto y coma al final de la asignacion`() {
        assertEquals(
            "Se esperaba ';' al final de la asignación",
            messageOf(id("a"), assign(), num("5")),
        )
    }

    @Test
    fun `falta el parentesis despues de println`() {
        assertEquals(
            "Se esperaba '(' después de println",
            messageOf(println_(), id("a"), rparen(), semi()),
        )
    }

    @Test
    fun `falta el parentesis que cierra la llamada`() {
        assertEquals(
            "Se esperaba ')' para cerrar la llamada",
            messageOf(println_(), lparen(), id("a"), semi()),
        )
    }

    @Test
    fun `falta el punto y coma al final de la sentencia`() {
        assertEquals(
            "Se esperaba ';' al final de la sentencia",
            messageOf(println_(), lparen(), id("a"), rparen()),
        )
    }

    @Test
    fun `un token que no puede empezar un statement`() {
        assertEquals("No se esperaba '+' acá", messageOf(plus(), num("2"), semi()))
    }

    @Test
    fun `falta el operando derecho de una operacion`() {
        assertEquals(
            "Se esperaba un valor, un identificador o '('",
            messageOf(let(), id("a"), colon(), typeNumber(), assign(), num("1"), plus(), semi()),
        )
    }

    @Test
    fun `el mismo punto y coma faltante se reporta distinto segun donde falte`() {
        val declaracion = messageOf(let(), id("a"), colon(), typeNumber(), assign(), num("12"))
        column = 1
        val asignacion = messageOf(id("a"), assign(), num("5"))
        column = 1
        val sentencia = messageOf(println_(), lparen(), id("a"), rparen())

        assertEquals(
            listOf(
                "Se esperaba ';' al final de la declaración",
                "Se esperaba ';' al final de la asignación",
                "Se esperaba ';' al final de la sentencia",
            ),
            listOf(declaracion, asignacion, sentencia),
        )
    }
}
