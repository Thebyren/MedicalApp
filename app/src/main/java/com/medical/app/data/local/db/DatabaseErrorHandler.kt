package com.medical.app.data.local.db

import android.database.sqlite.SQLiteException
import com.medical.app.util.AppException
import com.medical.app.util.DatabaseException

/**
 * Manejador de errores para operaciones de base de datos Room
 */
object DatabaseErrorHandler {

    /**
     * Maneja una excepción de base de datos y devuelve una excepción de aplicación
     */
    fun handleError(throwable: Throwable): AppException {
        return when (throwable) {
            is SQLiteException -> {
                when (throwable) {
                    // Errores de restricción
                    is android.database.sqlite.SQLiteConstraintException -> {
                        DatabaseException("Error de restricción en la base de datos: ${throwable.message}", throwable)
                    }
                    // Errores de base de datos bloqueada
                    is android.database.sqlite.SQLiteDatabaseLockedException -> {
                        DatabaseException("La base de datos está bloqueada. Intente nuevamente más tarde.", throwable)
                    }
                    // Otros errores de SQLite
                    else -> {
                        DatabaseException("Error de base de datos: ${throwable.message}", throwable)
                    }
                }
            }
            // Si ya es una excepción de la aplicación, la devolvemos tal cual
            is AppException -> throwable
            // Cualquier otra excepción la envolvemos en DatabaseException
            else -> DatabaseException("Error en la base de datos: ${throwable.message}", throwable)
        }
    }

    /**
     * Ejecuta una operación de base de datos y maneja cualquier error que pueda ocurrir
     */
    @Throws(DatabaseException::class)
    fun <T> executeWithErrorHandling(block: () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    /**
     * Ejecuta una operación de base de datos de forma segura y devuelve un Result
     */
    fun <T> safeExecute(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(handleError(e))
        }
    }

    /**
     * Ejecuta una operación de base de datos suspendida de forma segura y devuelve un Result
     */
    suspend fun <T> safeSuspendExecute(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(handleError(e))
        }
    }
}
