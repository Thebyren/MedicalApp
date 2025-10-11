package com.medical.app.ui.prescription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Tratamiento
import com.medical.app.data.local.SessionManager
import com.medical.app.data.repository.TratamientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrescriptionsViewModel @Inject constructor(
    private val tratamientoRepository: TratamientoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionsUiState())
    val uiState: StateFlow<PrescriptionsUiState> = _uiState.asStateFlow()

    init {
        loadPrescriptions()
    }

    private fun loadPrescriptions() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val currentUser = sessionManager.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No hay sesión activa"
                    )
                    return@launch
                }

                // TODO: Obtener prescripciones del médico actual
                // Por ahora, mostrar lista vacía
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    prescriptions = emptyList()
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar prescripciones: ${e.message}"
                )
            }
        }
    }

    fun addPrescription(prescription: Tratamiento) {
        viewModelScope.launch {
            try {
                tratamientoRepository.insert(prescription)
                loadPrescriptions()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al crear prescripción: ${e.message}"
                )
            }
        }
    }

    fun deletePrescription(prescription: Tratamiento) {
        viewModelScope.launch {
            try {
                tratamientoRepository.delete(prescription)
                loadPrescriptions()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al eliminar prescripción: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PrescriptionsUiState(
    val isLoading: Boolean = false,
    val prescriptions: List<Tratamiento> = emptyList(),
    val error: String? = null
)
