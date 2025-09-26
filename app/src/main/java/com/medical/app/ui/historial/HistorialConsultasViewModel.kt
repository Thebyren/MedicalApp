package com.medical.app.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.model.Consulta
import com.medical.app.data.repository.ConsultaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HistorialConsultasViewModel @Inject constructor(
    private val consultaRepository: ConsultaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialConsultasUiState())
    val uiState: StateFlow<HistorialConsultasUiState> = _uiState.asStateFlow()

    private val _consultas = MutableStateFlow<List<Consulta>>(emptyList())
    val consultas: StateFlow<List<Consulta>> = _consultas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _filteredConsultas = MutableStateFlow<List<Consulta>>(emptyList())
    val filteredConsultas: StateFlow<List<Consulta>> = _filteredConsultas

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var currentPatientId: Long = -1L

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    init {
        // Combinar flujos para búsqueda y filtrado
        combine(
            _consultas,
            _searchQuery,
            _uiState.map { it.dateRange }
        ) { consultas, query, dateRange ->
            filterConsultas(consultas, query, dateRange)
        }.onEach { filtered ->
            _filteredConsultas.value = filtered
        }.launchIn(viewModelScope)
    }

    private fun filterConsultas(
        consultas: List<Consulta>,
        query: String,
        dateRange: Pair<Date, Date>?
    ): List<Consulta> {
        return consultas.filter { consulta ->
            val matchesSearch = query.isEmpty() ||
                    consulta.motivo.contains(query, ignoreCase = true) ||
                    consulta.diagnostico?.contains(query, ignoreCase = true) == true ||
                    consulta.sintomas?.contains(query, ignoreCase = true) == true

            val matchesDate = if (dateRange != null) {
                consulta.fecha in dateRange.first..dateRange.second
            } else true

            matchesSearch && matchesDate
        }.sortedByDescending { it.fecha }
    }

    fun loadConsultas(patientId: Long) {
        currentPatientId = patientId
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                consultaRepository.getConsultasByPatient(patientId).collect { consultasList ->
                    _consultas.value = consultasList
                    _uiState.update { state ->
                        state.copy(
                            consultas = consultasList,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar el historial"
                _uiState.update { it.copy(isLoading = false, error = _error.value) }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query.trim()
    }

    fun setDateRange(start: Date?, end: Date?) {
        startDate = start
        endDate = end
        _uiState.update { state ->
            state.copy(
                dateRange = if (start != null && end != null) start to end else null
            )
        }
    }

    fun clearDateRange() {
        startDate = null
        endDate = null
        _uiState.update { it.copy(dateRange = null) }
    }

    fun setPatientId(patientId: Long) {
        currentPatientId = patientId
        _uiState.update { it.copy(patientId = patientId) }
        loadConsultas(patientId)
    }

    fun clearError() {
        _error.value = null
        _uiState.update { it.copy(error = null) }
    }

    fun getCurrentDateRangeLabel(): String? {
        return if (startDate != null && endDate != null) {
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        } else {
            null
        }
    }
}

data class HistorialConsultasUiState(
    val patientId: Long? = null,
    val consultas: List<Consulta> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val dateRange: Pair<Date, Date>? = null,
    val searchQuery: String = ""
)
