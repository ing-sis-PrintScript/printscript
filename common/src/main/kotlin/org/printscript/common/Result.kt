package org.printscript.common

/**
 * Una operación que puede fallar devuelve un Result: o salió bien (Success con el valor),
 * o salió mal (Failure con el error). No hay tercera opción.
 *
 * "sealed" significa que estas dos son las ÚNICAS implementaciones posibles. Gracias a eso,
 * cuando hacés un `when` sobre un Result, el compilador te obliga a cubrir los dos casos.
 * Ahí está la gracia: no te podés olvidar de manejar el error, como sí pasa con las excepciones.
 */
sealed interface Result<out T, out E> {
    data class Success<out T>(val value: T) : Result<T, Nothing>
    data class Failure<out E>(val error: E) : Result<Nothing, E>
}

/** Devuelve el valor, o null si fue Failure. */
fun <T, E> Result<T, E>.getOrNull(): T? = when (this) {
    is Result.Success -> value
    is Result.Failure -> null
}

/** Devuelve el error, o null si fue Success. */
fun <T, E> Result<T, E>.errorOrNull(): E? = when (this) {
    is Result.Success -> null
    is Result.Failure -> error
}

/**
 * Junta una secuencia de resultados en un único Result con la lista completa.
 * Corta en el primer Failure y no sigue consumiendo la secuencia.
 *
 * Sirve para los tests y para el modo "Validation" del CLI, donde sí querés
 * el resultado completo. El resto del sistema consume la secuencia de a un token.
 */
fun <T, E> Sequence<Result<T, E>>.collectResults(): Result<List<T>, E> {
    val items = mutableListOf<T>()
    for (result in this) {
        when (result) {
            is Result.Success -> items.add(result.value)
            is Result.Failure -> return result
        }
    }
    return Result.Success(items)
}
