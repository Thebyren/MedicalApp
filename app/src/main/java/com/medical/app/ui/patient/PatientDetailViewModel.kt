package com.medical.app.ui.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.entities.Paciente
import com.medical.app.data.model.Patient
import com.medical.app.data.model.toEntity
import com.medical.app.data.repository.PacienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val repository: PacienteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val patientId: String = checkNotNull(savedStateHandle["patientId"])

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableStateFlow<Event?>(null)
    val events: StateFlow<Event?> = _events.asStateFlow()

    init {
        loadPatient(patientId)
    }

    fun loadPatient(patientId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val patientEntity = repository.getById(patientId.toInt())
                if (patientEntity != null) {
                    _patient.value = patientEntity.toModel() // Convert entity to model
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
                val patientModel = _patient.value ?: return@launch
                // Assuming repository can delete using the model's ID or you have a way to get the entity
                repository.delete(patientModel.toEntity(patientModel.id.toInt()))
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

// Extension function to convert Paciente entity to Patient model
fun Paciente.toModel(): Patient {
    return Patient(
        id = this.id,
        name = this.nombre,
        lastName = this.apellidos,
        dni = this.numeroSeguridadSocial ?: "",
        birthdate = this.fechaNacimiento,
        gender = this.genero.toString(),
        phone = this.telefono ?: "",
        address = this.direccion ?: "",
        email = this.email ?: "",
        bloodType = this.bloodType ?: "",
        allergies = this.allergies ?: "",
        notes = this.notes ?: ""
    )
}
