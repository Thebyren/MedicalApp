package com.medical.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.util.AppException
import com.medical.app.util.ErrorHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado base para las pantallas de la aplicación
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String, val retryAction: (() -> Unit)? = null) : UiState<Nothing>() 
    object Empty : UiState<Nothing>()
}

/**
 * ViewModel base que maneja estados de carga y errores
 */
abstract class BaseViewModel : ViewModel() {

    @Inject
    lateinit var errorHandler: ErrorHandler

    private val _uiState = MutableStateFlow<UiState<*>>(UiState.Empty)
    val uiState: StateFlow<UiState<*>> = _uiState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /**
     * Manejador de excepciones para corrutinas
     */
    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    /**
     * Maneja un error y actualiza el estado correspondiente
     */
    protected fun handleError(throwable: Throwable) {
        val errorMessage = when (throwable) {
            is AppException -> throwable.message ?: errorHandler.getString(R.string.error_generic)
            else -> errorHandler.getErrorMessage(throwable)
        }
        
        _errorMessage.value = errorMessage
        _uiState.value = UiState.Error(errorMessage) { handleRetry() }
        _isLoading.value = false
    }

    /**
     * Método para manejar la acción de reintentar
     */
    open fun handleRetry() {
        // Implementar en las clases hijas si es necesario
    }

    /**
     * Ejecuta una operación asíncrona con manejo de errores
     */
    protected fun <T> execute(
        showLoading: Boolean = true,
        block: suspend () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (showLoading) {
            _isLoading.value = true
            _uiState.value = UiState.Loading
        }

        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val result = block()
                _isLoading.value = false
                _uiState.value = UiState.Success(result)
                onSuccess(result)
            } catch (e: Exception) {
                _isLoading.value = false
                val errorMsg = errorHandler.getErrorMessage(e)
                _uiState.value = UiState.Error(errorMsg) { execute(showLoading, block, onSuccess, onError) }
                onError(errorMsg)
            }
        }
    }

    /**
     * Muestra un mensaje de error
     */
    protected fun showError(message: String) {
        _errorMessage.value = message
        _uiState.value = UiState.Error(message)
    }

    /**
     * Actualiza el estado de carga
     */
    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
        _uiState.value = if (isLoading) UiState.Loading else UiState.Empty
    }

    /**
     * Actualiza el estado con datos exitosos
     */
    protected fun <T> setSuccess(data: T) {
        _isLoading.value = false
        _errorMessage.value = null
        _uiState.value = UiState.Success(data)
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _errorMessage.value = null
        if (_uiState.value is UiState.Error) {
            _uiState.value = UiState.Empty
        }
    }
}
