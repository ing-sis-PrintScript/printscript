package org.printscript.parser

import org.printscript.ast.ASTNode
import org.printscript.ast.AssignmentStatement
import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.CallExpression
import org.printscript.ast.DeclaredType
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.ast.VariableDeclaration
import org.printscript.common.Position
import org.printscript.common.PrintScriptError
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.token.Token
import org.printscript.token.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests del parser completo: de tokens a statements.
 *
 * Los tokens se arman a mano, sin lexer. El parser depende de "token", no de
 * "lexer", y estos tests lo demuestran: si mañana el lexer se reescribe entero,
 * ninguno de estos se rompe.
 */
class ParserTest {

    private val parser = Parser(PrintScript10.statementParsers())

    // ---- helpers ----

    private var column = 1

    private fun token(type: TokenType, text: String): Token {
        val start = Position(1, column)
        val end = Position(1, column + text.length - 1)
        column += text.length + 1
        val value = if (type == TokenType.STRING_LITERAL) text.trim('"') else text
        return Token(type, text, value, Range(start, end))
    }

    private fun eof() = Token(TokenType.EOF, "", "", Range(Position(1, column), Position(1, column)))

    private fun parse(vararg tokens: Token): List<Result<ASTNode, PrintScriptError>> {
        val all = (tokens.toList() + eof()).map { Result.Success(it) as Result<Token, PrintScriptError> }
        return parser.parse(all.asSequence()).toList()
    }

    private fun single(vararg tokens: Token): ASTNode {
        val results = parse(*tokens)
        assertEquals(1, results.size, "esperaba un solo statement, salieron ${results.size}")
        val first = results.first()
        assertIs<Result.Success<ASTNode>>(first, "esperaba parsear bien: $first")
        return first.value
    }

    private fun errorOf(vararg tokens: Token): PrintScriptError {
        val results = parse(*tokens)
        val failure = results.filterIsInstance<Result.Failure<PrintScriptError>>().firstOrNull()
        return failure?.error ?: error("esperaba un error y no hubo ninguno")
    }

    // atajos
    private fun let() = token(TokenType.LET, "let")
    private fun id(name: String) = token(TokenType.IDENTIFIER, name)
    private fun colon() = token(TokenType.COLON, ":")
    private fun assign() = token(TokenType.ASSIGN, "=")
    private fun semi() = token(TokenType.SEMICOLON, ";")
    private fun num(text: String) = token(TokenType.NUMBER_LITERAL, text)
    private fun str(text: String) = token(TokenType.STRING_LITERAL, "\"$text\"")
    private fun typeNumber() = token(TokenType.TYPE_NUMBER, "number")
    private fun typeString() = token(TokenType.TYPE_STRING, "string")
    private fun println_() = token(TokenType.PRINTLN, "println")
    private fun lparen() = token(TokenType.LPAREN, "(")
    private fun rparen() = token(TokenType.RPAREN, ")")
    private fun plus() = token(TokenType.PLUS, "+")
    private fun slash() = token(TokenType.SLASH, "/")

    // ---- DECLARACIÓN ----

    @Test
    fun `declaracion con inicializador`() {
        // let a: number = 12;
        val node = single(let(), id("a"), colon(), typeNumber(), assign(), num("12"), semi())

        val declaration = assertIs<VariableDeclaration>(node)
        assertEquals("a", declaration.identifier.name)
        assertEquals(DeclaredType.NUMBER, declaration.declaredType)
        assertEquals(12.0, assertIs<NumberLiteral>(declaration.initializer).value)
    }

    @Test
    fun `declaracion de string`() {
        // let name: string = "Joe";
        val node = single(let(), id("name"), colon(), typeString(), assign(), str("Joe"), semi())

        val declaration = assertIs<VariableDeclaration>(node)
        assertEquals(DeclaredType.STRING, declaration.declaredType)
        assertEquals("Joe", assertIs<StringLiteral>(declaration.initializer).value)
    }

    /** La gramática dice ["=", expression]: el inicializador es opcional. */
    @Test
    fun `declaracion sin inicializador`() {
        // let x: number;
        val node = single(let(), id("x"), colon(), typeNumber(), semi())

        val declaration = assertIs<VariableDeclaration>(node)
        assertEquals("x", declaration.identifier.name)
        assertNull(declaration.initializer, "sin '=' el initializer tiene que ser null")
    }

    @Test
    fun `declaracion con expresion como valor inicial`() {
        // let c: number = a / b;
        val node = single(let(), id("c"), colon(), typeNumber(), assign(), id("a"), slash(), id("b"), semi())

        val declaration = assertIs<VariableDeclaration>(node)
        val binary = assertIs<BinaryExpression>(declaration.initializer)
        assertEquals(BinaryOperator.DIVIDE, binary.operator)
    }

