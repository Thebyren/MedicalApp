package com.medical.app.util.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase de utilidad para manejar las preferencias de la aplicación de manera segura.
 * Proporciona métodos para guardar y recuperar valores de las preferencias compartidas.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("medical_app_prefs", Context.MODE_PRIVATE)

    // Keys para las preferencias
    private object Keys {
        const val IS_FIRST_RUN = "is_first_run"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val DARK_THEME_ENABLED = "dark_theme_enabled"
        const val LANGUAGE = "app_language"
    }

    /**
     * Indica si es la primera vez que se ejecuta la aplicación.
     */
    var isFirstRun: Boolean
        get() = sharedPreferences.getBoolean(Keys.IS_FIRST_RUN, true)
        set(value) = sharedPreferences.edit { putBoolean(Keys.IS_FIRST_RUN, value) }

    /**
     * Indica si las notificaciones están habilitadas.
     */
    var areNotificationsEnabled: Boolean
        get() = sharedPreferences.getBoolean(Keys.NOTIFICATIONS_ENABLED, true)
        set(value) = sharedPreferences.edit { putBoolean(Keys.NOTIFICATIONS_ENABLED, value) }

    /**
     * Indica si el tema oscuro está habilitado.
     */
    var isDarkThemeEnabled: Boolean
        get() = sharedPreferences.getBoolean(Keys.DARK_THEME_ENABLED, false)
        set(value) = sharedPreferences.edit { putBoolean(Keys.DARK_THEME_ENABLED, value) }

    /**
     * Idioma de la aplicación.
     */
    var appLanguage: String
        get() = sharedPreferences.getString(Keys.LANGUAGE, "es") ?: "es"
        set(value) = sharedPreferences.edit { putString(Keys.LANGUAGE, value) }

    /**
     * Limpia todas las preferencias guardadas.
     */
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Elimina una preferencia específica.
     * @param key Clave de la preferencia a eliminar
     */
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    companion object {
        // Preferencias personalizadas
        private const val PREF_USER_LOGGED_IN = "user_logged_in"
        private const val PREF_USER_ID = "user_id"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_USER_NAME = "user_name"
        private const val PREF_USER_TYPE = "user_type"
        private const val PREF_ACCESS_TOKEN = "access_token"
        private const val PREF_REFRESH_TOKEN = "refresh_token"
        private const val PREF_TOKEN_EXPIRATION = "token_expiration"
    }

    // Métodos específicos para la autenticación

    /**
     * Guarda la información del usuario autenticado.
     */
    fun saveUserInfo(
        userId: Int,
        email: String,
        name: String,
        userType: String,
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        sharedPreferences.edit {
            putBoolean(PREF_USER_LOGGED_IN, true)
            putInt(PREF_USER_ID, userId)
            putString(PREF_USER_EMAIL, email)
            putString(PREF_USER_NAME, name)
            putString(PREF_USER_TYPE, userType)
            putString(PREF_ACCESS_TOKEN, accessToken)
            putString(PREF_REFRESH_TOKEN, refreshToken)
            putLong(PREF_TOKEN_EXPIRATION, System.currentTimeMillis() + expiresIn * 1000)
        }
    }

    /**
     * Elimina la información del usuario al cerrar sesión.
     */
    fun clearUserInfo() {
        sharedPreferences.edit {
            remove(PREF_USER_LOGGED_IN)
            remove(PREF_USER_ID)
            remove(PREF_USER_EMAIL)
            remove(PREF_USER_NAME)
            remove(PREF_USER_TYPE)
            remove(PREF_ACCESS_TOKEN)
            remove(PREF_REFRESH_TOKEN)
            remove(PREF_TOKEN_EXPIRATION)
        }
    }

    /**
     * Verifica si hay un usuario con sesión activa.
     */
    val isUserLoggedIn: Boolean
        get() = sharedPreferences.getBoolean(PREF_USER_LOGGED_IN, false) && 
                sharedPreferences.contains(PREF_ACCESS_TOKEN) &&
                !isTokenExpired

    /**
     * Verifica si el token de acceso ha expirado.
     */
    private val isTokenExpired: Boolean
        get() {
            val expirationTime = sharedPreferences.getLong(PREF_TOKEN_EXPIRATION, 0L)
            return expirationTime < System.currentTimeMillis()
        }

    /**
     * Obtiene el ID del usuario autenticado.
     */
    val userId: Int
        get() = sharedPreferences.getInt(PREF_USER_ID, -1)

    /**
     * Obtiene el token de acceso del usuario autenticado.
     */
    val accessToken: String?
        get() = if (isUserLoggedIn) {
            sharedPreferences.getString(PREF_ACCESS_TOKEN, null)
        } else {
            null
        }
}
