package org.printscript.common

sealed interface Result<out T, out E> {
    data class Success<out T>(val value: T) : Result<T, Nothing>

    data class Failure<out E>(val error: E) : Result<Nothing, E>
}

fun <T, E> Result<T, E>.getOrNull(): T? =
    when (this) {
        is Result.Success -> value
        is Result.Failure -> null
    }

fun <T, E> Result<T, E>.errorOrNull(): E? =
    when (this) {
        is Result.Success -> null
        is Result.Failure -> error
    }

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

inline fun <T, E, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> =
    when (this) {
        is Result.Success -> Result.Success(transform(value))
        is Result.Failure -> this
    }

inline fun <T, E, R> Result<T, E>.flatMap(transform: (T) -> Result<R, E>): Result<R, E> =
    when (this) {
        is Result.Success -> transform(value)
        is Result.Failure -> this
    }

inline fun <T, E, F> Result<T, E>.mapError(transform: (E) -> F): Result<T, F> =
    when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Failure(transform(error))
    }

fun <T, E> Result<T, E>.getOrElse(default: T): T =
    when (this) {
        is Result.Success -> value
        is Result.Failure -> default
    }