    /**
     * El parser NO chequea tipos: esto parsea perfecto y el árbol queda bien
     * formado. Que un string no entre en un number es problema del análisis
     * semántico, que es un módulo aparte.
     */
    @Test
    fun `un tipo que no cierra igual parsea`() {
        val node = single(let(), id("x"), colon(), typeNumber(), assign(), str("hola"), semi())

        val declaration = assertIs<VariableDeclaration>(node)
        assertEquals(DeclaredType.NUMBER, declaration.declaredType)
        assertIs<StringLiteral>(declaration.initializer)
    }

    // ---- ASIGNACIÓN ----

    @Test
    fun `asignacion simple`() {
        // a = 5;
        val node = single(id("a"), assign(), num("5"), semi())

        val assignment = assertIs<AssignmentStatement>(node)
        assertEquals("a", assignment.target.name)
        assertEquals(5.0, assertIs<NumberLiteral>(assignment.value).value)
    }

    @Test
    fun `asignacion con expresion`() {
        // a = a / b;
        val node = single(id("a"), assign(), id("a"), slash(), id("b"), semi())

        val assignment = assertIs<AssignmentStatement>(node)
        assertEquals(BinaryOperator.DIVIDE, assertIs<BinaryExpression>(assignment.value).operator)
    }

    // ---- LLAMADA ----

    /**
     * println produce DOS nodos anidados: el ExpressionStatement es la línea del
     * programa, el CallExpression es la llamada (que es Expression porque en 1.1
     * readInput va a devolver valor).
     */
    @Test
    fun `println produce ExpressionStatement con CallExpression adentro`() {
        // println(a);
        val node = single(println_(), lparen(), id("a"), rparen(), semi())

        val statement = assertIs<ExpressionStatement>(node)
        val call = assertIs<CallExpression>(statement.expression)
        assertEquals("println", call.callee.name)
        assertEquals(1, call.arguments.size)
        assertEquals("a", assertIs<Identifier>(call.arguments.first()).name)
    }

    @Test
    fun `println con una expresion como argumento`() {
        // println("Result: " + c);
        val node = single(println_(), lparen(), str("Result: "), plus(), id("c"), rparen(), semi())

        val call = assertIs<CallExpression>(assertIs<ExpressionStatement>(node).expression)
        val argument = assertIs<BinaryExpression>(call.arguments.first())
        assertEquals(BinaryOperator.PLUS, argument.operator)
    }

    // ---- LOS TRES EJEMPLOS DE LA CONSIGNA ----

    /**
     * let name: string = "Joe";
     * let lastName: string = "Doe";
     * println(name + " " + lastName);
     */
    @Test
    fun `ejemplo 1 de la consigna`() {
        val results = parse(
            let(), id("name"), colon(), typeString(), assign(), str("Joe"), semi(),
            let(), id("lastName"), colon(), typeString(), assign(), str("Doe"), semi(),
            println_(), lparen(), id("name"), plus(), str(" "), plus(), id("lastName"), rparen(), semi(),
        )

        assertEquals(3, results.size)
        assertTrue(results.all { it is Result.Success }, "los tres statements tienen que parsear")

        val nodes = results.filterIsInstance<Result.Success<ASTNode>>().map { it.value }
        assertIs<VariableDeclaration>(nodes[0])
        assertIs<VariableDeclaration>(nodes[1])
        assertIs<ExpressionStatement>(nodes[2])
    }

    /**
     * let a: number = 12;
     * let b: number = 4;
     * let c: number = a / b;
     * println("Result: " + c);
     */
    @Test
    fun `ejemplo 2 de la consigna`() {
        val results = parse(
            let(), id("a"), colon(), typeNumber(), assign(), num("12"), semi(),
            let(), id("b"), colon(), typeNumber(), assign(), num("4"), semi(),
            let(), id("c"), colon(), typeNumber(), assign(), id("a"), slash(), id("b"), semi(),
            println_(), lparen(), str("Result: "), plus(), id("c"), rparen(), semi(),
        )

        assertEquals(4, results.size)
        assertTrue(results.all { it is Result.Success })
    }

    /**
     * let a: number = 12;
     * let b: number = 4;
     * a = a / b;
     * println("Result: " + a);
     */
    @Test
    fun `ejemplo 3 de la consigna`() {
        val results = parse(
            let(), id("a"), colon(), typeNumber(), assign(), num("12"), semi(),
            let(), id("b"), colon(), typeNumber(), assign(), num("4"), semi(),
            id("a"), assign(), id("a"), slash(), id("b"), semi(),
            println_(), lparen(), str("Result: "), plus(), id("a"), rparen(), semi(),
        )

        assertEquals(4, results.size)
        assertTrue(results.all { it is Result.Success })

        val nodes = results.filterIsInstance<Result.Success<ASTNode>>().map { it.value }
        assertIs<AssignmentStatement>(nodes[2], "la tercera línea es asignación, no declaración")
    }

    // ---- ERRORES ----

    @Test
    fun `falta el punto y coma`() {
        val error = errorOf(let(), id("a"), colon(), typeNumber(), assign(), num("12"))

        assertIs<SyntaxError>(error)
        assertTrue(error.message.contains("';'"), "el mensaje debería pedir el ';': ${error.message}")
    }

