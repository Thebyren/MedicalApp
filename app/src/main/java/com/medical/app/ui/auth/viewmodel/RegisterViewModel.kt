package com.medical.app.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Usuario
import com.medical.app.data.entities.enums.TipoUsuario
import com.medical.app.data.local.SessionManager
import com.medical.app.data.repository.AuthRepository
import com.medical.app.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
                // 1. Verificar si el correo ya está registrado
                if (authRepository.existeEmail(email)) {
                    _registrationState.value = Result.Error(Exception("El correo electrónico ya está registrado."))
                    return@launch
                }

                // 2. Crear el objeto Usuario
                val newUser = Usuario(
                    nombreCompleto = fullName,
                    email = email,
                    passwordHash = "", // El repositorio se encarga de esto
                    salt = null,      // El repositorio se encarga de esto
                    tipoUsuario = userType
                )

                // 3. Registrar el nuevo usuario y recolectar el resultado del Flow
                val result = authRepository.registrar(newUser, password).first()

                _registrationState.value = when (result) {
                    is Result.Success -> Result.Success(Unit)
                    is Result.Error -> result // Re-propagate the error
                    else -> Result.Error(Exception("Estado inesperado durante el registro."))
                }

            } catch (e: Exception) {
                _registrationState.value = Result.Error(e)
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
