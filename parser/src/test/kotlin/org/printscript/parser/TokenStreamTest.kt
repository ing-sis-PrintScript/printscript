package org.printscript.parser

import org.printscript.common.Position
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.parser.token.Parsed
import org.printscript.parser.token.TokenStream
import org.printscript.parser.token.UnexpectedEndOfInput
import org.printscript.parser.token.expect
import org.printscript.parser.token.next
import org.printscript.parser.token.peekIs
import org.printscript.parser.token.skip
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * El TokenStream es un VALOR, no un cursor: avanzar devuelve otro stream y el
 * original queda intacto. La mayoría de estos tests existen para fijar esa
 * propiedad, porque es de la que dependen la pureza de los parsers y el poder
 * descartar un parseo fallido sin dejar el stream corrupto.
 */
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
        return TokenStream.of(results.asSequence())
    }

    /** Desempaqueta un Success o falla el test. */
    private fun valueOf(result: Result<Token, PrintScriptError>): Token {
        assertIs<Result.Success<Token>>(result, "esperaba Success y vino Failure")
        return result.value
    }

    private fun parsedOf(result: Result<Parsed<Token>, PrintScriptError>): Parsed<Token> {
        assertIs<Result.Success<Parsed<Token>>>(result, "esperaba Success y vino Failure")
        return result.value
    }

    private fun errorOf(result: Result<*, PrintScriptError>): PrintScriptError {
        assertIs<Result.Failure<PrintScriptError>>(result, "esperaba Failure y vino Success")
        return result.error
    }

    /** Recorre un stream de punta a punta sin mutarlo. */
    private fun walk(stream: TokenStream): List<TokenType> {
        val visitados = mutableListOf<TokenType>()
        var current = stream
        while (!current.atEnd()) {
            visitados.add(valueOf(current.peek()).type)
            current = current.advance()
        }
        return visitados
    }

    // ---- INMUTABILIDAD: la propiedad central ----

    /**
     * El test que define el diseño: avanzar produce un stream nuevo y deja el
     * de origen exactamente donde estaba. Con el TokenStream mutable de antes
     * esto era imposible de pedir.
     */
    @Test
    fun `avanzar no toca el stream original`() {
        val stream = streamOf(token(TokenType.LET, "let"), token(TokenType.IDENTIFIER, "a"))

        val avanzado = stream.advance()

        assertEquals(TokenType.LET, valueOf(stream.peek()).type, "el original sigue en el primer token")
        assertEquals(TokenType.IDENTIFIER, valueOf(avanzado.peek()).type, "el derivado arrancó en el segundo")
        assertNotEquals(stream, avanzado, "avanzar tiene que dar OTRO stream, no el mismo mutado")
    }

    /**
     * Consecuencia directa de lo anterior, y la que permite descartar un parseo
     * fallido: recorrer el stream dos veces da lo mismo las dos veces.
     */
    @Test
    fun `el mismo stream se puede recorrer dos veces con igual resultado`() {
        val stream = streamOf(
            token(TokenType.LET, "let"),
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.SEMICOLON, ";"),
            token(TokenType.EOF),
        )

        assertEquals(walk(stream), walk(stream))
    }

    /**
     * Dos ramas derivadas del mismo punto no se pisan. Es lo que habilitaría un
     * parseo especulativo, y lo que garantiza que un expect fallido no le mueva
     * el piso a quien todavía tiene el stream anterior.
     */
    @Test
    fun `dos streams derivados del mismo punto son independientes`() {
        val stream = streamOf(
            token(TokenType.LET, "let"),
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.COLON, ":"),
            token(TokenType.EOF),
        )

        val ramaA = stream.advance()
        val ramaB = stream.advance().advance()

        assertEquals(TokenType.IDENTIFIER, valueOf(ramaA.peek()).type)
        assertEquals(TokenType.COLON, valueOf(ramaB.peek()).type)
        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
    }

    // ---- PEEK ----

    @Test
    fun `peek no avanza el stream`() {
        val stream = streamOf(token(TokenType.LET, "let"), token(TokenType.IDENTIFIER, "a"))

        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
    }

    @Test
    fun `advance encadenado recorre la secuencia completa`() {
        val stream = streamOf(
            token(TokenType.LET, "let"),
            token(TokenType.IDENTIFIER, "a"),
            token(TokenType.SEMICOLON, ";"),
            token(TokenType.EOF),
        )

        assertEquals(
            listOf(TokenType.LET, TokenType.IDENTIFIER, TokenType.SEMICOLON),
            walk(stream),
        )
    }

    // ---- NEXT ----

    @Test
    fun `next devuelve el token y el stream que sigue`() {
        val stream = streamOf(token(TokenType.LET, "let"), token(TokenType.IDENTIFIER, "a"))

        val (token, rest) = parsedOf(stream.next())

        assertEquals(TokenType.LET, token.type)
        assertEquals(TokenType.IDENTIFIER, valueOf(rest.peek()).type)
    }

    /** Sin EOF y sin tokens, leer es un error: la fuente se cortó antes de tiempo. */
    @Test
    fun `next sobre una fuente agotada da UnexpectedEndOfInput`() {
        val stream = streamOf()

        assertIs<UnexpectedEndOfInput>(errorOf(stream.next()))
    }

    // ---- EXPECT / SKIP ----

    @Test
    fun `expect con el tipo correcto devuelve el token`() {
        val esperado = token(TokenType.LET, "let")
        val stream = streamOf(esperado)

        // Se compara el token entero, no un campo suelto: lo que promete expect
        // es devolver ESE token, y así el test no depende de qué campos tenga.
        assertEquals(esperado, parsedOf(stream.expect(TokenType.LET)).value)
    }

    @Test
    fun `expect con el tipo equivocado devuelve SyntaxError en la posicion del token`() {
        val inesperado = token(TokenType.PRINTLN, "println")
        val stream = streamOf(inesperado)

        val error = errorOf(stream.expect(TokenType.SEMICOLON))

        assertIs<SyntaxError>(error)
        assertTrue(error.message.contains("';'"), "el mensaje debería decir qué esperaba: ${error.message}")
        assertEquals(inesperado.range, error.range)
    }

    /** El mensaje sale del tipo esperado, así nadie lo escribe a mano en cada call site. */
    @Test
    fun `el mensaje de expect describe el tipo esperado`() {
        val stream = streamOf(token(TokenType.SEMICOLON, ";"))

        val error = errorOf(stream.expect(TokenType.IDENTIFIER))

        assertTrue(
            error.message.contains("un identificador"),
            "esperaba la descripción del tipo: ${error.message}",
        )
    }

    @Test
    fun `expect fallido no consume nada`() {
        val stream = streamOf(token(TokenType.ASSIGN, "="), token(TokenType.EOF))

        errorOf(stream.expect(TokenType.COLON))

        // La razón de ser de la inmutabilidad: el parser que falló descarta su
        // rama y quien tenía el stream original lo sigue teniendo intacto.
        assertEquals(TokenType.ASSIGN, valueOf(stream.peek()).type)
    }

    @Test
    fun `expect propaga un error lexico sin transformarlo`() {
        val lexico = object : PrintScriptError {
            override val message = "Caracter inesperado '@'"
            override val range = Range(Position(1, 5), Position(1, 5))
        }
        val stream = TokenStream.of(sequenceOf(Result.Failure(lexico)))

        val error = errorOf(stream.expect(TokenType.LET))

        assertEquals(lexico, error, "el error del lexer tiene que salir tal cual, no disfrazado de SyntaxError")
    }

    @Test
    fun `skip consume el token esperado y devuelve el stream`() {
        val stream = streamOf(token(TokenType.COLON, ":"), token(TokenType.TYPE_NUMBER, "number"))

        val rest = stream.skip(TokenType.COLON)

        assertIs<Result.Success<TokenStream>>(rest)
        assertEquals(TokenType.TYPE_NUMBER, valueOf(rest.value.peek()).type)
    }

    @Test
    fun `skip falla si el token no es el esperado`() {
        val stream = streamOf(token(TokenType.ASSIGN, "="))

        errorOf(stream.skip(TokenType.COLON))
    }

    // ---- PEEKIS / ATEND ----

    @Test
    fun `peekIs no consume`() {
        val stream = streamOf(token(TokenType.LET, "let"))

        assertTrue(stream.peekIs(TokenType.LET))
        assertFalse(stream.peekIs(TokenType.PRINTLN))
        assertEquals(TokenType.LET, valueOf(stream.peek()).type)
    }

    @Test
    fun `atEnd detecta el EOF`() {
        val stream = streamOf(token(TokenType.SEMICOLON, ";"), token(TokenType.EOF))

        assertFalse(stream.atEnd())
        assertTrue(stream.advance().atEnd())
    }

    @Test
    fun `atEnd detecta la fuente agotada`() {
        assertTrue(streamOf().atEnd())
    }

    /** Un error léxico no es el final: hay que reportarlo y seguir leyendo. */
    @Test
    fun `un error lexico no cuenta como final del stream`() {
        val lexico = object : PrintScriptError {
            override val message = "Caracter inesperado '@'"
            override val range = Range(Position(1, 1), Position(1, 1))
        }
        val stream = TokenStream.of(
            sequenceOf(
                Result.Failure(lexico),
                Result.Success(token(TokenType.EOF)) as Result<Token, PrintScriptError>,
            ),
        )

        assertFalse(stream.atEnd(), "todavía queda el EOF por leer")
        assertTrue(stream.advance().atEnd())
    }

    // ---- PEREZA ----

    /**
     * La inmutabilidad no puede costar la pereza: sin memorizar por nodo, cada
     * stream derivado volvería a tirar del iterator y habría que leer todo por
     * adelantado. Con `by lazy` cada token se produce una sola vez y solo si
     * alguien lo pide.
     */
    @Test
    fun `el stream no consume mas tokens de los pedidos`() {
        var producidos = 0
        val perezosa = sequence {
            producidos++
            yield(Result.Success(token(TokenType.LET, "let")) as Result<Token, PrintScriptError>)
            producidos++
            yield(Result.Success(token(TokenType.IDENTIFIER, "a")))
            producidos++
            yield(Result.Success(token(TokenType.EOF)))
        }
        val stream = TokenStream.of(perezosa)

        stream.peek()
        stream.peek()

        assertEquals(1, producidos, "mirar el primer token no debería producir los siguientes")
    }

    @Test
    fun `releer el mismo nodo no vuelve a tirar del iterator`() {
        var producidos = 0
        val perezosa = sequence {
            producidos++
            yield(Result.Success(token(TokenType.LET, "let")) as Result<Token, PrintScriptError>)
            producidos++
            yield(Result.Success(token(TokenType.EOF)))
        }
        val stream = TokenStream.of(perezosa)

        val avanzado = stream.advance()
        avanzado.peek()
        avanzado.peek()
        stream.peek()

        assertEquals(2, producidos, "cada nodo memoriza su token: dos nodos, dos lecturas")
    }
}
