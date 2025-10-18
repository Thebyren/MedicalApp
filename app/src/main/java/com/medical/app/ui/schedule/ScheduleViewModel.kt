package com.medical.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.dao.AppointmentWithPatient
import com.medical.app.data.entities.Appointment
import com.medical.app.data.local.SessionManager
import com.medical.app.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        selectToday()
    }

    fun selectDate(date: Date) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadAppointmentsForDate(date)
    }

    fun selectToday() {
        selectDate(Date())
    }

    fun previousDay() {
        val currentDate = _uiState.value.selectedDate ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        selectDate(calendar.time)
    }

    fun nextDay() {
        val currentDate = _uiState.value.selectedDate ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        selectDate(calendar.time)
    }

    private fun loadAppointmentsForDate(date: Date) {
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

                // Obtener inicio y fin del día seleccionado
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfDay = calendar.time

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val endOfDay = calendar.time

                // Obtener citas del día con información del paciente
                val appointmentsWithPatient = appointmentRepository
                    .getAppointmentsWithPatientForDateRange(startOfDay, endOfDay)
                    .first()
                    // Mostrar citas sin doctor asignado o del doctor actual
                    .filter { it.appointment.doctorId == null || it.appointment.doctorId == currentUser.id.toLong() }
                    .sortedBy { it.appointment.dateTime }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    appointmentsWithPatient = appointmentsWithPatient
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar citas: ${e.message}"
                )
            }
        }
    }

    fun updateAppointmentStatus(appointment: Appointment, newStatus: Appointment.AppointmentStatus) {
        viewModelScope.launch {
            try {
                val updatedAppointment = appointment.copy(
                    status = newStatus,
                    updatedAt = Date()
                )
                appointmentRepository.updateAppointment(updatedAppointment)
                
                // Recargar citas
                _uiState.value.selectedDate?.let { loadAppointmentsForDate(it) }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar estado: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val selectedDate: Date? = null,
    val appointmentsWithPatient: List<AppointmentWithPatient> = emptyList(),
    val error: String? = null
)
