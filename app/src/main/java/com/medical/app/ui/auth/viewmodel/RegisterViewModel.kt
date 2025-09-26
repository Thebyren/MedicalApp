package com.medical.app.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Usuario
import com.medical.app.data.entities.enums.TipoUsuario
import com.medical.app.data.local.SessionManager
import com.medical.app.data.repository.AuthRepository
import com.medical.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejar la lógica de registro de usuarios.
 * Se encarga de validar los datos y registrar nuevos usuarios en el sistema.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _registrationState = MutableStateFlow<Result<Unit>>(Result.Idle)
    val registrationState: StateFlow<Result<Unit>> = _registrationState

    /**
     * Registra un nuevo usuario en el sistema.
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario (sin encriptar)
     * @param fullName Nombre completo del usuario
     * @param userType Tipo de usuario (Médico o Paciente)
     */
    fun register(email: String, password: String, fullName: String, userType: TipoUsuario) {
        viewModelScope.launch {
            _registrationState.value = Result.Loading

            try {
                // Verificar si el correo ya está registrado
                val emailExists = authRepository.existeEmail(email)
                
                if (emailExists) {
                    _registrationState.value = Result.Error("El correo electrónico ya está registrado")
                    return@launch
                }
                
                // Registrar el nuevo usuario
                val result = authRepository.registrarUsuario(email, password, fullName, userType)
                
                when (result) {
                    is Result.Success -> {
                        _registrationState.value = Result.Success(Unit)
                    }
                    is Result.Error -> {
                        _registrationState.value = Result.Error(
                            result.message ?: "Error en el registro"
                        )
                    }
                    else -> {
                        _registrationState.value = Result.Error("Error desconocido")
                    }
                }
            } catch (e: Exception) {
                _registrationState.value = Result.Error(
                    e.message ?: "Error al intentar registrarse"
                )
            }
        }
    }

    /**
     * Restablece el estado del ViewModel a su valor inicial.
     */
    fun resetState() {
        _registrationState.value = Result.Idle
    }
}
