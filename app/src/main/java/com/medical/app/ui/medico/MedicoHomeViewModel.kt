// MedicoHomeViewModel.kt
package com.medical.app.ui.medico

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.model.DoctorStats
import com.medical.app.data.repository.AppointmentRepository
import com.medical.app.data.repository.PatientRepository
import com.medical.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicoHomeViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _stats = MutableLiveData<DoctorStats>()
    val stats: LiveData<DoctorStats> = _stats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDoctorStats(doctorId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Obtener estadísticas del médico
                val appointmentsToday = appointmentRepository.getAppointmentsCountForToday(doctorId)
                val totalPatients = patientRepository.getTotalPatientsCount(doctorId)
                val monthlyEarnings = appointmentRepository.getMonthlyEarnings(doctorId)

                _stats.value = DoctorStats(
                    appointmentsToday = appointmentsToday,
                    totalPatients = totalPatients,
                    monthlyEarnings = monthlyEarnings
                )
            } catch (e: Exception) {
                _error.value = "Error al cargar las estadísticas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}