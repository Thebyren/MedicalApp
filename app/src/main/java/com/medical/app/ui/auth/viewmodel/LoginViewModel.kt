package com.medical.app.ui.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * ViewModel para manejar la lógica de inicio de sesión.
 * Se encarga de validar credenciales y gestionar el estado de autenticación.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
        private const val TIMEOUT_MILLIS = 30000L // 30 segundos
    }

    private val _loginState = MutableStateFlow<Result<Unit>>(Result.Idle)
    val loginState: StateFlow<Result<Unit>> = _loginState

    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas.
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario (sin encriptar)
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Iniciando login para: $email")
                _loginState.value = Result.Loading

                // Recolectar TODOS los valores del Flow
                try {
                    withTimeout(TIMEOUT_MILLIS) {
                        authRepository.login(email, password)
                            .catch { e ->
                                Log.e(TAG, "Error en el Flow de login", e)
                                emit(Result.Error(e as Exception))
                            }
                            .collect { result ->
                                Log.d(TAG, "Resultado del login: $result")

                                when (result) {
                                    is Result.Success -> {
                                        Log.d(TAG, "Login exitoso para: $email")
                                        // Guardar la sesión
                                        val user = result.data
                                        sessionManager.loginUser(user, "auth_token_placeholder")
                                        _loginState.value = Result.Success(Unit)
                                    }
                                    is Result.Error -> {
                                        Log.e(TAG, "Error en login: ${result.exception.message}")
                                        _loginState.value = result
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
                    Log.e(TAG, "Timeout en login", e)
                    _loginState.value = Result.Error(Exception("La operación tardó demasiado. Por favor intenta de nuevo."))
                } catch (e: Exception) {
                    Log.e(TAG, "Error inesperado en login", e)
                    _loginState.value = Result.Error(Exception("Error inesperado: ${e.message}"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error general en login()", e)
                _loginState.value = Result.Error(Exception("Error en el login: ${e.message ?: e.toString()}"))
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