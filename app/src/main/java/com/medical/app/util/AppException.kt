package com.medical.app.util

/**
 * Excepción personalizada para la aplicación.
 */
open class AppException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
