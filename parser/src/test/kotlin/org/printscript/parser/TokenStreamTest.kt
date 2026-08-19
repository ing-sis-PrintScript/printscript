package org.printscript.parser

import org.printscript.common.Position
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.parser.token.TokenStream
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue


class TokenStreamTest {


    private var column = 1

    /** Arma un token con un range plausible y consecutivo, para no escribirlos a mano. */
    private fun token(type: TokenType, text: String = ""): Token {
        val start = Position(1, column)
        val end = Position(1, column + text.length - 1)
        column += text.length + 1
        return Token(type, text, text, Range(start, end))
    }

    private fun streamOf(vararg tokens: Token): TokenStream {
        val results = tokens.map { Result.Success(it) as Result<Token, PrintScriptError> }
        return TokenStream(results.asSequence())
    }

    /** Desempaqueta un Success o falla el test. */
    private fun valueOf(result: Result<Token, PrintScriptError>): Token {
        assertIs<Result.Success<Token>>(result, "esperaba Success y vino Failure")
        return result.value
    }

    private fun errorOf(result: Result<*, PrintScriptError>): PrintScriptError {
        assertIs<Result.Failure<PrintScriptError>>(result, "esperaba Failure y vino Success")
        return result.error
    }


    @Test
    fun `peek no avanza el stream`() {
        val stream = streamOf(token(TokenType.LET, "let"), token(TokenType.IDENTIFIER, "a"))

        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
    }

    @Test
    fun `next despues de peek devuelve el mismo token`() {
        val stream = streamOf(token(TokenType.LET, "let"), token(TokenType.IDENTIFIER, "a"))

        val visto = valueOf(stream.peek())
        val consumido = valueOf(stream.next())

        assertEquals(visto, consumido)
    }

    @Test
    fun `next sin peek previo avanza`() {
        val stream = streamOf(
            token(TokenType.LET, "let"),
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.COLON, ":"),
        )

        assertEquals(TokenType.LET, valueOf(stream.next()).type)
        assertEquals(TokenType.IDENTIFIER, valueOf(stream.next()).type)
        assertEquals(TokenType.COLON, valueOf(stream.next()).type)
    }

    @Test
    fun `peek y next alternados recorren la secuencia completa`() {
        val stream = streamOf(
            token(TokenType.LET, "let"),
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.SEMICOLON, ";"),
        )

        val recorrido = mutableListOf<TokenType>()
        repeat(3) {
            stream.peek()               // mirar no debería alterar nada
            recorrido.add(valueOf(stream.next()).type)
        }

        assertEquals(
            listOf(TokenType.LET, TokenType.IDENTIFIER, TokenType.SEMICOLON),
            recorrido,
        )
    }


    @Test
    fun `expect con el tipo correcto devuelve el token`() {
        val esperado = token(TokenType.LET, "let")
        val stream = streamOf(esperado)

        val token = valueOf(stream.expect(TokenType.LET, "'let'"))

        // Se compara el token entero, no un campo suelto: lo que promete expect
        // es devolver ESE token, y así el test no depende de qué campos tenga.
        assertEquals(esperado, token)
    }

    @Test
    fun `expect con el tipo equivocado devuelve SyntaxError en la posicion del token`() {
        val inesperado = token(TokenType.PRINTLN, "println")
        val stream = streamOf(inesperado)

        val error = errorOf(stream.expect(TokenType.SEMICOLON, "';' al final de la declaracion"))

        assertIs<SyntaxError>(error)
        assertTrue(error.message.contains("';'"), "el mensaje deberia decir que esperaba: ${error.message}")
        assertEquals(inesperado.range, error.range)
    }

    @Test
    fun `expect propaga un error lexico sin transformarlo`() {
        val lexico = object : PrintScriptError {
            override val message = "Caracter inesperado '@'"
            override val range = Range(Position(1, 5), Position(1, 5))
        }
        val stream = TokenStream(sequenceOf(Result.Failure(lexico)))

        val error = errorOf(stream.expect(TokenType.LET, "'let'"))

        assertEquals(lexico, error, "el error del lexer tiene que salir tal cual, no disfrazado de SyntaxError")
    }


    @Test
    fun `skip consume el token esperado`() {
        val stream = streamOf(token(TokenType.COLON, ":"), token(TokenType.TYPE_NUMBER, "number"))

        assertIs<Result.Success<Unit>>(stream.skip(TokenType.COLON, "':'"))
        assertEquals(TokenType.TYPE_NUMBER, valueOf(stream.next()).type)
    }

    @Test
    fun `skip falla si el token no es el esperado`() {
        val stream = streamOf(token(TokenType.ASSIGN, "="))

        errorOf(stream.skip(TokenType.COLON, "':'"))
    }


    @Test
    fun `peekIs no consume`() {
        val stream = streamOf(token(TokenType.LET, "let"))

        assertTrue(stream.peekIs(TokenType.LET))
        assertFalse(stream.peekIs(TokenType.PRINTLN))
        assertEquals(TokenType.LET, valueOf(stream.next()).type)
    }

    @Test
    fun `atEnd detecta el EOF`() {
        val stream = streamOf(token(TokenType.SEMICOLON, ";"), token(TokenType.EOF))

        assertFalse(stream.atEnd())
        stream.next()
        assertTrue(stream.atEnd())
    }


    @Test
    fun `synchronize deja el stream despues del punto y coma`() {
        // simula el descarte de: println ( "hola" ) ;  → retomar en el let siguiente
        val stream = streamOf(
            token(TokenType.PRINTLN, "println"),
            token(TokenType.LPAREN, "("),
            token(TokenType.STRING_LITERAL, "\"hola\""),
            token(TokenType.RPAREN, ")"),
            token(TokenType.SEMICOLON, ";"),
            token(TokenType.LET, "let"),
            token(TokenType.EOF),
        )

        stream.synchronize()

        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
    }

    @Test
    fun `synchronize corta en el primer punto y coma, no en el ultimo`() {
        val stream = streamOf(
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.SEMICOLON, ";"),
            token(TokenType.LET, "let"),
            token(TokenType.SEMICOLON, ";"),
            token(TokenType.EOF),
        )

        stream.synchronize()

        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
    }

    /** El que protege del loop infinito: sin ";" tiene que frenar en el EOF. */
    @Test
    fun `synchronize sin punto y coma llega al EOF y termina`() {
        val stream = streamOf(
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.PLUS, "+"),
            token(TokenType.NUMBER_LITERAL, "5"),
            token(TokenType.EOF),
        )

        stream.synchronize()

        assertTrue(stream.atEnd())
    }

    @Test
    fun `synchronize sobre un stream ya en EOF no hace nada`() {
        val stream = streamOf(token(TokenType.EOF))

        stream.synchronize()

        assertTrue(stream.atEnd())
    }



    @Test
    fun `el stream no consume mas tokens de los pedidos`() {
        var producidos = 0
        val perezosa = sequence {
            producidos++; yield(Result.Success(token(TokenType.LET, "let")) as Result<Token, PrintScriptError>)
            producidos++; yield(Result.Success(token(TokenType.IDENTIFIER, "a")))
            producidos++; yield(Result.Success(token(TokenType.EOF)))
        }
        val stream = TokenStream(perezosa)

        stream.peek()
        stream.peek()
        stream.next()

        assertEquals(1, producidos, "peek dos veces y next una deberia producir un solo token")
    }
}