package com.medical.app.util

import com.medical.app.data.entities.Usuario
import com.medical.app.data.local.SessionManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Administrador de autenticación que proporciona una capa de abstracción
 * sobre el [SessionManager] para manejar el estado de autenticación.
 */
@Singleton
class AuthManager @Inject constructor(
    private val sessionManager: SessionManager
) {
    /**
     * Flujo que emite el estado actual de autenticación.
     */
    val authState: Flow<AuthState> = sessionManager.authState

    /**
     * Obtiene el usuario actualmente autenticado, si existe.
     * @return El usuario autenticado o null si no hay sesión activa
     */
    suspend fun getCurrentUser(): Usuario? {
        return sessionManager.getCurrentUser()
    }

    /**
     * Inicia sesión con el usuario proporcionado.
     * @param user Usuario que inicia sesión
     * @param authToken Token de autenticación (opcional)
     */
    fun login(user: Usuario, authToken: String = "") {
        sessionManager.loginUser(user, authToken)
    }

    /**
     * Cierra la sesión del usuario actual.
     */
    fun logout() {
        sessionManager.logoutUser()
    }

    /**
     * Verifica si hay un usuario autenticado.
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    /**
     * Obtiene el token de autenticación del usuario actual.
     * @return El token de autenticación o null si no hay sesión activa
     */
    fun getAuthToken(): String? {
        return sessionManager.getAuthToken()
    }
}
