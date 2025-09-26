package com.medical.app.ui.paciente

import androidx.lifecycle.viewModelScope
import com.medical.app.core.BaseViewModel
import com.medical.app.data.entities.Paciente
import com.medical.app.data.repository.PacienteRepository
import com.medical.app.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles para la pantalla de pacientes.
 */
sealed class PacienteState {
    object Loading : PacienteState()
    data class Success(val pacientes: List<Paciente>) : PacienteState()
    data class PacienteDetalle(val paciente: Paciente) : PacienteState()
    data class Error(val message: String) : PacienteState()
}

/**
 * Eventos que pueden ocurrir en la pantalla de pacientes.
 */
sealed class PacienteEvent {
    object NavigateBack : PacienteEvent()
    data class ShowError(val message: String) : PacienteEvent()
    data class NavigateToPacienteDetalle(val pacienteId: Int) : PacienteEvent()
}

/**
 * ViewModel para la gestión de pacientes.
 * Maneja la lógica de negocio relacionada con los pacientes.
 */
@HiltViewModel
class PacienteViewModel @Inject constructor(
    private val pacienteRepository: PacienteRepository
) : BaseViewModel<PacienteState, PacienteEvent>() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        loadPacientes()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadPacientes()
    }

    /**
     * Carga la lista de pacientes según el filtro de búsqueda actual.
     */
    fun loadPacientes() {
        viewModelScope.launch {
            setState(PacienteState.Loading)
            
            try {
                if (_searchQuery.value.isNotEmpty()) {
                    // Búsqueda por texto
                    val resultados = pacienteRepository.buscarPacientes(_searchQuery.value)
                    setState(PacienteState.Success(resultados))
                } else {
                    // Todos los pacientes
                    pacienteRepository.getAll()
                        .collect { pacientes ->
                            setState(PacienteState.Success(pacientes))
                        }
                }
            } catch (e: Exception) {
                setState(PacienteState.Error(e.message ?: "Error al cargar los pacientes"))
                postEvent(PacienteEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Carga los detalles de un paciente específico.
     */
    fun loadPacienteDetalle(pacienteId: Int) {
        viewModelScope.launch {
            setState(PacienteState.Loading)
            
            try {
                val paciente = pacienteRepository.getById(pacienteId)
                if (paciente != null) {
                    setState(PacienteState.PacienteDetalle(paciente))
                } else {
                    throw Exception("Paciente no encontrado")
                }
            } catch (e: Exception) {
                setState(PacienteState.Error(e.message ?: "Error al cargar el paciente"))
                postEvent(PacienteEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Crea o actualiza un paciente.
     */
    fun savePaciente(paciente: Paciente) {
        viewModelScope.launch {
            setState(PacienteState.Loading)
            
            try {
                val result = if (paciente.id == 0) {
                    pacienteRepository.insert(paciente)
                } else {
                    pacienteRepository.update(paciente)
                }
                
                if (result > 0) {
                    postEvent(PacienteEvent.NavigateBack)
                } else {
                    throw Exception("Error al guardar el paciente")
                }
            } catch (e: Exception) {
                setState(PacienteState.Error(e.message ?: "Error al guardar el paciente"))
                postEvent(PacienteEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Elimina un paciente.
     */
    fun deletePaciente(pacienteId: Int) {
        viewModelScope.launch {
            setState(PacienteState.Loading)
            
            try {
                val paciente = pacienteRepository.getById(pacienteId)
                if (paciente != null) {
                    val result = pacienteRepository.delete(paciente)
                    if (result > 0) {
                        postEvent(PacienteEvent.NavigateBack)
                    } else {
                        throw Exception("No se pudo eliminar el paciente")
                    }
                } else {
                    throw Exception("Paciente no encontrado")
                }
            } catch (e: Exception) {
                setState(PacienteState.Error(e.message ?: "Error al eliminar el paciente"))
                postEvent(PacienteEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }
}
