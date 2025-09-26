package com.medical.app.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.local.SessionManager
import com.medical.app.data.repository.AuthRepository
import com.medical.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejar la lógica de inicio de sesión.
 * Se encarga de validar credenciales y gestionar el estado de autenticación.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<Result<Unit>>(Result.Idle)
    val loginState: StateFlow<Result<Unit>> = _loginState
    
    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas.
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario (sin encriptar)
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Result.Loading
            
            try {
                val result = authRepository.login(email, password)
                
                when (result) {
                    is Result.Success -> {
                        // Iniciar sesión exitosamente
                        result.data?.let { user ->
                            // Guardar la sesión
                            sessionManager.loginUser(user, "auth_token_placeholder")
                            _loginState.value = Result.Success(Unit)
                        } ?: run {
                            _loginState.value = Result.Error("Error al procesar los datos del usuario")
                        }
                    }
                    is Result.Error -> {
                        _loginState.value = Result.Error(
                            result.message ?: "Error de autenticación"
                        )
                    }
                    else -> {
                        _loginState.value = Result.Error("Error desconocido")
                    }
                }
            } catch (e: Exception) {
                _loginState.value = Result.Error(
                    e.message ?: "Error al intentar iniciar sesión"
                )
            }
        }
    }
    
    /**
     * Restablece el estado del ViewModel a su valor inicial.
     */
    fun resetState() {
        _loginState.value = Result.Idle
    }
}
