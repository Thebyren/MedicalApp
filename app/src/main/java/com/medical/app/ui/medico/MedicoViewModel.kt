package com.medical.app.ui.medico

import androidx.lifecycle.viewModelScope
import com.medical.app.core.BaseViewModel
import com.medical.app.data.entities.Medico
import com.medical.app.data.repository.MedicoRepository
import com.medical.app.data.repository.PacienteRepository
import com.medical.app.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estados posibles para la pantalla de médicos.
 */
sealed class MedicoState {
    object Loading : MedicoState()
    data class Success(val medicos: List<Medico>) : MedicoState()
    data class MedicoDetalle(val medico: Medico) : MedicoState()
    data class Error(val message: String) : MedicoState()
}

/**
 * Eventos que pueden ocurrir en la pantalla de médicos.
 */
sealed class MedicoEvent {
    object NavigateBack : MedicoEvent()
    data class ShowError(val message: String) : MedicoEvent()
    data class NavigateToMedicoDetalle(val medicoId: Int) : MedicoEvent()
}

/**
 * ViewModel para la gestión de médicos.
 * Maneja la lógica de negocio relacionada con los médicos.
 */
@HiltViewModel
class MedicoViewModel @Inject constructor(
    private val medicoRepository: MedicoRepository,
    private val pacienteRepository: PacienteRepository
) : BaseViewModel<MedicoState, MedicoEvent>() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _especialidadFiltro = MutableStateFlow<String?>(null)
    val especialidadFiltro: StateFlow<String?> = _especialidadFiltro

    init {
        loadMedicos()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        loadMedicos()
    }

    fun setEspecialidadFiltro(especialidad: String?) {
        _especialidadFiltro.value = especialidad
        loadMedicos()
    }

    /**
     * Carga la lista de médicos según los filtros actuales.
     */
    fun loadMedicos() {
        viewModelScope.launch {
            setState(MedicoState.Loading)
            
            try {
                val medicos = if (_especialidadFiltro.value != null) {
                    medicoRepository.getMedicosPorEspecialidad(_especialidadFiltro.value!!)
                        .collectLatest { list ->
                            setState(MedicoState.Success(list))
                        }
                } else {
                    medicoRepository.getAll()
                        .collectLatest { list ->
                            val filteredList = if (_searchQuery.value.isNotEmpty()) {
                                list.filter { medico ->
                                    medico.nombre.contains(_searchQuery.value, ignoreCase = true) ||
                                    medico.apellidos.contains(_searchQuery.value, ignoreCase = true) ||
                                    medico.especialidad.contains(_searchQuery.value, ignoreCase = true)
                                }
                            } else {
                                list
                            }
                            setState(MedicoState.Success(filteredList))
                        }
                }
            } catch (e: Exception) {
                setState(MedicoState.Error(e.message ?: "Error al cargar los médicos"))
                postEvent(MedicoEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Carga los detalles de un médico específico.
     */
    fun loadMedicoDetalle(medicoId: Int) {
        viewModelScope.launch {
            setState(MedicoState.Loading)
            
            try {
                val medico = medicoRepository.getById(medicoId)
                if (medico != null) {
                    setState(MedicoState.MedicoDetalle(medico))
                } else {
                    throw Exception("Médico no encontrado")
                }
            } catch (e: Exception) {
                setState(MedicoState.Error(e.message ?: "Error al cargar el médico"))
                postEvent(MedicoEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Crea o actualiza un médico.
     */
    fun saveMedico(medico: Medico) {
        viewModelScope.launch {
            setState(MedicoState.Loading)
            
            try {
                val result = if (medico.id == 0) {
                    medicoRepository.insert(medico)
                } else {
                    medicoRepository.update(medico)
                }
                
                if (result > 0) {
                    postEvent(MedicoEvent.NavigateBack)
                } else {
                    throw Exception("Error al guardar el médico")
                }
            } catch (e: Exception) {
                setState(MedicoState.Error(e.message ?: "Error al guardar el médico"))
                postEvent(MedicoEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }

    /**
     * Elimina un médico.
     */
    fun deleteMedico(medicoId: Int) {
        viewModelScope.launch {
            setState(MedicoState.Loading)
            
            try {
                val medico = medicoRepository.getById(medicoId)
                if (medico != null) {
                    val result = medicoRepository.delete(medico)
                    if (result > 0) {
                        postEvent(MedicoEvent.NavigateBack)
                    } else {
                        throw Exception("No se pudo eliminar el médico")
                    }
                } else {
                    throw Exception("Médico no encontrado")
                }
            } catch (e: Exception) {
                setState(MedicoState.Error(e.message ?: "Error al eliminar el médico"))
                postEvent(MedicoEvent.ShowError(e.message ?: "Error desconocido"))
            }
        }
    }
}
