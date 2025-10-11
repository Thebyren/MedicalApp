package com.medical.app.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Appointment
import com.medical.app.data.repository.AppointmentRepository
import com.medical.app.data.repository.PacienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PatientHomeViewModel @Inject constructor(
    private val patientRepository: PacienteRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientHomeUiState())
    val uiState: StateFlow<PatientHomeUiState> = _uiState.asStateFlow()

    private val _upcomingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val upcomingAppointments: StateFlow<List<Appointment>> = _upcomingAppointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadPatientData(patientId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Cargar datos del paciente
                val patient = patientRepository.getById(patientId.toInt())
                
                // Cargar citas próximas
                appointmentRepository.getUpcomingAppointments(patientId, Date())
                    .collect { appointments ->
                        _upcomingAppointments.value = appointments
                        _uiState.value = _uiState.value.copy(
                            patientName = patient?.nombre ?: "",
                            nextAppointment = appointments.firstOrNull()
                        )
                    }
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar los datos"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData(patientId: Long) {
        loadPatientData(patientId)
    }

    fun clearError() {
        _error.value = null
    }
}

data class PatientHomeUiState(
    val patientName: String = "",
    val nextAppointment: Appointment? = null,
    val upcomingAppointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
