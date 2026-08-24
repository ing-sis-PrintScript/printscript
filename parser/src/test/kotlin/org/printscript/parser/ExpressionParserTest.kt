package org.printscript.parser

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.common.Position
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Se testea sin lexer: los tokens se arman a mano. El parser depende de "token",
 * no de "lexer", y estos tests lo demuestran.
 *
 * La mayoría de los casos mira la ESTRUCTURA del árbol (qué operador quedó arriba,
 * qué colgó de cada lado). Los ranges tienen sus propios tests al final, así no
 * hay que escribir posiciones exactas en cada árbol esperado.
 */
class ExpressionParserTest {
    private val parser = PrintScript10ExpressionParser()

    // ---- helpers ----

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

    private fun parse(vararg tokens: Token): Result<Parsed<Expression>, PrintScriptError> {
        val results =
            tokens.map { Result.Success(it) as Result<Token, PrintScriptError> } +
                Result.Success(Token(TokenType.EOF, "", Range(Position(1, 99), Position(1, 99))))
        return parser.parse(TokenStream(ResultTokenSource(results)))
    }

    private fun expressionOf(result: Result<Parsed<Expression>, PrintScriptError>): Expression {
        assertIs<Result.Success<Parsed<Expression>>>(result, "esperaba parsear bien y falló")
        return result.value.value
    }

    private fun errorOf(result: Result<Parsed<Expression>, PrintScriptError>): PrintScriptError {
        assertIs<Result.Failure<PrintScriptError>>(result, "esperaba un error y parseó bien")
        return result.error
    }

    private fun num(text: String) = token(TokenType.NUMBER_LITERAL, text)

    private fun id(name: String) = token(TokenType.IDENTIFIER, name)

    private fun str(text: String) = token(TokenType.STRING_LITERAL, "\"$text\"")

    private fun op(
        type: TokenType,
        text: String,
    ) = token(type, text)

    // ---- hojas ----

    @Test
    fun `numero suelto`() {
        val expression = expressionOf(parse(num("12")))

        assertEquals(NumberLiteral::class, expression::class)
        assertEquals(12.0, (expression as NumberLiteral).value)
    }

    @Test
    fun `numero decimal`() {
        val expression = expressionOf(parse(num("4.5")))

        assertEquals(4.5, (expression as NumberLiteral).value)
    }

    @Test
    fun `string sin las comillas`() {
        val expression = expressionOf(parse(str("Joe")))

        assertEquals("Joe", (expression as StringLiteral).value)
    }

    @Test
    fun `identificador`() {
        val expression = expressionOf(parse(id("nombre")))

        assertEquals("nombre", (expression as Identifier).name)
    }

    // ---- operaciones ----

    @Test
    fun `suma simple`() {
        val expression = expressionOf(parse(num("2"), op(TokenType.PLUS, "+"), num("3")))

        val binary = assertIs<BinaryExpression>(expression)
        assertEquals(BinaryOperator.PLUS, binary.operator)
        assertEquals(2.0, (binary.left as NumberLiteral).value)
        assertEquals(3.0, (binary.right as NumberLiteral).value)
    }

    @Test
    fun `los cuatro operadores se traducen al enum del AST`() {
        val casos =
            listOf(
                TokenType.PLUS to BinaryOperator.PLUS,
                TokenType.MINUS to BinaryOperator.MINUS,
                TokenType.STAR to BinaryOperator.TIMES,
                TokenType.SLASH to BinaryOperator.DIVIDE,
            )

        for ((tokenType, expected) in casos) {
            column = 1
            val expression = expressionOf(parse(num("6"), op(tokenType, "?"), num("2")))
            assertEquals(expected, assertIs<BinaryExpression>(expression).operator)
        }
    }

    // ---- PRECEDENCIA: el corazón del parser ----

