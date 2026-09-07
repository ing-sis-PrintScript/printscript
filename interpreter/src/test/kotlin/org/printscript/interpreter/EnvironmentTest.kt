package org.printscript.interpreter

import org.printscript.ast.DeclaredType
import org.printscript.common.Position
import org.printscript.common.Range
import org.printscript.common.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EnvironmentTest {
    private val dummyRange = Range(Position(1, 1), Position(1, 10))

    /** Desempaqueta un Success o falla el test. */
    private fun <T> valueOf(result: Result<T, InterpreterError>): T {
        assertIs<Result.Success<T>>(result, "esperaba Success y vino Failure")
        return result.value
    }

    /** Desempaqueta un Failure o falla el test. */
    private fun <T> errorOf(result: Result<T, InterpreterError>): InterpreterError {
        assertIs<Result.Failure<InterpreterError>>(result, "esperaba Failure y vino Success")
        return result.error
    }

    @Test
    fun `declarar una variable nueva es exitoso`() {
        val env = Environment()

        val result = env.declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(5.0), dummyRange)

        valueOf(result)
    }

    @Test
    fun `declarar no muta el Environment original`() {
        val env = Environment()

        env.declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(5.0), dummyRange)

        // El env original nunca tuvo "x": declare devolvió uno nuevo, no lo mutó.
        val result = env.get("x", dummyRange)
        assertEquals("La variable 'x' no ha sido declarada.", errorOf(result).message)
    }

    @Test
    fun `declarar una variable dos veces falla`() {
        val env = valueOf(env().declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(1.0), dummyRange))

        val result = env.declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(2.0), dummyRange)

        assertEquals("La variable 'x' ya fue declarada.", errorOf(result).message)
    }

    @Test
    fun `declarar con un valor de tipo distinto al declarado falla`() {
        val result = env().declare("x", DeclaredType.NUMBER, PrintScriptValue.StringValue("hola"), dummyRange)

        assertEquals(
            "Se esperaba un tipo 'NUMBER' pero se obtuvo un valor distinto.",
            errorOf(result).message,
        )
    }

    @Test
    fun `declarar sin inicializador no chequea tipo y queda no inicializada`() {
        val env = valueOf(env().declare("x", DeclaredType.STRING, null, dummyRange))

        val result = env.get("x", dummyRange)

        assertEquals("La variable 'x' no ha sido inicializada.", errorOf(result).message)
    }

    @Test
    fun `assign sobre variable no declarada falla`() {
        val result = env().assign("x", PrintScriptValue.NumberValue(1.0), dummyRange)

        assertEquals("La variable 'x' no ha sido declarada.", errorOf(result).message)
    }

    @Test
    fun `assign con tipo incompatible falla`() {
        val env = valueOf(env().declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(1.0), dummyRange))

        val result = env.assign("x", PrintScriptValue.StringValue("hola"), dummyRange)

        assertEquals(
            "Se esperaba un tipo 'NUMBER' pero se obtuvo un valor distinto.",
            errorOf(result).message,
        )
    }

    @Test
    fun `assign no muta el Environment original`() {
        val declared = valueOf(env().declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(1.0), dummyRange))

        declared.assign("x", PrintScriptValue.NumberValue(99.0), dummyRange)

        // El env sobre el que se llamó assign sigue teniendo el valor viejo.
        assertEquals(PrintScriptValue.NumberValue(1.0), valueOf(declared.get("x", dummyRange)))
    }

    @Test
    fun `assign exitoso actualiza el valor para el Environment devuelto`() {
        val declared = valueOf(env().declare("x", DeclaredType.NUMBER, PrintScriptValue.NumberValue(1.0), dummyRange))

        val reassigned = valueOf(declared.assign("x", PrintScriptValue.NumberValue(99.0), dummyRange))

        assertEquals(PrintScriptValue.NumberValue(99.0), valueOf(reassigned.get("x", dummyRange)))
    }

    @Test
    fun `get sobre variable no declarada falla`() {
        val result = env().get("x", dummyRange)

        assertEquals("La variable 'x' no ha sido declarada.", errorOf(result).message)
    }

    @Test
    fun `get sobre variable declarada e inicializada devuelve su valor`() {
        val env = valueOf(env().declare("x", DeclaredType.STRING, PrintScriptValue.StringValue("hola"), dummyRange))

        assertEquals(PrintScriptValue.StringValue("hola"), valueOf(env.get("x", dummyRange)))
    }

    private fun env(): Environment = Environment()
}
