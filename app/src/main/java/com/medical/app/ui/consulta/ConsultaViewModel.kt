package com.medical.app.ui.consulta

import androidx.lifecycle.viewModelScope
import com.medical.app.core.BaseViewModel
import com.medical.app.data.entities.Consulta
import com.medical.app.data.entities.Medico
import com.medical.app.data.entities.Paciente
import com.medical.app.data.entities.Tratamiento
import com.medical.app.data.repository.ConsultaRepository
import com.medical.app.data.repository.MedicoRepository
import com.medical.app.data.repository.PacienteRepository
import com.medical.app.data.repository.TratamientoRepository
import com.medical.app.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * Estados posibles para la pantalla de consultas.
 */
sealed class ConsultaState {
    object Loading : ConsultaState()
    data class Success(val consultas: List<Consulta>) : ConsultaState()
    data class ConsultaDetalle(
        val consulta: Consulta,
        val medico: Medico? = null,
        val paciente: Paciente? = null,
        val tratamientos: List<Tratamiento> = emptyList()
    ) : ConsultaState()
    data class Error(val message: String) : ConsultaState()
}

/**
 * Eventos que pueden ocurrir en la pantalla de consultas.
 */
sealed class ConsultaEvent {
    object NavigateBack : ConsultaEvent()
    data class ShowError(val message: String) : ConsultaEvent()
    data class NavigateToConsultaDetalle(val consultaId: Int) : ConsultaEvent()
    data class ShowTratamientos(val tratamientos: List<Tratamiento>) : ConsultaEvent()
}

/**
 * ViewModel para la gestión de consultas médicas.
 * Maneja la lógica de negocio relacionada con las consultas.
 */
@HiltViewModel
class ConsultaViewModel @Inject constructor(
    private val consultaRepository: ConsultaRepository,
    private val tratamientoRepository: TratamientoRepository,
    private val medicoRepository: MedicoRepository,
    private val pacienteRepository: PacienteRepository
) : BaseViewModel<ConsultaState, ConsultaEvent>() {

    private val _fechaFiltro = MutableStateFlow<Date?>(null)
    val fechaFiltro: StateFlow<Date?> = _fechaFiltro

    private val _medicoFiltro = MutableStateFlow<Int?>(null)
    val medicoFiltro: StateFlow<Int?> = _medicoFiltro

    private val _pacienteFiltro = MutableStateFlow<Int?>(null)
    val pacienteFiltro: StateFlow<Int?> = _pacienteFiltro

    init {
        loadConsultas()
    }

    fun setFechaFiltro(fecha: Date?) {
        _fechaFiltro.value = fecha
        loadConsultas()
    }

    fun setMedicoFiltro(medicoId: Int?) {
        _medicoFiltro.value = medicoId
        loadConsultas()
    }

    fun setPacienteFiltro(pacienteId: Int?) {
        _pacienteFiltro.value = pacienteId
        loadConsultas()
    }

    /**
     * Carga la lista de consultas según los filtros actuales.
     */
    fun loadConsultas() {
        viewModelScope.launch {
            setState(ConsultaState.Loading)
            
            try {
                // Aquí implementarías la lógica para filtrar las consultas
                // según los filtros actuales (_fechaFiltro, _medicoFiltro, _pacienteFiltro)
                val pacienteId = _pacienteFiltro.value

                if (pacienteId != null) {
                    consultaRepository.getConsultasByPatient(pacienteId.toLong())
                        .collect { consultas ->
                            setState(ConsultaState.Success(consultas))
                        }
                } else {
                    // Si no hay un paciente seleccionado, mostramos una lista vacía.
                    // O podrías decidir cargar las consultas de un paciente por defecto o mostrar un mensaje.
                    setState(ConsultaState.Success(emptyList()))
                }
            } catch (e: Exception) {
                setState(ConsultaState.Error(e.message ?: "Error al cargar las consultas"))
                postEvent(ConsultaEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Carga los detalles de una consulta específica, incluyendo información relacionada.
     */
    fun loadConsultaDetalle(consultaId: Int) {
        viewModelScope.launch {
            try {
                val consulta = consultaRepository.getConsultaById(consultaId.toLong()) // Corregido
                if (consulta != null) {
                    // Cargar información relacionada en paralelo
                    val medico = medicoRepository.getById(consulta.medicoId)
                    val paciente = pacienteRepository.getById(consulta.pacienteId)
                    val tratamientos = tratamientoRepository.getByConsultaId(consultaId).first()

                    setState(
                        ConsultaState.ConsultaDetalle(
                            consulta = consulta,
                            medico = medico,
                            paciente = paciente,
                            tratamientos = tratamientos
                        )
                    )
                } else {
                    throw Exception("Consulta no encontrada")
                }
            } catch (e: Exception) {
                setState(ConsultaState.Error(e.message ?: "Error al cargar la consulta"))
                postEvent(ConsultaEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }
    /**
     * Crea o actualiza una consulta.
     */
    fun saveConsulta(consulta: Consulta) {
        viewModelScope.launch {
            setState(ConsultaState.Loading)
            
            try {
                if (consulta.id.toLong() == 0L) { // Use Long for ID comparison
                    val newId = consultaRepository.insertConsulta(consulta)
                    if (newId > 0) {
                        postEvent(ConsultaEvent.NavigateBack)
                    } else {
                        throw Exception("Error al insertar la consulta")
                    }
                } else {
                    consultaRepository.updateConsulta(consulta)
                    postEvent(ConsultaEvent.NavigateBack) // Assume success if no exception
                }
            } catch (e: Exception) {
                setState(ConsultaState.Error(e.message ?: "Error al guardar la consulta"))
                postEvent(ConsultaEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Elimina una consulta.
     */
    fun deleteConsulta(consultaId: Int) {
        viewModelScope.launch {
            setState(ConsultaState.Loading)

            try {
                val consulta = consultaRepository.getConsultaById(consultaId.toLong())
                if (consulta != null) {
                    consultaRepository.deleteConsulta(consulta)
                    postEvent(ConsultaEvent.NavigateBack) // Assume success if no exception
                } else {
                    throw Exception("Consulta no encontrada")
                }
            } catch (e: Exception) {
                setState(ConsultaState.Error(e.message ?: "Error al eliminar la consulta"))
                postEvent(ConsultaEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Carga los tratamientos de una consulta.
     */
    fun loadTratamientos(consultaId: Int) {
        viewModelScope.launch {
            try {
                val tratamientos = tratamientoRepository.getByConsultaId(consultaId).first()
                postEvent(ConsultaEvent.ShowTratamientos(tratamientos))
            } catch (e: Exception) {
                postEvent(ConsultaEvent.ShowError("Error al cargar los tratamientos: ${e.message}"))
            }
        }
    }
}
