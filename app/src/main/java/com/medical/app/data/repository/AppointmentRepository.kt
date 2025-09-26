package com.medical.app.data.repository

import com.medical.app.data.local.dao.AppointmentDao
import com.medical.app.data.model.Appointment
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao
) {
    fun getUpcomingAppointments(patientId: Long): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointments(patientId)
    }

    suspend fun getAppointmentById(id: Long): Appointment? {
        return appointmentDao.getAppointmentById(id)
    }

    suspend fun insertAppointment(appointment: Appointment): Long {
        return appointmentDao.insert(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.update(appointment)
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.delete(appointment)
    }

    suspend fun deleteAppointmentById(id: Long) {
        appointmentDao.deleteById(id)
    }

    fun getAppointmentsInRange(patientId: Long, startDate: Date, endDate: Date): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsInRange(patientId, startDate, endDate)
    }
}
