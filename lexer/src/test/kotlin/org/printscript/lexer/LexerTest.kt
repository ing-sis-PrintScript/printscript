package org.printscript.lexer

import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.common.collectResults
import org.printscript.common.errorOrNull
import org.printscript.common.getOrNull
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LexerTest {
    private val lexer = Lexer()

    /** Atajo: junta la secuencia y devuelve solo los tipos, para comparar más cómodo. */
    private fun typesOf(source: String): List<TokenType> {
        val tokens = lexer.tokenize(source).collectResults().getOrNull()
        assertNotNull(tokens, "esperaba que funcione y falló")
        return tokens.map { it.type }
    }

    private fun errorOf(source: String): LexicalError {
        val error = lexer.tokenize(source).collectResults().errorOrNull()
        return assertNotNull(error, "esperaba un error léxico")
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
        val tokens = lexer.tokenize("let x = 5;").collectResults().getOrNull()!!
        assertEquals("let", tokens[0].value)
        assertEquals(Range(Position(1, 1), Position(1, 3)), tokens[0].range)
        assertEquals("x", tokens[1].value)
        assertEquals(Range(Position(1, 5), Position(1, 5)), tokens[1].range)
    }

    @Test
    fun `cuenta bien el numero de linea`() {
        val tokens =
            lexer.tokenize("let a: number = 1;\nlet b: number = 2;")
                .collectResults().getOrNull()!!
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
        val resultados = lexer.tokenize("let a = 1;\n@\nlet b = 2;").toList()
        assertTrue(resultados.last() is Result.Failure)
        assertTrue(resultados.none { it.getOrNull()?.type == TokenType.EOF })
    }

    /**
     * La prueba de que es realmente perezoso: una secuencia infinita de líneas.
     * Si el lexer leyera todo antes de devolver, este test colgaría para siempre.
     */
    @Test
    fun `es perezoso y no lee de mas`() {
        var lineasLeidas = 0
        val infinitas =
            sequence {
                while (true) {
                    lineasLeidas++
                    yield("let a: number = 1;")
                }
            }

        val primeros = lexer.tokenize(infinitas).take(3).toList()

        assertEquals(3, primeros.size)
        assertTrue(primeros.all { it is Result.Success })
        assertEquals(1, lineasLeidas) // solo hizo falta la primera línea
    }
}
