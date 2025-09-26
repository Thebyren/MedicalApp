package com.medical.app.utils

/**
 * Clase sellada para representar el resultado de una operación.
 * Puede ser de tipo Success (éxito) o Error (fallo).
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>() {
        override fun toString() = "Success[data=$data]"
    }
    
    data class Error(val exception: Exception) : Result<Nothing>() {
        override fun toString() = "Error[exception=$exception]"
    }
    
    object Loading : Result<Nothing>() {
        override fun toString() = "Loading"
    }
    
    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun error(exception: Exception): Result<Nothing> = Error(exception)
        fun loading(): Result<Nothing> = Loading
    }
    
    /**
     * Ejecuta la función [onSuccess] si el resultado es un éxito.
     */
    inline fun onSuccess(block: (T) -> Unit): Result<T> {
        if (this is Success) block(data)
        return this
    }
    
    /**
     * Ejecuta la función [onError] si el resultado es un error.
     */
    inline fun onError(block: (Exception) -> Unit): Result<T> {
        if (this is Error) block(exception)
        return this
    }
    
    /**
     * Ejecuta la función [onLoading] si el resultado es de carga.
     */
    inline fun onLoading(block: () -> Unit): Result<T> {
        if (this is Loading) block()
        return this
    }
}
