package com.medical.app.util

import androidx.annotation.StringRes
import com.medical.app.data.remote.api.ApiResponse

/**
 * Clase sellada que representa un recurso que puede estar en diferentes estados:
 * - Cargando (Loading)
 * - Éxito (Success) con datos
 * - Error (Error) con un mensaje
 */
sealed class Resource<out T> {
    /**
     * Estado de carga
     */
    object Loading : Resource<Nothing>()

    /**
     * Estado de éxito con datos
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     * Estado de error con un mensaje
     */
    data class Error(val message: UiMessage, val code: Int? = null) : Resource<Nothing>() {
        constructor(@StringRes resId: Int, code: Int? = null) : this(UiMessage.ResourceMessage(resId), code)
        constructor(message: String, code: Int? = null) : this(UiMessage.StringMessage(message), code)
    }

    /**
     * Obtiene los datos si el estado es Success, o nulo en caso contrario
     */
    fun getOrNull(): T? = if (this is Success) data else null

    /**
     * Obtiene el mensaje de error si el estado es Error, o nulo en caso contrario
     */
    fun getErrorOrNull(): UiMessage? = if (this is Error) message else null

    /**
     * Obtiene el código de error si el estado es Error, o nulo en caso contrario
     */
    fun getErrorCodeOrNull(): Int? = if (this is Error) code else null

    /**
     * Ejecuta una acción si el estado es Success
     */
    fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    /**
     * Ejecuta una acción si el estado es Error
     */
    fun onError(action: (UiMessage, Int?) -> Unit): Resource<T> {
        if (this is Error) {
            action(message, code)
        }
        return this
    }

    /**
     * Transforma los datos si el estado es Success
     */
    fun <R> map(transform: (T) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> Loading
        }
    }

    /**
     * Transforma los datos si el estado es Success (versión suspendida)
     */
    suspend fun <R> mapSuspend(transform: suspend (T) -> R): Resource<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> Loading
        }
    }

    /**
     * Transforma el mensaje de error si el estado es Error
     */
    fun mapError(transform: (UiMessage) -> UiMessage): Resource<T> {
        return when (this) {
            is Success -> this
            is Error -> Error(transform(message), code)
            is Loading -> this
        }
    }

    companion object {
        /**
         * Crea un recurso en estado de carga
         */
        fun <T> loading(): Resource<T> = Loading

        /**
         * Crea un recurso en estado de éxito con los datos proporcionados
         */
        fun <T> success(data: T): Resource<T> = Success(data)

        /**
         * Crea un recurso en estado de error con un mensaje
         */
        fun <T> error(message: String, code: Int? = null): Resource<T> = Error(UiMessage.StringMessage(message), code)

        /**
         * Crea un recurso en estado de error con un ID de recurso de string
         */
        fun <T> error(@StringRes resId: Int, code: Int? = null): Resource<T> = Error(UiMessage.ResourceMessage(resId), code)

        /**
         * Crea un recurso en estado de error con un mensaje formateado
         */
        fun <T> error(@StringRes resId: Int, vararg formatArgs: Any, code: Int? = null): Resource<T> {
            return Error(UiMessage.FormattedMessage(resId, formatArgs), code)
        }

        /**
         * Crea un recurso en estado de error a partir de una excepción
         */
        fun <T> error(throwable: Throwable, code: Int? = null): Resource<T> {
            return when (throwable) {
                is AppException -> Error(UiMessage.StringMessage(throwable.message ?: "Error desconocido"), code)
                else -> Error(UiMessage.StringMessage(throwable.message ?: "Error desconocido"), code)
            }
        }
    }
}

/**
 * Función de extensión para ejecutar una operación que devuelve un Resource
 */
fun <T> resource(block: () -> Resource<T>): Resource<T> {
    return try {
        block()
    } catch (e: Exception) {
        Resource.error(e)
    }
}

/**
 * Función de extensión para ejecutar una operación suspendida que devuelve un Resource
 */
suspend fun <T> resourceSuspend(block: suspend () -> Resource<T>): Resource<T> {
    return try {
        block()
    } catch (e: Exception) {
        Resource.error(e)
    }
}

/**
 * Función de extensión para convertir un Result en un Resource
 */
fun <T> Result<T>.toResource(): Resource<T> {
    return fold(
        onSuccess = { Resource.success(it) },
        onFailure = { Resource.error(it) }
    )
}

/**
 * Función de extensión para convertir un ApiResponse en un Resource
 */
fun <T> ApiResponse<T>.toResource(): Resource<T> {
    return when (this) {
        is ApiResponse.Success -> Resource.success(data)
        is ApiResponse.Error -> Resource.error(message)
    }
}
