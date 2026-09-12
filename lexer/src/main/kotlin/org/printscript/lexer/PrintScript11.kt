package org.printscript.lexer

import org.printscript.lexer.rules.NumberRule
import org.printscript.lexer.rules.StringRule
import org.printscript.lexer.rules.SymbolRule
import org.printscript.lexer.rules.TokenRule
import org.printscript.lexer.rules.WordRule
import org.printscript.token.TokenType

// Que reconoce el lexer en PrintScript 1.1.
//
// La consigna dice que 1.1 es "una simple extension de la version 1.0", asi que los mapas
// se arman SOBRE los de 1.0 en vez de repetirlos: si manana cambia una keyword de 1.0,
// cambia en un solo lugar y las dos versiones la ven.
//
// 'true' y 'false' son keywords que producen un BOOLEAN_LITERAL, igual que un numero
// produce un NUMBER_LITERAL. No hace falta una regla nueva: WordRule ya busca la palabra
// en el mapa y usa IDENTIFIER cuando no esta.
object PrintScript11 {
    val KEYWORDS: Map<String, TokenType> =
        PrintScript10.KEYWORDS +
            mapOf(
                "const" to TokenType.CONST,
                "if" to TokenType.IF,
                "else" to TokenType.ELSE,
                "boolean" to TokenType.TYPE_BOOLEAN,
                "true" to TokenType.BOOLEAN_LITERAL,
                "false" to TokenType.BOOLEAN_LITERAL,
                // Son keywords y no identificadores, igual que println: asi el parser
                // despacha por tipo. El costo es que dejan de servir como nombre de
                // variable, que es lo que ya pasa con println en 1.0.
                "readInput" to TokenType.READ_INPUT,
                "readEnv" to TokenType.READ_ENV,
            )

    val SYMBOLS: Map<String, TokenType> =
        PrintScript10.SYMBOLS +
            mapOf(
                "{" to TokenType.LBRACE,
                "}" to TokenType.RBRACE,
            )

    val RULES: List<TokenRule> =
        listOf(
            NumberRule,
            WordRule(KEYWORDS),
            StringRule,
            SymbolRule(SYMBOLS),
        )
}
