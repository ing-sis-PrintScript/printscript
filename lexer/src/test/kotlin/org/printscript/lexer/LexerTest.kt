package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.lexer.source.InfiniteSourceReader
import org.printscript.lexer.source.LineReadCounter
import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LexerTest {
    private val lexer = Lexer()

    private fun drain(source: TokenSource): List<TokenReadResult> =
        when (val result = source.nextToken()) {
            is TokenReadResult.Success -> listOf(result) + drain(result.remaining)
            is TokenReadResult.Failure -> listOf(result) + drain(result.remaining)
            TokenReadResult.EndOfInput -> emptyList()
        }

    private fun take(
        source: TokenSource,
        count: Int,
    ): List<TokenReadResult> =
        if (count == 0) {
            emptyList()
        } else {
            when (val result = source.nextToken()) {
                is TokenReadResult.Success -> listOf(result) + take(result.remaining, count - 1)
                is TokenReadResult.Failure -> listOf(result)
                TokenReadResult.EndOfInput -> emptyList()
            }
        }

    private fun tokensOf(source: String): List<Token> {
        val resultados = drain(lexer.tokenize(source))
        assertTrue(resultados.all { it is TokenReadResult.Success }, "esperaba que funcione y falló")
        return resultados.filterIsInstance<TokenReadResult.Success>().map { it.token }
    }

    private fun typesOf(source: String): List<TokenType> = tokensOf(source).map { it.type }

    private fun errorOf(source: String): LexicalError {
        val failure = drain(lexer.tokenize(source)).filterIsInstance<TokenReadResult.Failure>().firstOrNull()
        assertNotNull(failure, "esperaba un error léxico")
        return assertIs<LexicalError>(failure.error)
    }

    @Test
    fun `declaracion con asignacion`() {
        assertEquals(
            listOf(
                TokenType.LET,
                TokenType.IDENTIFIER,
                TokenType.COLON,
                TokenType.TYPE_STRING,
                TokenType.ASSIGN,
                TokenType.STRING_LITERAL,
                TokenType.SEMICOLON,
                TokenType.EOF,
            ),
            typesOf("""let name: string = "Joe";"""),
        )
    }

    @Test
    fun `println con concatenacion`() {
        assertEquals(
            listOf(
                TokenType.PRINTLN,
                TokenType.LPAREN,
                TokenType.IDENTIFIER,
                TokenType.PLUS,
                TokenType.STRING_LITERAL,
                TokenType.RPAREN,
                TokenType.SEMICOLON,
                TokenType.EOF,
            ),
            typesOf("""println(name + " ");"""),
        )
    }

    @Test
    fun `operaciones y decimales`() {
        assertEquals(
            listOf(
                TokenType.NUMBER_LITERAL,
                TokenType.SLASH,
                TokenType.NUMBER_LITERAL,
                TokenType.MINUS,
                TokenType.NUMBER_LITERAL,
                TokenType.EOF,
            ),
            typesOf("12.5 / 4 - 1"),
        )
    }

    @Test
    fun `strings con comillas simples`() {
        assertEquals(listOf(TokenType.STRING_LITERAL, TokenType.EOF), typesOf("'hola mundo'"))
    }

    @Test
    fun `letter no es la keyword let`() {
        assertEquals(listOf(TokenType.IDENTIFIER, TokenType.EOF), typesOf("letter"))
    }

    @Test
    fun `el valor y la posicion son correctos`() {
        val tokens = tokensOf("let x = 5;")
        assertEquals("let", tokens[0].value)
        assertEquals(Range(Position(1, 1), Position(1, 3)), tokens[0].range)
        assertEquals("x", tokens[1].value)
        assertEquals(Range(Position(1, 5), Position(1, 5)), tokens[1].range)
    }

    @Test
    fun `cuenta bien el numero de linea`() {
        val tokens = tokensOf("let a: number = 1;\nlet b: number = 2;")
        val segundoLet = tokens.first { it.range.start.line == 2 }
        assertEquals(TokenType.LET, segundoLet.type)
        assertEquals(Position(2, 1), segundoLet.range.start)
    }

    @Test
    fun `caracter invalido devuelve Failure con la posicion`() {
        val error = errorOf("let a: number = 5 @ 3;")
        assertEquals("Caracter inesperado '@'", error.message)
        assertEquals(Position(1, 19), error.range.start)
    }

    @Test
    fun `string sin cerrar devuelve Failure`() {
        val error = errorOf("""let a: string = "hola;""")
        assertEquals("String sin cerrar", error.message)
        assertEquals(Position(1, 17), error.range.start)
    }

    @Test
    fun `fuente vacio devuelve solo EOF`() {
        assertEquals(listOf(TokenType.EOF), typesOf(""))
    }

    @Test
    fun `corta en el primer error y no sigue tokenizando`() {
        val resultados = drain(lexer.tokenize("let a = 1;\n@\nlet b = 2;"))
        assertTrue(resultados.last() is TokenReadResult.Failure)
        assertTrue(resultados.none { it is TokenReadResult.Success && it.token.type == TokenType.EOF })
    }

    @Test
    fun `es perezoso y no lee de mas`() {
        val leidas = LineReadCounter()

        val primeros = take(lexer.tokenize(InfiniteSourceReader("let a: number = 1;", leidas)), 3)

        assertEquals(3, primeros.size)
        assertTrue(primeros.all { it is TokenReadResult.Success })
        assertEquals(1, leidas.total)
    }

    @Test
    fun `recorrer dos veces la misma fuente da la misma secuencia`() {
        val source = lexer.tokenize("let x = 5;")

        assertEquals(drain(source), drain(source))
    }

    @Test
    fun `un remaining guardado a mitad de camino da el mismo resto`() {
        val source = lexer.tokenize("let x = 5;")
        val guardado = assertIs<TokenReadResult.Success>(source.nextToken()).remaining

        val primerRecorrido = drain(guardado)
        drain(source)

        assertEquals(primerRecorrido, drain(guardado))
    }

    @Test
    fun `despues del EOF la lectura siguiente devuelve EndOfInput`() {
        val ultimo = drain(lexer.tokenize("let x = 5;")).last()

        val eof = assertIs<TokenReadResult.Success>(ultimo)
        assertEquals(TokenType.EOF, eof.token.type)
        assertEquals(TokenReadResult.EndOfInput, eof.remaining.nextToken())
    }

    @Test
    fun `despues de un Failure la lectura siguiente devuelve EndOfInput`() {
        val ultimo = drain(lexer.tokenize("let a = 1;\n@\nlet b = 2;")).last()

        val failure = assertIs<TokenReadResult.Failure>(ultimo)
        assertEquals(TokenReadResult.EndOfInput, failure.remaining.nextToken())
    }

    @Test
    fun `dos lexers distintos sobre el mismo fuente producen fuentes iguales`() {
        val fuente = "let x: number = 5;\nprintln(x);"

        assertEquals(Lexer().tokenize(fuente), Lexer().tokenize(fuente))
    }

    @Test
    fun `dos lexers distintos sobre fuentes distintas producen fuentes distintas`() {
        assertNotEquals(Lexer().tokenize("let x = 1;"), Lexer().tokenize("let y = 2;"))
    }
}