    @Test
    fun `tipo invalido en la declaracion`() {
        val error = errorOf(let(), id("a"), colon(), id("entero"), assign(), num("12"), semi())

        assertIs<SyntaxError>(error)
        assertTrue(error.message.contains("number"), "debería decir qué tipos son válidos: ${error.message}")
    }

    @Test
    fun `un token que no puede empezar un statement`() {
        val error = errorOf(plus(), num("2"), semi())

        assertIs<SyntaxError>(error)
    }

    /**
     * El mensaje describe el TIPO del token, no su texto. Antes salía el
     * lexema crudo ("No se esperaba '"hola"' acá", con las comillas del
     * literal adentro del mensaje); ahora el texto se deriva del TokenType,
     * así que no depende de qué campos tenga Token.
     */
    @Test
    fun `el error describe el tipo del token, no su texto`() {
        val error = errorOf(str("hola"), semi())

        assertIs<SyntaxError>(error)
        assertTrue(
            error.message.contains("un string"),
            "el mensaje debería describir el tipo: ${error.message}",
        )
        assertTrue(
            !error.message.contains("hola"),
            "el mensaje no debería depender del texto del token: ${error.message}",
        )
    }

    /** El error del lexer se reenvía tal cual, sin envolverlo en un SyntaxError. */
    @Test
    fun `un error lexico se propaga`() {
        val lexico = object : PrintScriptError {
            override val message = "Caracter inesperado '@'"
            override val range = Range(Position(1, 1), Position(1, 1))
        }
        val results = parser.parse(
            sequenceOf(
                Result.Failure(lexico),
                Result.Success(eof()),
            ),
        ).toList()

        val failure = assertIs<Result.Failure<PrintScriptError>>(results.first())
        assertEquals(lexico, failure.error)
    }

    // ---- RECUPERACIÓN ----

    /**
     * El test que justifica haber elegido Result sobre excepciones: después de
     * un error el parser sigue, y el usuario ve todos los problemas de una
     * pasada en vez de uno por corrida.
     */
    @Test
    fun `despues de un error el parser retoma`() {
        // let a: number = 12;   ← ok
        // let b: number = ;     ← roto (falta el valor)
        // let c: number = 3;    ← ok, tiene que parsear igual
        val results = parse(
            let(), id("a"), colon(), typeNumber(), assign(), num("12"), semi(),
            let(), id("b"), colon(), typeNumber(), assign(), semi(),
            let(), id("c"), colon(), typeNumber(), assign(), num("3"), semi(),
        )

        assertEquals(3, results.size)
        assertIs<Result.Success<ASTNode>>(results[0])
        assertIs<Result.Failure<PrintScriptError>>(results[1])
        assertIs<Result.Success<ASTNode>>(results[2], "el statement posterior al error tiene que recuperarse")
    }

    // ---- RANGES ----

    @Test
    fun `el range de una declaracion va del let al punto y coma`() {
        val letToken = let()
        val a = id("a")
        val dosPuntos = colon()
        val tipo = typeNumber()
        val igual = assign()
        val valor = num("12")
        val puntoYComa = semi()

        val node = single(letToken, a, dosPuntos, tipo, igual, valor, puntoYComa)

        assertEquals(letToken.range.start, node.range.start, "el statement arranca en el 'let'")
        assertEquals(puntoYComa.range.end, node.range.end, "y termina en el ';'")
    }

    /**
     * El statement incluye el ";", la llamada no: si el linter reporta algo
     * sobre println, no debería subrayar el punto y coma.
     */
    @Test
    fun `el range de la llamada termina antes que el del statement`() {
        val node = single(println_(), lparen(), id("a"), rparen(), semi())

        val statement = assertIs<ExpressionStatement>(node)
        val call = assertIs<CallExpression>(statement.expression)

        assertEquals(statement.range.start, call.range.start, "los dos arrancan en println")
        assertTrue(
            call.range.end.column < statement.range.end.column,
            "la llamada termina en ')' y el statement en ';'",
        )
    }

    // ---- STREAMING ----

    /**
     * La propiedad que sostiene todo el diseño: el parser produce statements de
     * a uno, sin leer el archivo entero. Si esto se rompe, se rompió el
     * requisito de la consigna sobre fuentes que no entran en memoria.
     */
    @Test
    fun `el parser no consume mas tokens de los necesarios`() {
        var producidos = 0
        val perezosa = sequence {
            val tokens = listOf(
                let(), id("a"), colon(), typeNumber(), assign(), num("12"), semi(),
                let(), id("b"), colon(), typeNumber(), assign(), num("4"), semi(),
                eof(),
            )
            for (t in tokens) {
                producidos++
                yield(Result.Success(t) as Result<Token, PrintScriptError>)
            }
        }

        // pido SOLO el primer statement
        parser.parse(perezosa).first()

        assertTrue(
            producidos <= 8,
            "para el primer statement no deberían leerse los tokens del segundo (leyó $producidos)",
        )
    }
}