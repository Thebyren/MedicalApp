package com.medical.app.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.medical.app.data.entities.toModel
import com.medical.app.data.model.Patient
import com.medical.app.data.repository.PacienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PatientListViewModel @Inject constructor(
    private val repository: PacienteRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val patients: Flow<PagingData<Patient>> = query.flatMapLatest { query ->
        repository.getPacientesPaginados(query)
    }.map { pagingData ->
        pagingData.map { paciente ->
            paciente.toModel()
        }
    }.cachedIn(viewModelScope)


    /**
     * Actualiza la consulta de búsqueda
     * @param query Término de búsqueda
     */
    fun searchPatients(query: String) {
        _query.value = query
    }

    companion object {
        private const val TAG = "PatientListViewModel"
    }
}
