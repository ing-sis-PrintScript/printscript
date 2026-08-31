package org.printscript.interpreter

import org.printscript.ast.BinaryExpression
import org.printscript.ast.BinaryOperator
import org.printscript.ast.CallExpression
import org.printscript.ast.DeclaredType
import org.printscript.ast.Expression
import org.printscript.ast.Identifier
import org.printscript.ast.NumberLiteral
import org.printscript.ast.StringLiteral
import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExpressionEvaluatorTest {
    private val dummyRange = Range(Position(1, 1), Position(1, 10))
    private val evaluator = ExpressionEvaluator()

    /** Desempaqueta un Success o falla el test. */
    private fun valueOf(result: Result<PrintScriptValue, InterpreterError>): PrintScriptValue {
        assertIs<Result.Success<PrintScriptValue>>(result, "esperaba Success y vino Failure")
        return result.value
    }

    /** Desempaqueta un Failure o falla el test. */
    private fun errorOf(result: Result<PrintScriptValue, InterpreterError>): InterpreterError {
        assertIs<Result.Failure<InterpreterError>>(result, "esperaba Failure y vino Success")
        return result.error
    }

    private fun eval(
        expression: Expression,
        env: Environment = Environment(),
    ) = evaluator.evaluate(expression, env)

    private fun number(value: Double) = NumberLiteral(value, dummyRange)

    private fun string(value: String) = StringLiteral(value, dummyRange)

    private fun binary(
        operator: BinaryOperator,
        left: Expression,
        right: Expression,
    ) = BinaryExpression(operator, left, right, dummyRange)

    /** Evalúa "left operator right" entre dos números y desempaqueta el resultado. */
    private fun evalNumberBinary(
        operator: BinaryOperator,
        left: Double,
        right: Double,
    ) = valueOf(eval(binary(operator, number(left), number(right))))

    @Test
    fun `un NumberLiteral evalua a su propio valor`() {
        assertEquals(PrintScriptValue.NumberValue(5.0), valueOf(eval(number(5.0))))
    }

    @Test
    fun `un StringLiteral evalua a su propio valor`() {
        assertEquals(PrintScriptValue.StringValue("hola"), valueOf(eval(string("hola"))))
    }

    @Test
    fun `un Identifier se resuelve contra el Environment`() {
        val declared =
            Environment().declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(42.0), dummyRange)
        val env = (declared as Result.Success).value

        assertEquals(PrintScriptValue.NumberValue(42.0), valueOf(eval(Identifier("x", dummyRange), env)))
    }

    @Test
    fun `un Identifier no declarado falla con el error de Environment`() {
        val error = errorOf(eval(Identifier("x", dummyRange)))

        assertEquals("La variable 'x' no ha sido declarada.", error.message)
    }

    @Test
    fun `suma entre numeros`() {
        assertEquals(PrintScriptValue.NumberValue(7.0), evalNumberBinary(BinaryOperator.PLUS, 3.0, 4.0))
    }

    @Test
    fun `resta entre numeros`() {
        assertEquals(PrintScriptValue.NumberValue(1.0), evalNumberBinary(BinaryOperator.MINUS, 3.0, 2.0))
    }

    @Test
    fun `multiplicacion entre numeros`() {
        assertEquals(PrintScriptValue.NumberValue(6.0), evalNumberBinary(BinaryOperator.TIMES, 3.0, 2.0))
    }

    @Test
    fun `division entre numeros`() {
        assertEquals(PrintScriptValue.NumberValue(2.0), evalNumberBinary(BinaryOperator.DIVIDE, 6.0, 3.0))
    }

    @Test
    fun `mas concatena cuando alguno de los dos lados es string`() {
        assertEquals(
            PrintScriptValue.StringValue("valor: 5"),
            valueOf(eval(binary(BinaryOperator.PLUS, string("valor: "), number(5.0)))),
        )
        assertEquals(
            PrintScriptValue.StringValue("5 es el valor"),
            valueOf(eval(binary(BinaryOperator.PLUS, number(5.0), string(" es el valor")))),
        )
    }

    @Test
    fun `division por cero falla`() {
        val error = errorOf(eval(binary(BinaryOperator.DIVIDE, number(10.0), number(0.0))))

        assertEquals("División por cero.", error.message)
    }

    @Test
    fun `operacion no soportada entre strings falla`() {
        val error = errorOf(eval(binary(BinaryOperator.MINUS, string("a"), string("b"))))

        assertEquals("Operación inválida entre 'a' y 'b'.", error.message)
    }

    @Test
    fun `una CallExpression anidada dentro de otra expresion no es soportada`() {
        val call = CallExpression(Identifier("println", dummyRange), listOf(number(1.0)), dummyRange)

        val error = errorOf(eval(call))

        assertEquals("No se puede usar la llamada a 'println' dentro de una expresión.", error.message)
    }
}
