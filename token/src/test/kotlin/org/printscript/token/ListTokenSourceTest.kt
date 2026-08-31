package org.printscript.token

import org.printscript.common.Position
import org.printscript.common.Range
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ListTokenSourceTest {
    private fun token(
        type: TokenType,
        value: String = "",
    ): Token = Token(type, value, Range(Position(1, 1), Position(1, 1)))

    private fun successOf(result: TokenReadResult): TokenReadResult.Success {
        assertIs<TokenReadResult.Success>(result, "esperaba un token y vino $result")
        return result
    }

    private fun TokenSource.allTokens(): List<Token> =
        when (val result = nextToken()) {
            is TokenReadResult.Success -> listOf(result.token) + result.remaining.allTokens()
            is TokenReadResult.Failure -> result.remaining.allTokens()
            TokenReadResult.EndOfInput -> emptyList()
        }

    private fun declaracion(): List<Token> =
        listOf(
            token(TokenType.LET, "let"),
            token(TokenType.IDENTIFIER, "x"),
            token(TokenType.SEMICOLON, ";"),
        )

    @Test
    fun `una lista vacia da EndOfInput`() {
        assertEquals(TokenReadResult.EndOfInput, ListTokenSource(emptyList()).nextToken())
    }

    @Test
    fun `un solo token sale y despues se termina`() {
        val unico = token(TokenType.LET, "let")

        val primera = successOf(ListTokenSource(listOf(unico)).nextToken())

        assertEquals(unico, primera.token)
        assertEquals(TokenReadResult.EndOfInput, primera.remaining.nextToken())
    }

    @Test
    fun `los tokens salen en orden`() {
        val tokens = declaracion()

        assertEquals(tokens, ListTokenSource(tokens).allTokens())
    }

    @Test
    fun `leer dos veces de la misma fuente devuelve lo mismo`() {
        val source = ListTokenSource(declaracion())

        assertEquals(source.nextToken(), source.nextToken())
    }

    @Test
    fun `un remaining guardado sigue dando la misma secuencia despues de recorrer todo`() {
        val tokens = declaracion()
        val source = ListTokenSource(tokens)
        val restoGuardado = successOf(source.nextToken()).remaining

        val primerRecorrido = restoGuardado.allTokens()
        source.allTokens()

        assertEquals(tokens.drop(1), primerRecorrido)
        assertEquals(primerRecorrido, restoGuardado.allTokens())
        assertEquals(tokens, source.allTokens())
    }
}
