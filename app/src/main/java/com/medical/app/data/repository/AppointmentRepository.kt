package com.medical.app.data.repository

import com.medical.app.data.dao.AppointmentDao
import com.medical.app.data.dao.AppointmentWithPatient
import com.medical.app.data.entities.Appointment
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class AppointmentRepository @Inject constructor(private val appointmentDao: AppointmentDao) {

    fun getUpcomingAppointments(patientId: Long, date: Date): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointments(patientId, date)
    }

    fun getAppointmentsForDateRange(startDate: Date, endDate: Date): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsForDateRange(startDate, endDate)
    }

    fun getAppointmentsWithPatientForDateRange(startDate: Date, endDate: Date): Flow<List<AppointmentWithPatient>> {
        return appointmentDao.getAppointmentsWithPatientForDateRange(startDate, endDate)
    }

    suspend fun getAppointmentById(id: Long): Appointment? {
        return appointmentDao.getAppointmentById(id)
    }

    suspend fun insertAppointment(appointment: Appointment) {
        appointmentDao.insert(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.update(appointment)
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.delete(appointment)
    }
}
