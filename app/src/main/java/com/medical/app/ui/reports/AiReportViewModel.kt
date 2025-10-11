package com.medical.app.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.BuildConfig
import com.medical.app.ai.GeminiService
import com.medical.app.data.local.SessionManager
import com.medical.app.data.repository.AppointmentRepository
import com.medical.app.data.repository.PacienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AiReportViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val sessionManager: SessionManager,
    private val patientRepository: PacienteRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiReportUiState())
    val uiState: StateFlow<AiReportUiState> = _uiState.asStateFlow()

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    init {
        // Inicializar Gemini
        try {
            geminiService.initialize(apiKey)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Error al inicializar Gemini: ${e.message}"
            )
        }
    }

    fun generateGeneralStatsReport() {
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

                // Obtener datos
                val patients = patientRepository.getPacientesPorMedico(currentUser.id).first()
                val calendar = Calendar.getInstance()
                val startOfMonth = calendar.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time
                val endOfMonth = calendar.apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time

                val appointments = appointmentRepository.getAppointmentsForDateRange(startOfMonth, endOfMonth).first()

                // Preparar datos para Gemini
                val data = mapOf(
                    "Total de Pacientes" to patients.size,
                    "Citas del Mes" to appointments.size,
                    "Citas Completadas" to appointments.count { it.status.name == "COMPLETED" },
                    "Citas Canceladas" to appointments.count { it.status.name == "CANCELLED" },
                    "Citas Pendientes" to appointments.count { it.status.name == "SCHEDULED" },
                    "Promedio de Citas por Día" to (appointments.size / 30.0).toString()
                )

                // Generar reporte con Gemini
                val report = geminiService.generateGeneralStatsReport(data)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    report = report
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al generar reporte: ${e.message}"
                )
            }
        }
    }

    fun generatePatientsTrendReport() {
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

                // Obtener pacientes
                val patients = patientRepository.getPacientesPorMedico(currentUser.id).first()

                // Obtener citas de los últimos 6 meses para analizar tendencias
                val calendar = Calendar.getInstance()
                val endDate = calendar.time
                calendar.add(Calendar.MONTH, -6)
                val startDate = calendar.time

                val appointments = appointmentRepository.getAppointmentsForDateRange(startDate, endDate).first()
                    .filter { it.doctorId == currentUser.id.toLong() }

                // Agrupar citas por mes para ver la actividad
                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                val monthlyData = appointments
                    .groupBy { monthFormat.format(it.dateTime) }
                    .map { (month, appointmentsList) -> month to appointmentsList.size }
                    .sortedBy { it.first }

                // Si no hay datos de citas, usar datos generales de pacientes
                val finalData = if (monthlyData.isEmpty()) {
                    listOf(
                        "Total de Pacientes" to patients.size,
                        "Promedio de Edad" to (patients.map { 
                            val age = Calendar.getInstance().get(Calendar.YEAR) - 
                                     Calendar.getInstance().apply { time = it.fechaNacimiento }.get(Calendar.YEAR)
                            age
                        }.average().toInt())
                    )
                } else {
                    monthlyData
                }

                // Generar reporte con Gemini
                val report = geminiService.generatePatientsTrendReport(finalData)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    report = report
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al generar reporte: ${e.message}"
                )
            }
        }
    }

    fun generateAppointmentsAnalysis() {
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

                // Obtener citas del último mes
                val calendar = Calendar.getInstance()
                val endDate = calendar.time
                calendar.add(Calendar.MONTH, -1)
                val startDate = calendar.time

                val appointments = appointmentRepository.getAppointmentsForDateRange(startDate, endDate).first()
                    .filter { it.doctorId == currentUser.id.toLong() }

                // Agrupar por estado
                val statusData = appointments
                    .groupBy { it.status.name }
                    .mapValues { it.value.size }

                // Generar reporte con Gemini
                val report = geminiService.generateAppointmentsAnalysis(statusData)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    report = report
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al generar reporte: ${e.message}"
                )
            }
        }
    }
}

data class AiReportUiState(
    val isLoading: Boolean = false,
    val report: String? = null,
    val error: String? = null
)
