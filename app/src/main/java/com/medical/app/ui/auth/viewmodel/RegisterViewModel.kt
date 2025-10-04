package com.medical.app.ui.auth.viewmodel

import android.util.Log
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
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

    companion object {
        private const val TAG = "RegisterViewModel"
        private const val TIMEOUT_MILLIS = 30000L // 30 segundos
    }

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
            try {
                Log.d(TAG, "Iniciando registro para: $email")
                _registrationState.value = Result.Loading

                // 1. Verificar si el correo ya está registrado
                val emailExists = try {
                    withTimeout(TIMEOUT_MILLIS) {
                        authRepository.existeEmail(email)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al verificar email", e)
                    _registrationState.value = Result.Error(Exception("Error al verificar el correo: ${e.message}"))
                    return@launch
                }

                if (emailExists) {
                    Log.w(TAG, "El email ya existe: $email")
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

                Log.d(TAG, "Llamando a authRepository.registrar()")

                // 3. Registrar el nuevo usuario - Recolectar TODOS los valores del Flow
                try {
                    withTimeout(TIMEOUT_MILLIS) {
                        authRepository.registrar(newUser, password)
                            .catch { e ->
                                Log.e(TAG, "Error en el Flow de registro", e)
                                emit(Result.Error(e as Exception))
                            }
                            .collect { result ->
                                Log.d(TAG, "Resultado del registro: $result")

                                // Solo actualizar el estado si NO es Loading
                                // (ya lo establecimos arriba)
                                when (result) {
                                    is Result.Success -> {
                                        Log.d(TAG, "Registro exitoso")
                                        _registrationState.value = Result.Success(Unit)
                                    }
                                    is Result.Error -> {
                                        Log.e(TAG, "Error en el registro: ${result.exception.message}")
                                        _registrationState.value = result
                                    }
                                    is Result.Loading -> {
                                        // Ignorar, ya establecimos Loading arriba
                                        Log.d(TAG, "Estado Loading (ignorado)")
                                    }
                                    is Result.Idle -> {
                                        // No debería llegar aquí
                                        Log.w(TAG, "Estado Idle inesperado")
                                    }
                                }
                            }
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    Log.e(TAG, "Timeout en registro", e)
                    _registrationState.value = Result.Error(Exception("La operación tardó demasiado. Por favor intenta de nuevo."))
                } catch (e: Exception) {
                    Log.e(TAG, "Error inesperado en registro", e)
                    _registrationState.value = Result.Error(Exception("Error inesperado: ${e.message}"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error general en register()", e)
                _registrationState.value = Result.Error(Exception("Error en el registro: ${e.message ?: e.toString()}"))
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