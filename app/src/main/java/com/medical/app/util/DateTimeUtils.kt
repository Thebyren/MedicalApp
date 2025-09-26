package com.medical.app.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidades para el manejo de fechas y horas.
 */
object DateTimeUtils {
    
    // Formatos de fecha y hora
    private const val DATE_FORMAT = "dd/MM/yyyy"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm"
    private const val DATE_TIME_FORMAT_FILE = "yyyyMMdd_HHmmss"
    private const val DATE_FORMAT_API = "yyyy-MM-dd"
    private const val DATE_TIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss"
    
    // Instancia de SimpleDateFormat para evitar crear múltiples instancias
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    private val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
    private val dateTimeFileFormat = SimpleDateFormat(DATE_TIME_FORMAT_FILE, Locale.getDefault())
    private val dateFormatApi = SimpleDateFormat(DATE_FORMAT_API, Locale.getDefault())
    private val dateTimeFormatApi = SimpleDateFormat(DATE_TIME_FORMAT_API, Locale.getDefault())
    
    /**
     * Formatea una fecha en formato de texto legible.
     * @param date Fecha a formatear
     * @return Fecha formateada como String
     */
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
    
    /**
     * Formatea una hora en formato de texto legible.
     * @param date Hora a formatear
     * @return Hora formateada como String
     */
    fun formatTime(date: Date): String {
        return timeFormat.format(date)
    }
    
    /**
     * Formatea una fecha y hora en formato de texto legible.
     * @param date Fecha y hora a formatear
     * @return Fecha y hora formateada como String
     */
    fun formatDateTime(date: Date): String {
        return dateTimeFormat.format(date)
    }
    
    /**
     * Formatea una fecha para usar en nombres de archivo.
     * @param date Fecha a formatear
     * @return Fecha formateada como String para nombres de archivo
     */
    fun formatDateTimeForFile(date: Date): String {
        return dateTimeFileFormat.format(date)
    }
    
    /**
     * Formatea una fecha en el formato esperado por la API.
     * @param date Fecha a formatear
     * @return Fecha formateada como String para la API
     */
    fun formatDateForApi(date: Date): String {
        return dateFormatApi.format(date)
    }
    
    /**
     * Formatea una fecha y hora en el formato esperado por la API.
     * @param date Fecha y hora a formatear
     * @return Fecha y hora formateada como String para la API
     */
    fun formatDateTimeForApi(date: Date): String {
        return dateTimeFormatApi.format(date)
    }
    
    /**
     * Parsea una cadena de fecha en formato API a un objeto Date.
     * @param dateString Cadena de fecha en formato API
     * @return Objeto Date o null si no se pudo parsear
     */
    fun parseDateFromApi(dateString: String): Date? {
        return try {
            dateFormatApi.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parsea una cadena de fecha y hora en formato API a un objeto Date.
     * @param dateTimeString Cadena de fecha y hora en formato API
     * @return Objeto Date o null si no se pudo parsear
     */
    fun parseDateTimeFromApi(dateTimeString: String): Date? {
        return try {
            dateTimeFormatApi.parse(dateTimeString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Obtiene la fecha actual.
     * @return Fecha actual
     */
    fun getCurrentDate(): Date {
        return Date()
    }
    
    /**
     * Obtiene la fecha de mañana.
     * @return Fecha de mañana
     */
    fun getTomorrowDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.time
    }
    
    /**
     * Obtiene la fecha de ayer.
     * @return Fecha de ayer
     */
    fun getYesterdayDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return calendar.time
    }
    
    /**
     * Agrega días a una fecha.
     * @param date Fecha base
     * @param days Número de días a agregar (puede ser negativo)
     * @return Nueva fecha con los días agregados
     */
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }
    
    /**
     * Compara dos fechas ignorando la hora.
     * @param date1 Primera fecha
     * @param date2 Segunda fecha
     * @return 0 si son iguales, negativo si date1 es anterior a date2, positivo si date1 es posterior a date2
     */
    fun compareDates(date1: Date, date2: Date): Int {
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }
        
        calendar1.set(Calendar.HOUR_OF_DAY, 0)
        calendar1.set(Calendar.MINUTE, 0)
        calendar1.set(Calendar.SECOND, 0)
        calendar1.set(Calendar.MILLISECOND, 0)
        
        calendar2.set(Calendar.HOUR_OF_DAY, 0)
        calendar2.set(Calendar.MINUTE, 0)
        calendar2.set(Calendar.SECOND, 0)
        calendar2.set(Calendar.MILLISECOND, 0)
        
        return calendar1.compareTo(calendar2)
    }
    
    /**
     * Verifica si dos fechas son el mismo día.
     * @param date1 Primera fecha
     * @param date2 Segunda fecha
     * @return true si son el mismo día, false en caso contrario
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        return compareDates(date1, date2) == 0
    }
}
