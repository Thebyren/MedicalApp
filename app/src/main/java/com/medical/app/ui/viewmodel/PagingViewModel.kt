package com.medical.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.medical.app.data.entities.Paciente
import com.medical.app.data.repository.PacienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientPagingViewModel @Inject constructor(
    private val repository: PacienteRepository
) : ViewModel() {

    private val _pagingData = MutableStateFlow<PagingData<Paciente>>(PagingData.empty())
    val pagingData: StateFlow<PagingData<Paciente>> = _pagingData

    private var currentQuery: String = ""

    init {
        loadData()
    }

    /**
     * Carga los datos iniciales o realiza una búsqueda.
     */
    fun loadData(query: String = "") {
        // Si la consulta es la misma, no hacemos nada para evitar recargas innecesarias
        if (query == currentQuery && _pagingData.value != PagingData.empty<Paciente>()) return

        currentQuery = query

        viewModelScope.launch {
            repository.getPacientesPaginados(query)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _pagingData.value = pagingData
                }
        }
    }

    /**
     * Actualiza los datos actuales, re-ejecutando la última consulta.
     */
    fun refresh() {
        loadData(currentQuery)
    }
}
