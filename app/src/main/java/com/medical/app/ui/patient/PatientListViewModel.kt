package com.medical.app.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.medical.app.data.model.Patient
import com.medical.app.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientListViewModel @Inject constructor(
    private val repository: PatientRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentQuery: String = ""
    
    // Flujo de datos paginados
    val patients: Flow<PagingData<Patient>> = repository.getPatientsPaged(currentQuery)
        .cachedIn(viewModelScope)
        .map { pagingData ->
            pagingData.map { patient ->
                // Aquí podrías hacer transformaciones a los datos si es necesario
                patient
            }
        }

    /**
     * Actualiza la consulta de búsqueda
     * @param query Término de búsqueda
     */
    fun searchPatients(query: String) {
        currentQuery = query
        // La actualización del flujo se manejará automáticamente
    }
    
    /**
     * Marca un error como mostrado
     */
    fun onErrorShown() {
        _error.value = null
    }
    
    companion object {
        private const val TAG = "PatientListViewModel"
    }
}
