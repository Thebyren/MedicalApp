package com.medical.app.ui.medico

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medical.app.data.model.DoctorStats
import com.medical.app.data.repository.AppointmentRepository
import com.medical.app.data.repository.PacienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MedicoHomeViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PacienteRepository
) : ViewModel() {

    private val _stats = MutableLiveData<DoctorStats>()
    val stats: LiveData<DoctorStats> = _stats

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadDoctorStats(doctorId: String) {
        viewModelScope.launch {
            try {
                // 1. Get total patients
                val totalPatients = patientRepository.getPacientesPorMedico(doctorId.toInt()).first().size

                // 2. Get appointments for today
                val calendar = Calendar.getInstance()
                val startOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val endOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time

                val appointments = appointmentRepository.getAppointmentsForDateRange(startOfDay, endOfDay).first()
                // --- LA CORRECCIÓN ESTÁ AQUÍ ---
                val appointmentsToday = appointments.count { it.doctorId == doctorId.toLong() }

                // 3. Get monthly earnings (TODO: Implement this logic in the repository)
                val monthlyEarnings = 0.0

                _stats.value = DoctorStats(
                    appointmentsToday = appointmentsToday,
                    totalPatients = totalPatients,
                    monthlyEarnings = monthlyEarnings
                )
            } catch (e: Exception) {
                _error.value = "Error loading stats: ${e.message}"
            }
        }
    }
}