package com.medical.app.ui.appointment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Appointment
import com.medical.app.data.entities.toModel
import com.medical.app.data.model.Patient
import com.medical.app.data.repository.AppointmentRepository
import com.medical.app.data.repository.PacienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NewAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val pacienteRepository: PacienteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val appointmentId: Long = savedStateHandle["appointmentId"] ?: -1L
    
    private val _uiState = MutableStateFlow(NewAppointmentState())
    val uiState: StateFlow<NewAppointmentState> = _uiState.asStateFlow()
    
    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    
    val patients: Flow<List<Patient>> = pacienteRepository.getAll().map { pacientes ->
        pacientes.map { it.toModel() }
    }
    
    init {
        loadAppointmentTypes()
        loadDurationOptions()
        if (appointmentId != -1L) {
            loadAppointment(appointmentId)
        }
    }
    
    private fun loadAppointment(id: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val appointment = appointmentRepository.getAppointmentById(id)
                
                if (appointment != null) {
                    // Cargar paciente
                    val patients = pacienteRepository.getAll().first()
                    val patient = patients.find { it.id.toLong() == appointment.patientId }?.toModel()
                    
                    // Extraer hora de la fecha
                    val calendar = Calendar.getInstance().apply { time = appointment.dateTime }
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val minute = calendar.get(Calendar.MINUTE)
                    val timeString = String.format("%02d:%02d", hour, minute)
                    
                    // Convertir duración a string
                    val durationString = when (appointment.duration) {
                        15 -> "15 minutos"
                        30 -> "30 minutos"
                        45 -> "45 minutos"
                        60 -> "1 hora"
                        90 -> "1 hora 30 minutos"
                        120 -> "2 horas"
                        else -> "30 minutos"
                    }
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedPatient = patient,
                            title = appointment.title,
                            appointmentType = appointment.type,
                            date = appointment.dateTime,
                            time = timeString,
                            duration = durationString,
                            description = appointment.description ?: ""
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(Event.ShowErrorMessage("No se encontró la cita"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(Event.ShowErrorMessage("Error al cargar la cita: ${e.message}"))
            }
        }
    }
    
    private fun loadAppointmentTypes() {
        val types = listOf(
            "Consulta General",
            "Consulta de Seguimiento",
            "Urgencia",
            "Control Rutinario",
            "Primera Consulta",
            "Teleconsulta",
            "Procedimiento",
            "Otro"
        )
        _uiState.update { it.copy(appointmentTypes = types) }
    }
    
    private fun loadDurationOptions() {
        val durations = listOf(
            "15 minutos",
            "30 minutos",
            "45 minutos",
            "1 hora",
            "1 hora 30 minutos",
            "2 horas"
        )
        _uiState.update { it.copy(durationOptions = durations) }
    }
    
    fun onEvent(event: NewAppointmentEvent) {
        when (event) {
            is NewAppointmentEvent.PatientSelected -> {
                _uiState.update { it.copy(selectedPatient = event.patient) }
            }
            is NewAppointmentEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.title) }
            }
            is NewAppointmentEvent.TypeSelected -> {
                _uiState.update { it.copy(appointmentType = event.type) }
            }
            is NewAppointmentEvent.DateSelected -> {
                _uiState.update { it.copy(date = event.date) }
            }
            is NewAppointmentEvent.TimeSelected -> {
                _uiState.update { it.copy(time = event.time) }
            }
            is NewAppointmentEvent.DurationSelected -> {
                _uiState.update { it.copy(duration = event.duration) }
            }
            is NewAppointmentEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.description) }
            }
            is NewAppointmentEvent.CostChanged -> {
                _uiState.update { it.copy(cost = event.cost) }
            }
            NewAppointmentEvent.SaveAppointment -> {
                saveAppointment()
            }
        }
    }
    
    private fun saveAppointment() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Validar campos requeridos
            when {
                state.selectedPatient == null -> {
                    _events.send(Event.ShowErrorMessage("Por favor selecciona un paciente"))
                    return@launch
                }
                state.title.isBlank() -> {
                    _events.send(Event.ShowErrorMessage("Por favor ingresa un título"))
                    return@launch
                }
                state.date == null -> {
                    _events.send(Event.ShowErrorMessage("Por favor selecciona una fecha"))
                    return@launch
                }
                state.time.isBlank() -> {
                    _events.send(Event.ShowErrorMessage("Por favor selecciona una hora"))
                    return@launch
                }
                state.duration.isBlank() -> {
                    _events.send(Event.ShowErrorMessage("Por favor selecciona la duración"))
                    return@launch
                }
            }
            
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Combinar fecha y hora
                val calendar = Calendar.getInstance().apply {
                    time = state.date!!
                    val timeParts = state.time.split(":")
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    set(Calendar.MINUTE, timeParts[1].toInt())
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                // Parsear duración
                val durationMinutes = parseDuration(state.duration)
                
                if (appointmentId == -1L) {
                    // Crear nueva cita
                    val appointment = Appointment(
                        patientId = state.selectedPatient!!.id.toLong(),
                        doctorId = null,
                        title = state.title,
                        description = state.description,
                        dateTime = calendar.time,
                        duration = durationMinutes,
                        type = state.appointmentType,
                        status = Appointment.AppointmentStatus.SCHEDULED,
                        cost = state.cost,
                        isPaid = false
                    )
                    appointmentRepository.insertAppointment(appointment)
                } else {
                    // Actualizar cita existente
                    val existingAppointment = appointmentRepository.getAppointmentById(appointmentId)
                    if (existingAppointment != null) {
                        val updatedAppointment = existingAppointment.copy(
                            patientId = state.selectedPatient!!.id.toLong(),
                            title = state.title,
                            description = state.description,
                            dateTime = calendar.time,
                            duration = durationMinutes,
                            type = state.appointmentType,
                            cost = state.cost,
                            updatedAt = Date()
                        )
                        appointmentRepository.updateAppointment(updatedAppointment)
                    }
                }
                _events.send(Event.NavigateBackWithResult(true))
                
            } catch (e: Exception) {
                _events.send(Event.ShowErrorMessage("Error al guardar la cita: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun parseDuration(duration: String): Int {
        return when (duration) {
            "15 minutos" -> 15
            "30 minutos" -> 30
            "45 minutos" -> 45
            "1 hora" -> 60
            "1 hora 30 minutos" -> 90
            "2 horas" -> 120
            else -> 30
        }
    }
    
    sealed class Event {
        data class ShowErrorMessage(val message: String) : Event()
        data class NavigateBackWithResult(val success: Boolean) : Event()
    }
}

data class NewAppointmentState(
    val isLoading: Boolean = false,
    val selectedPatient: Patient? = null,
    val title: String = "",
    val appointmentType: String = "Consulta General",
    val date: Date? = null,
    val time: String = "",
    val duration: String = "30 minutos",
    val description: String = "",
    val cost: Double = 0.0,
    val appointmentTypes: List<String> = emptyList(),
    val durationOptions: List<String> = emptyList()
)

sealed class NewAppointmentEvent {
    data class PatientSelected(val patient: Patient?) : NewAppointmentEvent()
    data class TitleChanged(val title: String) : NewAppointmentEvent()
    data class TypeSelected(val type: String) : NewAppointmentEvent()
    data class DateSelected(val date: Date) : NewAppointmentEvent()
    data class TimeSelected(val time: String) : NewAppointmentEvent()
    data class DurationSelected(val duration: String) : NewAppointmentEvent()
    data class DescriptionChanged(val description: String) : NewAppointmentEvent()
    data class CostChanged(val cost: Double) : NewAppointmentEvent()
    object SaveAppointment : NewAppointmentEvent()
}
