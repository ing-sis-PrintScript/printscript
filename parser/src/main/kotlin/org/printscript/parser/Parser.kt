package org.printscript.parser

import org.printscript.ast.ASTNode
import org.printscript.common.PrintScriptError
import org.printscript.common.Result
import org.printscript.parser.statements.StatementParser
import org.printscript.token.Token

/**
 * Convierte una secuencia de tokens en una secuencia de nodos del AST.
 *
 * Entra Sequence, sale Sequence: un statement por vez, tirando del lexer solo
 * lo necesario. En ningún momento existe el AST completo en memoria, que es lo
 * que pide la consigna para fuentes que no entran en RAM.
 *
 * No sabe parsear nada: elige quién parsea y coordina qué hacer ante un error.
 * La gramática vive en los StatementParser y en el ExpressionParser.
 *
 * Tampoco sabe qué versión del lenguaje está parseando: la lista de parsers es
 * obligatoria y viene de afuera. Un default apuntando a PrintScript10 dejaba la
 * 1.0 horneada adentro de la clase que justamente no tiene que conocerla.
 */
class Parser(
    private val statementParsers: List<StatementParser>,
) {

    fun parse(tokens: Sequence<Result<Token, PrintScriptError>>): Sequence<Result<ASTNode, PrintScriptError>> =
        sequence {
            val stream = TokenStream(tokens)

            while (!stream.atEnd()) {
                val result = parseStatement(stream)
                yield(result)

                // Tras un error el stream quedó en el medio de un statement roto.
                // Sin sincronizar, el próximo intento arrancaría desde la mitad y
                // produciría errores en cascada, todos consecuencia del primero.
                if (result is Result.Failure) stream.synchronize()
            }
        }

    /**
     * Un peek para elegir, y el parser elegido consume TODOS sus tokens,
     * incluido el primero. El que decide, mira; el que construye, consume.
     */
    private fun parseStatement(stream: TokenStream): Result<ASTNode, PrintScriptError> {
        val peeked = stream.peek()
        if (peeked is Result.Failure) {
            // Error léxico: lo reenviamos tal cual. No es nuestro y no podemos
            // arreglarlo, pero tampoco lo disfrazamos de error de sintaxis.
            stream.next() // consumirlo, para que synchronize no lo vuelva a leer
            return peeked
        }

        val token = (peeked as Result.Success).value
        val parser = statementParsers.firstOrNull { it.canHandle(token.type) }
            ?: return Result.Failure(
                SyntaxError("No se esperaba '${token.lexeme}' acá", token.range),
            )

        return parser.parse(stream)
    }
}