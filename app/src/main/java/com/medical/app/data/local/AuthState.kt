package com.medical.app.data.local

import com.medical.app.data.entities.Usuario

/**
 * Representa los posibles estados de autenticación de la aplicación.
 */
sealed class AuthState {
    /**
     * Estado inicial, no hay información de autenticación.
     */
    object Initial : AuthState()
    
    /**
     * Estado cuando el usuario no está autenticado.
     */
    object Unauthenticated : AuthState()
    
    /**
     * Estado cuando el usuario está autenticado exitosamente.
     * @property user El usuario autenticado
     */
    data class Authenticated(val user: Usuario) : AuthState()
    
    /**
     * Estado cuando ocurre un error durante la autenticación.
     * @property errorMessage El mensaje de error
     */
    data class Error(val errorMessage: String) : AuthState()
    
    /**
     * Estado cuando se está realizando una operación de autenticación.
     */
    object Loading : AuthState()
}
