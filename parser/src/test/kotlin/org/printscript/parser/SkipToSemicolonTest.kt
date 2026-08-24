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
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * La política de recuperación de PrintScript 1.0, ahora separada del TokenStream.
 *
 * Que viva en su propia clase es lo que va a permitir que 1.1 use otra (con
 * bloques `{ }`, cortar siempre en el ";" se comería el "}") sin tocar ni el
 * Parser ni el stream.
 */
class SkipToSemicolonTest {
    private var column = 1

    private fun token(
        type: TokenType,
        text: String = "",
    ): Token {
        val start = Position(1, column)
        val end = Position(1, column + text.length - 1)
        column += text.length + 1
        return Token(type, text, Range(start, end))
    }

    private fun streamOf(vararg tokens: Token): TokenStream {
        val results = tokens.map { Result.Success(it) as Result<Token, PrintScriptError> }
        return TokenStream.of(results.asSequence())
    }

    private fun typeAt(stream: TokenStream): TokenType {
        val peeked = stream.peek()
        assertIs<Result.Success<Token>>(peeked, "esperaba un token y vino un error")
        return peeked.value.type
    }

    @Test
    fun `deja el stream despues del punto y coma`() {
        // descarta: println ( "hola" ) ;  → retoma en el let siguiente
        val stream =
            streamOf(
                token(TokenType.PRINTLN, "println"),
                token(TokenType.LPAREN, "("),
                token(TokenType.STRING_LITERAL, "\"hola\""),
                token(TokenType.RPAREN, ")"),
                token(TokenType.SEMICOLON, ";"),
                token(TokenType.LET, "let"),
                token(TokenType.EOF),
            )

        assertEquals(TokenType.LET, typeAt(SkipToSemicolon.recover(stream)))
    }

    @Test
    fun `corta en el primer punto y coma, no en el ultimo`() {
        val stream =
            streamOf(
                token(TokenType.IDENTIFIER, "a"),
                token(TokenType.SEMICOLON, ";"),
                token(TokenType.LET, "let"),
                token(TokenType.SEMICOLON, ";"),
                token(TokenType.EOF),
            )

        assertEquals(TokenType.LET, typeAt(SkipToSemicolon.recover(stream)))
    }

    /** El que protege del loop infinito: sin ";" tiene que frenar en el EOF. */
    @Test
    fun `sin punto y coma llega al EOF y termina`() {
        val stream =
            streamOf(
                token(TokenType.IDENTIFIER, "a"),
                token(TokenType.PLUS, "+"),
                token(TokenType.NUMBER_LITERAL, "5"),
                token(TokenType.EOF),
            )

        assertTrue(SkipToSemicolon.recover(stream).atEnd())
    }

    @Test
    fun `sobre un stream ya en EOF no hace nada`() {
        val stream = streamOf(token(TokenType.EOF))

        assertTrue(SkipToSemicolon.recover(stream).atEnd())
    }

    /** Un error léxico no frena la recuperación: se descarta como cualquier token. */
    @Test
    fun `descarta tambien los errores lexicos`() {
        val lexico =
            object : PrintScriptError {
                override val message = "Caracter inesperado '@'"
                override val range = Range(Position(1, 1), Position(1, 1))
            }
        val stream =
            TokenStream.of(
                sequenceOf(
                    Result.Failure(lexico),
                    Result.Success(token(TokenType.SEMICOLON, ";")) as Result<Token, PrintScriptError>,
                    Result.Success(token(TokenType.LET, "let")),
                    Result.Success(token(TokenType.EOF)),
                ),
            )

        assertEquals(TokenType.LET, typeAt(SkipToSemicolon.recover(stream)))
    }

    /** No muta lo que recibe: el stream original sigue en su lugar. */
    @Test
    fun `no toca el stream que recibe`() {
        val stream =
            streamOf(
                token(TokenType.IDENTIFIER, "a"),
                token(TokenType.SEMICOLON, ";"),
                token(TokenType.LET, "let"),
                token(TokenType.EOF),
            )

        SkipToSemicolon.recover(stream)

        assertEquals(TokenType.IDENTIFIER, typeAt(stream))
    }
}
