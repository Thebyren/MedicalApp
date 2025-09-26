package com.medical.app.data.remote

import com.medical.app.util.AppException
import com.medical.app.util.NetworkException
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

/**
 * Interceptor para manejar errores de red
 */
class NetworkErrorInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        return try {
            val response = chain.proceed(request)
            
            if (!response.isSuccessful) {
                throw createExceptionFromResponse(response)
            }
            
            response
            
        } catch (e: Exception) {
            throw when (e) {
                is SocketTimeoutException -> NetworkException("Tiempo de espera agotado", e)
                is ConnectException -> NetworkException("No se pudo conectar al servidor", e)
                is UnknownHostException -> NetworkException("No hay conexión a Internet", e)
                is SSLHandshakeException -> NetworkException("Error de seguridad en la conexión", e)
                is IOException -> NetworkException("Error de red: ${e.message}", e)
                is AppException -> e
                else -> NetworkException("Error desconocido: ${e.message}", e)
            }
        }
    }
    
    private fun createExceptionFromResponse(response: Response): AppException {
        return when (response.code) {
            400 -> NetworkException("Solicitud incorrecta (400)")
            401 -> NetworkException("No autorizado (401)")
            403 -> NetworkException("Acceso denegado (403)")
            404 -> NetworkException("Recurso no encontrado (404)")
            408 -> NetworkException("Tiempo de espera agotado (408)")
            409 -> NetworkException("Conflicto (409)")
            500 -> NetworkException("Error interno del servidor (500)")
            502 -> NetworkException("Error de puerta de enlace (502)")
            503 -> NetworkException("Servicio no disponible (503)")
            504 -> NetworkException("Tiempo de espera de la puerta de enlace agotado (504)")
            else -> NetworkException("Error HTTP ${response.code}: ${response.message}")
        }
    }
}

/**
 * Manejador de errores para operaciones de red
 */
object NetworkErrorHandler {

    /**
     * Maneja una excepción de red y devuelve una excepción de aplicación
     */
    fun handleError(throwable: Throwable): AppException {
        return when (throwable) {
            is AppException -> throwable
            is SocketTimeoutException -> NetworkException("Tiempo de espera agotado", throwable)
            is ConnectException -> NetworkException("No se pudo conectar al servidor", throwable)
            is UnknownHostException -> NetworkException("No hay conexión a Internet", throwable)
            is SSLHandshakeException -> NetworkException("Error de seguridad en la conexión", throwable)
            is IOException -> NetworkException("Error de red: ${throwable.message}", throwable)
            else -> NetworkException("Error desconocido: ${throwable.message}", throwable)
        }
    }

    /**
     * Ejecuta una operación de red de forma segura y devuelve un Result
     */
    fun <T> safeExecute(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(handleError(e))
        }
    }

    /**
     * Ejecuta una operación de red suspendida de forma segura y devuelve un Result
     */
    suspend fun <T> safeSuspendExecute(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(handleError(e))
        }
    }
}