    /**
     * 2 + 3 * 4 tiene que dar 14, no 20. En la lista de tokens no hay nada que lo
     * diga; lo dice la FORMA del árbol. Si este test se rompe, el interpreter
     * empieza a calcular mal sin que nadie toque el interpreter.
     */
    @Test
    fun `la multiplicacion agrupa antes que la suma`() {
        val expression =
            expressionOf(
                parse(num("2"), op(TokenType.PLUS, "+"), num("3"), op(TokenType.STAR, "*"), num("4")),
            )

        val root = assertIs<BinaryExpression>(expression)
        assertEquals(BinaryOperator.PLUS, root.operator, "arriba tiene que quedar el +")
        assertEquals(2.0, (root.left as NumberLiteral).value)

        val right = assertIs<BinaryExpression>(root.right)
        assertEquals(BinaryOperator.TIMES, right.operator, "el * tiene que quedar más abajo")
    }

    @Test
    fun `la multiplicacion agrupa antes aunque venga primero`() {
        // 2 * 3 + 4  →  (2 * 3) + 4
        val expression =
            expressionOf(
                parse(num("2"), op(TokenType.STAR, "*"), num("3"), op(TokenType.PLUS, "+"), num("4")),
            )

        val root = assertIs<BinaryExpression>(expression)
        assertEquals(BinaryOperator.PLUS, root.operator)

        val left = assertIs<BinaryExpression>(root.left)
        assertEquals(BinaryOperator.TIMES, left.operator)
        assertEquals(4.0, (root.right as NumberLiteral).value)
    }

    @Test
    fun `division y multiplicacion estan en el mismo nivel`() {
        // 12 / 4 * 2  →  (12 / 4) * 2, izquierda a derecha
        val expression =
            expressionOf(
                parse(num("12"), op(TokenType.SLASH, "/"), num("4"), op(TokenType.STAR, "*"), num("2")),
            )

        val root = assertIs<BinaryExpression>(expression)
        assertEquals(BinaryOperator.TIMES, root.operator)
        assertEquals(BinaryOperator.DIVIDE, assertIs<BinaryExpression>(root.left).operator)
    }

    // ---- ASOCIATIVIDAD ----

    /**
     * a - b - c es (a - b) - c, no a - (b - c). Con 10 - 3 - 2 la diferencia es
     * 5 contra 9. Lo garantiza el while del parser; con recursión a la derecha
     * daría mal.
     */
    @Test
    fun `la resta asocia a la izquierda`() {
        val expression =
            expressionOf(
                parse(num("10"), op(TokenType.MINUS, "-"), num("3"), op(TokenType.MINUS, "-"), num("2")),
            )

        val root = assertIs<BinaryExpression>(expression)
        assertEquals(2.0, (root.right as NumberLiteral).value, "el último operando va a la derecha de la raíz")

        val left = assertIs<BinaryExpression>(root.left)
        assertEquals(BinaryOperator.MINUS, left.operator)
        assertEquals(10.0, (left.left as NumberLiteral).value)
        assertEquals(3.0, (left.right as NumberLiteral).value)
    }

    @Test
    fun `concatenacion encadenada asocia a la izquierda`() {
        // name + " " + lastName  →  (name + " ") + lastName, el ejemplo 1 de la consigna
        val expression =
            expressionOf(
                parse(
                    id("name"),
                    op(TokenType.PLUS, "+"),
                    str(" "),
                    op(TokenType.PLUS, "+"),
                    id("lastName"),
                ),
            )

        val root = assertIs<BinaryExpression>(expression)
        assertEquals("lastName", (root.right as Identifier).name)

        val left = assertIs<BinaryExpression>(root.left)
        assertEquals("name", (left.left as Identifier).name)
        assertEquals(" ", (left.right as StringLiteral).value)
    }

    // ---- PARÉNTESIS ----

    /**
     * Los paréntesis no dejan ningún nodo en el AST: su efecto es que la
     * expresión de adentro quede más abajo, y esa posición ya significa
     * "se evalúa primero". Es la parte "abstract" del árbol.
     */
    @Test
    fun `los parentesis cambian el agrupamiento sin dejar nodo`() {
        // (2 + 3) * 4  →  el * arriba, el + abajo (al revés que sin paréntesis)
        val expression =
            expressionOf(
                parse(
                    op(TokenType.LPAREN, "("),
                    num("2"),
                    op(TokenType.PLUS, "+"),
                    num("3"),
                    op(TokenType.RPAREN, ")"),
                    op(TokenType.STAR, "*"),
                    num("4"),
                ),
            )

        val root = assertIs<BinaryExpression>(expression)
        assertEquals(BinaryOperator.TIMES, root.operator)
        assertEquals(BinaryOperator.PLUS, assertIs<BinaryExpression>(root.left).operator)
        assertEquals(4.0, (root.right as NumberLiteral).value)
    }

