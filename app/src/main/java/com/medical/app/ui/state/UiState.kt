package com.medical.app.ui.state

import androidx.annotation.StringRes
import com.medical.app.data.remote.api.ApiResponse
import com.medical.app.util.AppException
import com.medical.app.util.Resource
import com.medical.app.util.UiMessage

/**
 * Clase sellada que representa el estado de la interfaz de usuario
 */
sealed class UiState<out T> {
    /**
     * Estado inicial, cuando la pantalla se carga por primera vez
     */
    object Initial : UiState<Nothing>()

    /**
     * Estado de carga
     */
    object Loading : UiState<Nothing>()

    /**
     * Estado de éxito con datos
     */
    data class Success<out T>(val data: T) : UiState<T>()

    /**
     * Estado de error con un mensaje
     */
    data class Error(
        val message: UiMessage,
        val code: Int? = null,
        val retryAction: (() -> Unit)? = null
    ) : UiState<Nothing>() {
        constructor(
            @StringRes resId: Int,
            code: Int? = null,
            retryAction: (() -> Unit)? = null
        ) : this(UiMessage.ResourceMessage(resId), code, retryAction)

        constructor(
            message: String,
            code: Int? = null,
            retryAction: (() -> Unit)? = null
        ) : this(UiMessage.StringMessage(message), code, retryAction)
    }

    /**
     * Estado vacío (cuando no hay datos para mostrar)
     */
    data class Empty(
        val message: UiMessage? = null,
        val action: (() -> Unit)? = null
    ) : UiState<Nothing>() {
        constructor(
            @StringRes resId: Int,
            action: (() -> Unit)? = null
        ) : this(UiMessage.ResourceMessage(resId), action)

        constructor(
            message: String,
            action: (() -> Unit)? = null
        ) : this(UiMessage.StringMessage(message), action)
    }

    /**
     * Obtiene los datos si el estado es Success, o nulo en caso contrario
     */
    fun getDataOrNull(): T? = if (this is Success) data else null

    /**
     * Obtiene el mensaje de error si el estado es Error, o nulo en caso contrario
     */
    fun getErrorMessageOrNull(): UiMessage? = if (this is Error) message else null

    /**
     * Verifica si el estado es de carga
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Verifica si el estado es de éxito
     */
    fun isSuccess(): Boolean = this is Success<*>

    /**
     * Verifica si el estado es de error
     */
    fun isError(): Boolean = this is Error

    /**
     * Verifica si el estado es vacío
     */
    fun isEmpty(): Boolean = this is Empty

    /**
     * Ejecuta una acción si el estado es Success
     */
    fun onSuccess(action: (T) -> Unit): UiState<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    /**
     * Ejecuta una acción si el estado es Error
     */
    fun onError(action: (UiMessage, Int?) -> Unit): UiState<T> {
        if (this is Error) {
            action(message, code)
        }
        return this
    }

    /**
     * Ejecuta una acción si el estado es Empty
     */
    fun onEmpty(action: (UiMessage?) -> Unit): UiState<T> {
        if (this is Empty) {
            action(message)
        }
        return this
    }

    /**
     * Transforma los datos si el estado es Success
     */
    fun <R> map(transform: (T) -> R): UiState<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(message, code, retryAction)
            is Loading -> Loading
            is Empty -> Empty(message, action)
            is Initial -> Initial
        }
    }

    /**
     * Transforma los datos si el estado es Success (versión suspendida)
     */
    suspend fun <R> mapSuspend(transform: suspend (T) -> R): UiState<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(message, code, retryAction)
            is Loading -> Loading
            is Empty -> Empty(message, action)
            is Initial -> Initial
        }
    }

    companion object {
        /**
         * Crea un estado inicial
         */
        fun <T> initial(): UiState<T> = Initial

        /**
         * Crea un estado de carga
         */
        fun <T> loading(): UiState<T> = Loading

        /**
         * Crea un estado de éxito con los datos proporcionados
         */
        fun <T> success(data: T): UiState<T> = Success(data)

        /**
         * Crea un estado de error con un mensaje
         */
        fun <T> error(message: String, code: Int? = null, retryAction: (() -> Unit)? = null): UiState<T> =
            Error(message, code, retryAction)

        /**
         * Crea un estado de error con un ID de recurso de string
         */
        fun <T> error(@StringRes resId: Int, code: Int? = null, retryAction: (() -> Unit)? = null): UiState<T> =
            Error(resId, code, retryAction)

        /**
         * Crea un estado de error con un mensaje formateado
         */
        fun <T> error(@StringRes resId: Int, vararg formatArgs: Any, code: Int? = null, retryAction: (() -> Unit)? = null): UiState<T> {
            return Error(UiMessage.FormattedMessage(resId, formatArgs), code, retryAction)
        }

        /**
         * Crea un estado de error a partir de una excepción
         */
        fun <T> error(throwable: Throwable, code: Int? = null, retryAction: (() -> Unit)? = null): UiState<T> {
            return when (throwable) {
                is AppException -> Error(
                    UiMessage.StringMessage(throwable.message ?: "Error desconocido"),
                    code,
                    retryAction
                )
                else -> Error(
                    UiMessage.StringMessage(throwable.message ?: "Error desconocido"),
                    code,
                    retryAction
                )
            }
        }

        /**
         * Crea un estado de error con un UiMessage
         */
        fun <T> error(message: UiMessage, code: Int? = null, retryAction: (() -> Unit)? = null): UiState<T> =
            Error(message, code, retryAction)

        /**
         * Crea un estado vacío con un mensaje opcional
         */
        fun <T> empty(message: UiMessage? = null, action: (() -> Unit)? = null): UiState<T> =
            Empty(message, action)

        /**
         * Crea un estado vacío con un ID de recurso de string
         */
        fun <T> empty(@StringRes resId: Int, action: (() -> Unit)? = null): UiState<T> =
            Empty(resId, action)

        /**
         * Crea un estado vacío con un mensaje
         */
        fun <T> empty(message: String, action: (() -> Unit)? = null): UiState<T> =
            Empty(message, action)
    }
}

/**
 * Función de extensión para convertir un Resource en un UiState
 */
fun <T> Resource<T>.toUiState(): UiState<T> {
    return when (this) {
        is Resource.Success -> UiState.success(data)
        is Resource.Error -> UiState.error(message, code)
        is Resource.Loading -> UiState.loading()
    }
}

/**
 * Función de extensión para convertir un Result en un UiState
 */
fun <T> Result<T>.toUiState(): UiState<T> {
    return fold(
        onSuccess = { UiState.success(it) },
        onFailure = { UiState.error(it) }
    )
}

/**
 * Función de extensión para convertir un ApiResponse en un UiState
 */
fun <T> ApiResponse<T>.toUiState(): UiState<T> {
    return when (this) {
        is ApiResponse.Success -> UiState.success(data)
        is ApiResponse.Error -> UiState.error(message)
    }
}
