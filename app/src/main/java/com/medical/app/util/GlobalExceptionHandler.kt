package com.medical.app.util

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresApi
import com.medical.app.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manejador global de excepciones no capturadas
 */
@Singleton
class GlobalExceptionHandler @Inject constructor(
    private val application: Application,
    private val errorHandler: ErrorHandler
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val crashDir by lazy { File(application.cacheDir, "crashes") }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
        setupExceptionHandlers()
        
        // Crear directorio de crashes si no existe
        if (!crashDir.exists()) {
            crashDir.mkdirs()
        }
    }

    /**
     * Configura manejadores adicionales para excepciones específicas
     */
    private fun setupExceptionHandlers() {
        // Manejar excepciones en el hilo principal
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            while (true) {
                try {
                    android.os.Looper.loop()
                } catch (e: Throwable) {
                    handleException(Thread.currentThread(), e)
                }
            }
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        handleException(thread, throwable)
        
        // Delegar al manejador por defecto (cierra la aplicación)
        defaultHandler?.uncaughtException(thread, throwable)
    }

    /**
     * Maneja una excepción
     */
    fun handleException(thread: Thread, throwable: Throwable) {
        try {
            // Registrar el error en el log
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            
            // Guardar el crash en un archivo
            saveCrashToFile(throwable)
            
            // Notificar al servidor (si es necesario)
            // reportCrashToServer(throwable)
            
            // Mostrar un mensaje de error al usuario (opcional)
            // showErrorToUser(throwable)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling uncaught exception", e)
        }
    }

    /**
     * Guarda el crash en un archivo
     */
    private fun saveCrashToFile(throwable: Throwable) {
        try {
            val timestamp = dateFormat.format(Date())
            val deviceInfo = getDeviceInfo()
            val stackTrace = getStackTrace(throwable)
            
            val crashFile = File(crashDir, "crash_${timestamp}.txt")
            crashFile.writeText("""
                |Timestamp: $timestamp
                |
                |=== Device Info ===
                |${deviceInfo}
                |
                |=== Stack Trace ===
                |$stackTrace
                |
                |=== Cause ===
                |${getRootCause(throwable)}
                |
                |=== Logcat ===
                |${getLogcat()}
                |
            """.trimMargin())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving crash to file", e)
        }
    }

    /**
     * Obtiene información del dispositivo
     */
    private fun getDeviceInfo(): String {
        return """
            |App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
            |Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            |Device: ${Build.MANUFACTURER} ${Build.MODEL}
            |Board: ${Build.BOARD}
            |Brand: ${Build.BRAND}
            |Device: ${Build.DEVICE}
            |Display: ${Build.DISPLAY}
            |Fingerprint: ${Build.FINGERPRINT}
            |Hardware: ${Build.HARDWARE}
            |Host: ${Build.HOST}
            |ID: ${Build.ID}
            |Product: ${Build.PRODUCT}
            |Tags: ${Build.TAGS}
            |Type: ${Build.TYPE}
            |User: ${Build.USER}
        """.trimMargin()
    }

    /**
     * Obtiene el stack trace como una cadena
     */
    private fun getStackTrace(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    /**
     * Obtiene la causa raíz de una excepción
     */
    private fun getRootCause(throwable: Throwable): String {
        var cause = throwable.cause
        var rootCause = throwable
        while (cause != null) {
            rootCause = cause
            cause = cause.cause
        }
        return getStackTrace(rootCause)
    }

    /**
     * Obtiene el logcat
     */
    private fun getLogcat(): String {
        return try {
            val process = Runtime.getRuntime().exec("logcat -d -v threadtime *:E")
            process.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Error getting logcat: ${e.message}"
        }
    }

    /**
     * Obtiene la lista de archivos de crash
     */
    fun getCrashFiles(): List<File> {
        return crashDir.listFiles()?.toList() ?: emptyList()
    }

    /**
     * Elimina todos los archivos de crash
     */
    fun clearCrashFiles() {
        crashDir.listFiles()?.forEach { it.delete() }
    }

    companion object {
        private const val TAG = "GlobalExceptionHandler"
        
        /**
         * Inicializa el manejador global de excepciones
         */
        fun initialize(application: Application) {
            // La inyección de dependencias se encargará de crear la instancia
        }
    }
}

/**
 * Extensión para obtener el manejador global de excepciones
 */
val Context.globalExceptionHandler: GlobalExceptionHandler
    get() = (applicationContext as? HasGlobalExceptionHandler)?.globalExceptionHandler
        ?: throw IllegalStateException("Application must implement HasGlobalExceptionHandler")

/**
 * Interfaz que debe implementar la clase Application
 */
interface HasGlobalExceptionHandler {
    val globalExceptionHandler: GlobalExceptionHandler
}
