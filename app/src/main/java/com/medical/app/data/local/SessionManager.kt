package com.medical.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.medical.app.data.entities.Usuario
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Administrador de sesión que maneja el estado de autenticación del usuario.
 * Utiliza SharedPreferences encriptado para almacenar la información sensible.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_JSON = "user_json"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    init {
        // Verificar si hay una sesión activa al inicializar
        if (isLoggedIn()) {
            _authState.value = AuthState.Authenticated(getCurrentUser()!!)
        }
    }

    /**
     * Inicia la sesión del usuario y guarda sus datos.
     */
    fun loginUser(user: Usuario, authToken: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_USER_JSON, gson.toJson(user))
            .putString(KEY_AUTH_TOKEN, authToken)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()

        _authState.value = AuthState.Authenticated(user)
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logoutUser() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_JSON)
            .remove(KEY_AUTH_TOKEN)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()

        _authState.value = AuthState.Unauthenticated
    }

    /**
     * Obtiene el token de autenticación del usuario actual.
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Verifica si hay un usuario con sesión activa.
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getCurrentUser() != null
    }

    /**
     * Obtiene el usuario actual con la sesión activa.
     */
    fun getCurrentUser(): Usuario? {
        val userJson = prefs.getString(KEY_USER_JSON, null) ?: return null
        return try {
            gson.fromJson(userJson, Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualiza la información del usuario en la sesión.
     */
    fun updateUser(user: Usuario) {
        prefs.edit()
            .putString(KEY_USER_JSON, gson.toJson(user))
            .apply()
        
        if (isLoggedIn()) {
            _authState.value = AuthState.Authenticated(user)
        }
    }
}

/**
 * Estados posibles de autenticación.
 */
sealed class AuthState {
    object Unauthenticated : AuthState()
    data class Authenticated(val user: Usuario) : AuthState()
}
