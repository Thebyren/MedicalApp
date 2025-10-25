package com.medical.app.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Paciente
import com.medical.app.data.repository.PacienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientProfileViewModel @Inject constructor(
    private val pacienteRepository: PacienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientProfileUiState())
    val uiState: StateFlow<PatientProfileUiState> = _uiState.asStateFlow()

    fun loadPatientData(patientId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val patient = pacienteRepository.getById(patientId.toInt())
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    patient = patient
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos del paciente: ${e.message}"
                )
            }
        }
    }

    fun updateProfile(patient: Paciente) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                pacienteRepository.update(patient)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    patient = patient,
                    profileUpdated = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al actualizar el perfil: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearUpdateFlag() {
        _uiState.value = _uiState.value.copy(profileUpdated = false)
    }
}

data class PatientProfileUiState(
    val isLoading: Boolean = false,
    val patient: Paciente? = null,
    val error: String? = null,
    val profileUpdated: Boolean = false
)
