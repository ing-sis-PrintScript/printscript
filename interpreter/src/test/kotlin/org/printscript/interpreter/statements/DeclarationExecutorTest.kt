package org.printscript.interpreter.statements

import org.printscript.ast.DeclaredType
import org.printscript.ast.Expression
import org.printscript.ast.ExpressionStatement
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.VariableDeclaration
import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import org.printscript.interpreter.Environment
import org.printscript.interpreter.InterpreterError
import org.printscript.interpreter.PrintScriptValue
import org.printscript.interpreter.io.PrintScriptIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeclarationExecutorTest {
    private val dummyRange = Range(Position(1, 1), Position(1, 10))
    private val executor = DeclarationExecutor()

    private object NoOpIO : PrintScriptIO {
        override fun print(message: String) = Unit

        override fun read(prompt: String): String = ""
    }

    private fun declaration(
        name: String,
        type: DeclaredType,
        initializer: Expression?,
    ) = VariableDeclaration(Identifier(name, dummyRange), type, initializer, dummyRange)

    private fun valueOf(result: Result<Environment, InterpreterError>?): Environment {
        assertIs<Result.Success<Environment>>(result, "esperaba Success y vino Failure o null")
        return result.value
    }

    private fun errorOf(result: Result<Environment, InterpreterError>?): InterpreterError {
        assertIs<Result.Failure<InterpreterError>>(result, "esperaba Failure y vino Success o null")
        return result.error
    }

    @Test
    fun `devuelve null si el statement no es una VariableDeclaration`() {
        val notADeclaration = ExpressionStatement(NumberLiteral(1.0, dummyRange), dummyRange)

        assertEquals(null, executor.execute(notADeclaration, Environment(), NoOpIO))
    }

    @Test
    fun `declara una variable con inicializador y la deja disponible en el Environment devuelto`() {
        val declaration = declaration("x", DeclaredType.NUMBER, NumberLiteral(5.0, dummyRange))

        val env = valueOf(executor.execute(declaration, Environment(), NoOpIO))

        assertEquals(PrintScriptValue.NumberValue(5.0), (env.get("x", dummyRange) as Result.Success).value)
    }

    @Test
    fun `declara una variable sin inicializador`() {
        val declaration = declaration("x", DeclaredType.STRING, null)

        val env = valueOf(executor.execute(declaration, Environment(), NoOpIO))

        val error = env.get("x", dummyRange)
        assertIs<Result.Failure<InterpreterError>>(error)
        assertEquals("La variable 'x' no ha sido inicializada.", error.error.message)
    }

    @Test
    fun `propaga el error si el inicializador falla al evaluarse`() {
        val declaration = declaration("x", DeclaredType.NUMBER, Identifier("noExiste", dummyRange))

        val error = errorOf(executor.execute(declaration, Environment(), NoOpIO))

        assertEquals("La variable 'noExiste' no ha sido declarada.", error.message)
    }

    @Test
    fun `propaga el error de Environment si la variable ya existia`() {
        val alreadyDeclared =
            (
                Environment().declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(1.0), dummyRange)
                    as Result.Success
            ).value
        val declaration = declaration("x", DeclaredType.NUMBER, NumberLiteral(2.0, dummyRange))

        val error = errorOf(executor.execute(declaration, alreadyDeclared, NoOpIO))

        assertEquals("La variable 'x' ya fue declarada.", error.message)
    }
}
