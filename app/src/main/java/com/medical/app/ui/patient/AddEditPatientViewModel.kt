package com.medical.app.ui.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.model.Patient
import com.medical.app.data.repository.PatientRepository
import com.medical.app.ui.navigation.NavArg
import com.medical.app.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditPatientViewModel @Inject constructor(
    private val repository: PatientRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditPatientState())
    val uiState: StateFlow<AddEditPatientState> = _uiState

    private val _events = MutableStateFlow<Event?>(null)
    val events: StateFlow<Event?> = _events

    private var currentPatientId: String? = savedStateHandle[NavArg.PATIENT_ID]

    init {
        currentPatientId?.let { loadPatient(it) }
    }

    private fun loadPatient(patientId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val patient = repository.getPatientById(patientId)
                if (patient != null) {
                    _uiState.value = _uiState.value.copy(
                        firstName = patient.firstName,
                        lastName = patient.lastName,
                        dateOfBirth = patient.dateOfBirth,
                        gender = patient.gender,
                        phoneNumber = patient.phoneNumber,
                        email = patient.email ?: "",
                        address = patient.address ?: "",
                        bloodType = patient.bloodType ?: "",
                        allergies = patient.allergies ?: "",
                        notes = patient.notes ?: "",
                        isLoading = false
                    )
                } else {
                    _events.value = Event.ShowErrorMessage("Patient not found")
                }
            } catch (e: Exception) {
                _events.value = Event.ShowErrorMessage("Error loading patient: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onEvent(event: AddEditPatientEvent) {
        when (event) {
            is AddEditPatientEvent.FirstNameChanged -> {
                _uiState.value = _uiState.value.copy(firstName = event.value)
            }
            is AddEditPatientEvent.LastNameChanged -> {
                _uiState.value = _uiState.value.copy(lastName = event.value)
            }
            is AddEditPatientEvent.DateOfBirthChanged -> {
                _uiState.value = _uiState.value.copy(dateOfBirth = event.date)
            }
            is AddEditPatientEvent.GenderSelected -> {
                _uiState.value = _uiState.value.copy(gender = event.gender)
            }
            is AddEditPatientEvent.PhoneNumberChanged -> {
                _uiState.value = _uiState.value.copy(phoneNumber = event.value)
            }
            is AddEditPatientEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(email = event.value)
            }
            is AddEditPatientEvent.AddressChanged -> {
                _uiState.value = _uiState.value.copy(address = event.value)
            }
            is AddEditPatientEvent.BloodTypeSelected -> {
                _uiState.value = _uiState.value.copy(bloodType = event.bloodType)
            }
            is AddEditPatientEvent.AllergiesChanged -> {
                _uiState.value = _uiState.value.copy(allergies = event.value)
            }
            is AddEditPatientEvent.NotesChanged -> {
                _uiState.value = _uiState.value.copy(notes = event.value)
            }
            is AddEditPatientEvent.SavePatient -> {
                savePatient()
            }
        }
    }

    private fun savePatient() {
        val currentState = _uiState.value
        
        // Validate required fields
        if (currentState.firstName.isBlank() || currentState.lastName.isBlank() || 
            currentState.dateOfBirth == null || currentState.gender.isBlank() || 
            currentState.phoneNumber.isBlank()) {
            _events.value = Event.ShowErrorMessage("Please fill in all required fields")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val patient = Patient(
                    id = currentPatientId ?: UUID.randomUUID().toString(),
                    firstName = currentState.firstName.trim(),
                    lastName = currentState.lastName.trim(),
                    dateOfBirth = currentState.dateOfBirth!!,
                    gender = currentState.gender,
                    phoneNumber = currentState.phoneNumber.trim(),
                    email = currentState.email.ifBlank { null },
                    address = currentState.address.ifBlank { null },
                    bloodType = currentState.bloodType.ifBlank { null },
                    allergies = currentState.allergies.ifBlank { null },
                    notes = currentState.notes.ifBlank { null },
                    updatedAt = Date()
                )
                
                repository.savePatient(patient)
                _events.value = Event.NavigateBackWithResult(true)
                
            } catch (e: Exception) {
                _events.value = Event.ShowErrorMessage("Error saving patient: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    sealed class Event {
        data class ShowErrorMessage(val message: String) : Event()
        data class NavigateBackWithResult(val success: Boolean) : Event()
    }
}

data class AddEditPatientState(
    val isLoading: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: Date? = null,
    val gender: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val notes: String = ""
)

sealed class AddEditPatientEvent {
    data class FirstNameChanged(val value: String) : AddEditPatientEvent()
    data class LastNameChanged(val value: String) : AddEditPatientEvent()
    data class DateOfBirthChanged(val date: Date) : AddEditPatientEvent()
    data class GenderSelected(val gender: String) : AddEditPatientEvent()
    data class PhoneNumberChanged(val value: String) : AddEditPatientEvent()
    data class EmailChanged(val value: String) : AddEditPatientEvent()
    data class AddressChanged(val value: String) : AddEditPatientEvent()
    data class BloodTypeSelected(val bloodType: String) : AddEditPatientEvent()
    data class AllergiesChanged(val value: String) : AddEditPatientEvent()
    data class NotesChanged(val value: String) : AddEditPatientEvent()
    object SavePatient : AddEditPatientEvent()
}
