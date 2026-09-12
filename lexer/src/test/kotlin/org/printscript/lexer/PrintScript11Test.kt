package org.printscript.lexer

import org.printscript.token.Token
import org.printscript.token.TokenReadResult
import org.printscript.token.TokenSource
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrintScript11Test {
    private val lexer11 = Lexer(TokenMatcher(PrintScript11.RULES))
    private val lexer10 = Lexer(TokenMatcher(PrintScript10.RULES))

    private fun drain(source: TokenSource): List<TokenReadResult> =
        when (val result = source.nextToken()) {
            is TokenReadResult.Success -> listOf(result) + drain(result.remaining)
            is TokenReadResult.Failure -> listOf(result) + drain(result.remaining)
            TokenReadResult.EndOfInput -> emptyList()
        }

    private fun tokensOf(
        lexer: Lexer,
        source: String,
    ): List<Token> {
        val resultados = drain(lexer.tokenize(source))
        assertTrue(resultados.all { it is TokenReadResult.Success }, "esperaba que funcione y falló")
        return resultados.filterIsInstance<TokenReadResult.Success>().map { it.token }
    }

    private fun typesOf(
        lexer: Lexer,
        source: String,
    ): List<TokenType> = tokensOf(lexer, source).map { it.type }

    @Test
    fun `const declara una constante`() {
        assertEquals(
            listOf(
                TokenType.CONST,
                TokenType.IDENTIFIER,
                TokenType.COLON,
                TokenType.TYPE_BOOLEAN,
                TokenType.ASSIGN,
                TokenType.BOOLEAN_LITERAL,
                TokenType.SEMICOLON,
                TokenType.EOF,
            ),
            typesOf(lexer11, "const activo: boolean = true;"),
        )
    }

    @Test
    fun `if con bloque y else`() {
        assertEquals(
            listOf(
                TokenType.IF,
                TokenType.LPAREN,
                TokenType.IDENTIFIER,
                TokenType.RPAREN,
                TokenType.LBRACE,
                TokenType.RBRACE,
                TokenType.ELSE,
                TokenType.LBRACE,
                TokenType.RBRACE,
                TokenType.EOF,
            ),
            typesOf(lexer11, "if(activo) {} else {}"),
        )
    }

    @Test
    fun `readInput y readEnv son keywords, no identificadores`() {
        assertEquals(TokenType.READ_INPUT, typesOf(lexer11, "readInput").first())
        assertEquals(TokenType.READ_ENV, typesOf(lexer11, "readEnv").first())
    }

    @Test
    fun `true y false son el mismo tipo y se distinguen por el valor`() {
        val verdadero = tokensOf(lexer11, "true").first()
        val falso = tokensOf(lexer11, "false").first()

        assertEquals(TokenType.BOOLEAN_LITERAL, verdadero.type)
        assertEquals(TokenType.BOOLEAN_LITERAL, falso.type)
        assertEquals("true", verdadero.value)
        assertEquals("false", falso.value)
    }

    @Test
    fun `1_1 sigue reconociendo todo lo de 1_0`() {
        val fuente = """let name: string = "Joe";  println(name + " ");"""

        assertEquals(typesOf(lexer10, fuente), typesOf(lexer11, fuente))
    }

    // Lo que hace que --version 1.0 rechace un programa 1.1 sin ningun chequeo especial:
    // la palabra no esta en el mapa de keywords de 1.0, asi que cae en IDENTIFIER y el
    // parser se queja de que no espera un identificador ahi.
    @Test
    fun `con las reglas de 1_0 las palabras de 1_1 son identificadores`() {
        for (palabra in listOf("const", "if", "else", "boolean", "true", "false", "readInput", "readEnv")) {
            assertEquals(TokenType.IDENTIFIER, typesOf(lexer10, palabra).first(), "fallo con '$palabra'")
        }
    }

    // Las llaves ni siquiera existen como simbolo en 1.0: no es un tipo distinto, es un
    // error lexico.
    @Test
    fun `con las reglas de 1_0 las llaves son un error lexico`() {
        val resultados = drain(lexer10.tokenize("{"))

        assertTrue(resultados.any { it is TokenReadResult.Failure })
    }

    @Test
    fun `la trivia se sigue guardando en 1_1`() {
        val tokens = tokensOf(lexer11, "if  (activo)")

        assertEquals(listOf("", "  ", "", ""), tokens.dropLast(1).map { it.leadingTrivia.text })
    }
}
