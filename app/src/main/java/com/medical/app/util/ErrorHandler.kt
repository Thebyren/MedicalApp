package com.medical.app.util

import android.content.Context
import androidx.annotation.StringRes
import com.medical.app.R
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import com.medical.app.util.AppException

@Singleton
class ErrorHandler @Inject constructor(
    private val context: Context
) {

    /**
     * Procesa una excepción y devuelve un mensaje de error amigable
     */
    fun getErrorMessage(throwable: Throwable?): String {
        return when (throwable) {
            is SocketTimeoutException -> context.getString(R.string.error_timeout)
            is UnknownHostException -> context.getString(R.string.error_no_internet)
            is IOException -> context.getString(R.string.error_network)
            is HttpException -> handleHttpError(throwable)
            is ValidationException -> throwable.errors.values.joinToString("\n")
            is AppException -> throwable.message ?: context.getString(R.string.error_generic)
            else -> context.getString(R.string.error_unexpected)
        }
    }

    /**
     * Maneja errores HTTP específicos
     */
    private fun handleHttpError(exception: HttpException): String {
        return when (exception.code()) {
            400 -> context.getString(R.string.error_bad_request)
            401 -> context.getString(R.string.error_unauthorized)
            403 -> context.getString(R.string.error_forbidden)
            404 -> context.getString(R.string.error_not_found)
            408 -> context.getString(R.string.error_timeout)
            409 -> context.getString(R.string.error_conflict)
            500 -> context.getString(R.string.error_server)
            502 -> context.getString(R.string.error_bad_gateway)
            503 -> context.getString(R.string.error_service_unavailable)
            504 -> context.getString(R.string.error_gateway_timeout)
            else -> context.getString(R.string.error_http, exception.code())
        }
    }

    /**
     * Obtiene un mensaje de error desde un recurso de strings
     */
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    /**
     * Obtiene un mensaje de error formateado con argumentos
     */
    fun getString(@StringRes resId: Int, vararg args: Any): String {
        return context.getString(resId, *args)
    }
}




/**
 * Excepción para errores de validación
 */
class ValidationException(
    val errors: Map<String, String>,
    cause: Throwable? = null
) : AppException("Error de validación", cause)

/**
 * Excepción para errores de base de datos
 */
class DatabaseException(
    message: String = "Error en la base de datos",
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Excepción para errores de red
 */
class NetworkException(
    message: String = "Error de red",
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Excepción para errores de autenticación
 */
class AuthException(
    message: String = "Error de autenticación",
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Excepción para recursos no encontrados
 */
class NotFoundException(
    message: String = "Recurso no encontrado",
    cause: Throwable? = null
) : AppException(message, cause)

/**
 * Extensión para lanzar una excepción de validación con un solo error
 */
fun validationError(field: String, message: String): Nothing {
    throw ValidationException(mapOf(field to message))
}

/**
 * Extensión para lanzar una excepción de validación con múltiples errores
 */
fun validationErrors(errors: Map<String, String>): Nothing {
    throw ValidationException(errors)
}