    @Test
    fun `parentesis redundantes no cambian el arbol`() {
        val conParentesis =
            expressionOf(
                parse(op(TokenType.LPAREN, "("), num("5"), op(TokenType.RPAREN, ")")),
            )

        assertEquals(5.0, assertIs<NumberLiteral>(conParentesis).value)
    }

    @Test
    fun `parentesis anidados`() {
        // ((2))
        val expression =
            expressionOf(
                parse(
                    op(TokenType.LPAREN, "("),
                    op(TokenType.LPAREN, "("),
                    num("2"),
                    op(TokenType.RPAREN, ")"),
                    op(TokenType.RPAREN, ")"),
                ),
            )

        assertEquals(2.0, assertIs<NumberLiteral>(expression).value)
    }

    // ---- ERRORES ----

    @Test
    fun `parentesis sin cerrar`() {
        val error = errorOf(parse(op(TokenType.LPAREN, "("), num("2")))

        assertIs<SyntaxError>(error)
        assertTrue(error.message.contains("')'"), "el mensaje debería mencionar el paréntesis: ${error.message}")
    }

    @Test
    fun `operador sin operando derecho`() {
        val error = errorOf(parse(num("2"), op(TokenType.PLUS, "+")))

        assertIs<SyntaxError>(error)
    }

    @Test
    fun `token que no puede empezar una expresion`() {
        val error = errorOf(parse(op(TokenType.SEMICOLON, ";")))

        assertIs<SyntaxError>(error)
    }

    /**
     * El texto del token es un String, así que convertirlo a número puede
     * fallar. Antes esto era un toDouble() pelado: reventaba con
     * NumberFormatException y era el único punto del módulo donde una falla
     * NO volvía como Result. Que este test pase significa que el parser no
     * tira excepciones, ni siquiera con un lexer que emita basura.
     */
    @Test
    fun `un numero mal formado es un error, no una excepcion`() {
        val error = errorOf(parse(num("12.3.4")))

        assertIs<SyntaxError>(error)
        assertTrue(
            error.message.contains("12.3.4"),
            "el mensaje debería mostrar el texto que no pudo convertir: ${error.message}",
        )
    }

    @Test
    fun `un error lexico se propaga tal cual`() {
        val lexico =
            object : PrintScriptError {
                override val message = "Caracter inesperado '@'"
                override val range = Range(Position(1, 1), Position(1, 1))
            }
        val stream = TokenStream(ResultTokenSource(listOf(Result.Failure(lexico))))

        val error = errorOf(parser.parse(stream))

        assertEquals(lexico, error, "el error del lexer no se envuelve en un SyntaxError")
    }

    // ---- RANGES ----

    @Test
    fun `el range de una hoja es el del token`() {
        val expression = expressionOf(parse(num("12")))

        assertEquals(Range(Position(1, 1), Position(1, 2)), expression.range)
    }

    /**
     * El range de un nodo binario NO sale de un token: se compone del inicio
     * del hijo izquierdo y el fin del derecho. Sin esto, el linter y los
     * mensajes de error señalan mal.
     */
    @Test
    fun `el range de un binario abarca de punta a punta`() {
        // "2 + 3": el 2 en la columna 1, el 3 en la 5
        val expression = expressionOf(parse(num("2"), op(TokenType.PLUS, "+"), num("3")))

        assertEquals(Position(1, 1), expression.range.start)
        assertEquals(Position(1, 5), expression.range.end)
    }

    @Test
    fun `el range de un binario anidado abarca toda la expresion`() {
        // "2 + 3 * 4": del 2 (col 1) al 4 (col 9)
        val expression =
            expressionOf(
                parse(num("2"), op(TokenType.PLUS, "+"), num("3"), op(TokenType.STAR, "*"), num("4")),
            )

        assertEquals(Position(1, 1), expression.range.start)
        assertEquals(Position(1, 9), expression.range.end)
    }
}
