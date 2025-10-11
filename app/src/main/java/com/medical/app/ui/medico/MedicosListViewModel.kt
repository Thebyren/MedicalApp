package com.medical.app.ui.medico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Medico
import com.medical.app.data.repository.MedicoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MedicosListViewModel @Inject constructor(
    private val medicoRepository: MedicoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicosListUiState())
    val uiState: StateFlow<MedicosListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadMedicos()
        
        // Filtrar médicos cuando cambia la búsqueda
        _searchQuery
            .debounce(300) // Esperar 300ms después de la última pulsación
            .onEach { query -> filterMedicos(query) }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun loadMedicos() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                medicoRepository.getAll()
                    .collect { medicos ->
                        _uiState.update { state ->
                            state.copy(
                                medicos = medicos,
                                filteredMedicos = filterMedicos(medicos, _searchQuery.value),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Error al cargar los médicos",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun filterMedicos(query: String) {
        _uiState.update { state ->
            state.copy(
                filteredMedicos = filterMedicos(state.medicos, query),
                searchQuery = query
            )
        }
    }

    private fun filterMedicos(
        medicos: List<Medico>,
        query: String
    ): List<Medico> {
        return if (query.isBlank()) {
            medicos
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            medicos.filter { medico ->
                medico.nombre?.lowercase(Locale.getDefault())?.contains(lowerCaseQuery) == true ||
                medico.especialidad?.lowercase(Locale.getDefault())?.contains(lowerCaseQuery) == true
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class MedicosListUiState(
    val medicos: List<Medico> = emptyList(),
    val filteredMedicos: List<Medico> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
