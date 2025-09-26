package com.medical.app.ui.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.model.Patient
import com.medical.app.data.repository.PatientRepository
import com.medical.app.ui.navigation.NavArg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val repository: PatientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: String = checkNotNull(savedStateHandle[NavArg.PATIENT_ID])
    
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _events = MutableStateFlow<Event?>(null)
    val events: StateFlow<Event?> = _events.asStateFlow()

    fun loadPatient(patientId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val patient = repository.getPatientById(patientId)
                if (patient != null) {
                    _patient.value = patient
                } else {
                    _events.value = Event.ShowErrorMessage("Patient not found")
                }
            } catch (e: Exception) {
                _events.value = Event.ShowErrorMessage("Error loading patient: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deletePatient() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val patient = _patient.value ?: return@launch
                repository.deletePatient(patient)
                _events.value = Event.NavigateBackWithResult(deleted = true)
            } catch (e: Exception) {
                _events.value = Event.ShowErrorMessage("Error deleting patient: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    sealed class Event {
        data class ShowErrorMessage(val message: String) : Event()
        data class NavigateBackWithResult(val deleted: Boolean = false) : Event()
    }
}
