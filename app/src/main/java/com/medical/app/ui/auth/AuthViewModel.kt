package com.medical.app.ui.auth

import androidx.lifecycle.viewModelScope
import com.medical.app.core.BaseViewModel
import com.medical.app.data.entities.enums.TipoUsuario
import com.medical.app.data.repository.AuthRepository
import com.medical.app.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles para la pantalla de autenticación.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: Int) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Eventos que pueden ocurrir en la pantalla de autenticación.
 */
sealed class AuthEvent {
    data class NavigateToHome(val userId: Int) : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

/**
 * ViewModel para la autenticación de usuarios.
 * Maneja el inicio de sesión y registro de usuarios.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthState, AuthEvent>() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _tipoUsuario = MutableStateFlow(TipoUsuario.PACIENTE)
    val tipoUsuario: StateFlow<TipoUsuario> = _tipoUsuario

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setTipoUsuario(tipoUsuario: TipoUsuario) {
        _tipoUsuario.value = tipoUsuario
    }

    fun toggleAuthMode() {
        _isLoginMode.value = !_isLoginMode.value
    }

    /**
     * Maneja el proceso de autenticación (login o registro).
     */
    fun authenticate() {
        viewModelScope.launch {
            setState(AuthState.Loading)
            
            val result = if (_isLoginMode.value) {
                login()
            } else {
                register()
            }
            
            when (result) {
                is Result.Success -> {
                    setState(AuthState.Success(result.data))
                    postEvent(AuthEvent.NavigateToHome(result.data))
                }
                is Result.Error -> {
                    val errorMessage = result.exception.message ?: "Error desconocido"
                    setState(AuthState.Error(errorMessage))
                    postEvent(AuthEvent.ShowError(errorMessage))
                }
                else -> {}
            }
        }
    }

    private suspend fun login(): Result<Int> {
        return when (val result = authRepository.login(_email.value, _password.value)) {
            is Result.Success -> {
                val userId = result.data.id
                Result.Success(userId)
            }
            is Result.Error -> result
            else -> Result.Error(Exception("Estado inesperado"))
        }
    }

    private suspend fun register(): Result<Int> {
        return when (val result = authRepository.registrarUsuario(
            _email.value,
            _password.value,
            _tipoUsuario.value
        )) {
            is Result.Success -> {
                val userId = result.data.toInt()
                // Después de registrar, hacer login automáticamente
                login()
            }
            is Result.Error -> result
            else -> Result.Error(Exception("Estado inesperado"))
        }
    }

    /**
     * Verifica si el formulario actual es válido.
     */
    fun isFormValid(): Boolean {
        return _email.value.isNotBlank() && 
               _password.value.isNotBlank() && 
               (_isLoginMode.value || _tipoUsuario.value != null)
    }
}
