package com.medical.app.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Tratamiento
import com.medical.app.data.local.SessionManager
import com.medical.app.data.repository.PacienteRepository
import com.medical.app.data.repository.TratamientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientPrescriptionsViewModel @Inject constructor(
    private val tratamientoRepository: TratamientoRepository,
    private val pacienteRepository: PacienteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientPrescriptionsUiState())
    val uiState: StateFlow<PatientPrescriptionsUiState> = _uiState.asStateFlow()

    fun loadPrescriptions() {
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
                
                // Obtener el paciente asociado al usuario
                val patient = pacienteRepository.getPacienteByUsuarioId(currentUser.id)
                if (patient == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se encontró información del paciente"
                    )
                    return@launch
                }
                
                // Cargar recetas del paciente
                tratamientoRepository.getByPacienteId(patient.id).collect { tratamientos ->
                    val prescriptions = tratamientos.map { tratamiento ->
                        PatientPrescription(
                            id = tratamiento.id,
                            medicationName = tratamiento.medicamento,
                            dosage = tratamiento.dosis,
                            frequency = tratamiento.frecuencia,
                            duration = tratamiento.duracionDias,
                            instructions = tratamiento.indicaciones,
                            doctorName = "Dr. Pendiente", // TODO: Obtener nombre del doctor
                            prescribedDate = null, // TODO: Agregar fecha en la entidad
                            isActive = true // TODO: Determinar si está activa
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        prescriptions = prescriptions,
                        activePrescriptions = prescriptions.count { it.isActive },
                        totalPrescriptions = prescriptions.size
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar recetas: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PatientPrescriptionsUiState(
    val isLoading: Boolean = false,
    val prescriptions: List<PatientPrescription> = emptyList(),
    val activePrescriptions: Int = 0,
    val totalPrescriptions: Int = 0,
    val error: String? = null
)

data class PatientPrescription(
    val id: Int,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val duration: Int? = null,
    val instructions: String? = null,
    val doctorName: String,
    val prescribedDate: java.util.Date? = null,
    val isActive: Boolean = true
)
