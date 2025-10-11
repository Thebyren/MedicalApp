package com.medical.app.ui.consulta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.repository.ConsultaRepository
import com.medical.app.data.entities.Consulta
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RegistroConsultaViewModel @Inject constructor(
    private val consultaRepository: ConsultaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroConsultaUiState())
    val uiState: StateFlow<RegistroConsultaUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegistroConsultaEvent) {
        when (event) {
            is RegistroConsultaEvent.MotivoChanged -> {
                _uiState.value = _uiState.value.copy(
                    motivo = event.motivo,
                    isFormValid = validateForm(
                        event.motivo,
                        _uiState.value.sintomas,
                        _uiState.value.diagnostico,
                        _uiState.value.tratamiento
                    )
                )
            }
            is RegistroConsultaEvent.SintomasChanged -> {
                _uiState.value = _uiState.value.copy(
                    sintomas = event.sintomas,
                    isFormValid = validateForm(
                        _uiState.value.motivo,
                        event.sintomas,
                        _uiState.value.diagnostico,
                        _uiState.value.tratamiento
                    )
                )
            }
            is RegistroConsultaEvent.DiagnosticoChanged -> {
                _uiState.value = _uiState.value.copy(
                    diagnostico = event.diagnostico,
                    isFormValid = validateForm(
                        _uiState.value.motivo,
                        _uiState.value.sintomas,
                        event.diagnostico,
                        _uiState.value.tratamiento
                    )
                )
            }
            is RegistroConsultaEvent.TratamientoChanged -> {
                _uiState.value = _uiState.value.copy(
                    tratamiento = event.tratamiento,
                    isFormValid = validateForm(
                        _uiState.value.motivo,
                        _uiState.value.sintomas,
                        _uiState.value.diagnostico,
                        event.tratamiento
                    )
                )
            }
            is RegistroConsultaEvent.NotasChanged -> {
                _uiState.value = _uiState.value.copy(notas = event.notas)
            }
            is RegistroConsultaEvent.ProximaCitaChanged -> {
                _uiState.value = _uiState.value.copy(proximaCita = event.fecha)
            }
            is RegistroConsultaEvent.Submit -> {
                if (_uiState.value.isFormValid) {
                    saveConsulta(
                        patientId = event.patientId,
                        motivo = _uiState.value.motivo,
                        sintomas = _uiState.value.sintomas,
                        diagnostico = _uiState.value.diagnostico,
                        tratamiento = _uiState.value.tratamiento,
                        notas = _uiState.value.notas,
                        proximaCita = _uiState.value.proximaCita
                    )
                }
            }
        }
    }

    private fun validateForm(
        motivo: String,
        sintomas: String,
        diagnostico: String,
        tratamiento: String
    ): Boolean {
        return motivo.isNotBlank() && sintomas.isNotBlank() && 
               diagnostico.isNotBlank() && tratamiento.isNotBlank()
    }

    private fun saveConsulta(
        patientId: Long,
        motivo: String,
        sintomas: String,
        diagnostico: String,
        tratamiento: String,
        notas: String,
        proximaCita: Date?
    ) {
        viewModelScope.launch {
            try {
                val consulta = Consulta(
                    medicoId = 0, // TODO: Get from session
                    pacienteId = patientId.toInt(),
                    fechaConsulta = Date(),
                    motivoConsulta = motivo,
                    diagnostico = diagnostico,
                    observaciones = "$sintomas\n\nTratamiento: $tratamiento\n\nNotas: $notas",
                    proximaCita = proximaCita
                )
                consultaRepository.insertConsulta(consulta)
                _uiState.value = _uiState.value.copy(
                    isSuccess = true,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al guardar la consulta",
                    isSuccess = false
                )
            }
        }
    }
}

data class RegistroConsultaUiState(
    val motivo: String = "",
    val sintomas: String = "",
    val diagnostico: String = "",
    val tratamiento: String = "",
    val notas: String = "",
    val proximaCita: Date? = null,
    val isFormValid: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
sealed class RegistroConsultaState {
    object Idle : RegistroConsultaState()
    object Loading : RegistroConsultaState()
    object Success : RegistroConsultaState()
    data class Error(val message: String) : RegistroConsultaState()
    data class FormValidation(val isFormValid: Boolean) : RegistroConsultaState()
}

sealed class RegistroConsultaEvent {

    data class MotivoChanged(val motivo: String) : RegistroConsultaEvent()
    data class SintomasChanged(val sintomas: String) : RegistroConsultaEvent()
    data class DiagnosticoChanged(val diagnostico: String) : RegistroConsultaEvent()
    data class TratamientoChanged(val tratamiento: String) : RegistroConsultaEvent()
    data class NotasChanged(val notas: String) : RegistroConsultaEvent()
    data class ProximaCitaChanged(val fecha: Date?) : RegistroConsultaEvent()
    data class Submit(val patientId: Long) : RegistroConsultaEvent()
}
