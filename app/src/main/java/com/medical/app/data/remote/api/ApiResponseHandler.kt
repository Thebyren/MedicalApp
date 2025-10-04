package com.medical.app.data.remote.api

import com.medical.app.util.AppException
import com.medical.app.util.ErrorHandler
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

/**
 * Manejador de respuestas de la API
 */
class ApiResponseHandler @Inject constructor(
    private val errorHandler: ErrorHandler
) {

    /**
     * Maneja una respuesta de la API y devuelve un objeto ApiResponse
     */
    fun <T> handleResponse(response: Response<T>): ApiResponse<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResponse.Success(body)
                } else {
                    ApiResponse.Error("Respuesta vacía del servidor")
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                ApiResponse.Error(errorMsg ?: "Error desconocido")
            }
        } catch (e: Exception) {
            ApiResponse.Error(handleException(e).message ?: "Error desconocido")
        }
    }

    /**
     * Maneja una excepción y devuelve una excepción de aplicación
     */
    fun handleException(throwable: Throwable): AppException {
        return when (throwable) {
            is HttpException -> {
                val errorResponse = throwable.response()
                val errorMsg = try {
                    errorResponse?.errorBody()?.string()
                } catch (e: Exception) {
                    null
                } ?: "Error HTTP ${errorResponse?.code()}: ${errorResponse?.message()}"
                AppException(errorMsg, throwable)
            }
            is IOException -> AppException("Error de red: ${throwable.message}", throwable)
            is AppException -> throwable
            else -> AppException("Error desconocido: ${throwable.message}", throwable)
        }
    }

    /**
     * Ejecuta una operación de API de forma segura y devuelve un objeto ApiResponse
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResponse<T> {
        return try {
            val response = apiCall()
            handleResponse(response)
        } catch (e: Exception) {
            ApiResponse.Error(handleException(e).message ?: "Error desconocido")
        }
    }

    /**
     * Ejecuta una operación de API de forma segura y devuelve un Result
     */
    suspend fun <T> safeApiCallResult(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(AppException("Respuesta vacía del servidor", null))
                }
            } else {
                Result.failure(handleException(HttpException(response)))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
}

/**
 * Clase sellada que representa una respuesta de la API
 */
sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>() {
        override fun isSuccess(): Boolean = true
        override fun getDataOrNull(): T? = data
        override fun getErrorOrNull(): String? = null
    }

    data class Error(val message: String) : ApiResponse<Nothing>() {
        override fun isSuccess(): Boolean = false
        override fun getDataOrNull(): Nothing? = null
        override fun getErrorOrNull(): String = message
    }

    abstract fun isSuccess(): Boolean
    abstract fun getDataOrNull(): T?
    abstract fun getErrorOrNull(): String?

    /**
     * Ejecuta una acción si la respuesta es exitosa
     */
    fun onSuccess(action: (T) -> Unit): ApiResponse<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    /**
     * Ejecuta una acción si hay un error
     */
    fun onError(action: (String) -> Unit): ApiResponse<T> {
        if (this is Error) {
            action(message)
        }
        return this
    }

    /**
     * Transforma el contenido exitoso usando la función proporcionada
     */
    fun <R> map(transform: (T) -> R): ApiResponse<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
        }
    }
}
