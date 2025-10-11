package com.medical.app.data.repository

import com.medical.app.data.dao.AppointmentDao
import com.medical.app.data.entities.Appointment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class AppointmentRepository @Inject constructor(private val appointmentDao: AppointmentDao) {

    fun getUpcomingAppointments(patientId:Long,date: Date): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointments(patientId,date).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getAppointmentsForDateRange(startDate: Date, endDate: Date): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsForDateRange(startDate, endDate).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun getAppointmentById(id: Long): Appointment? {
        return appointmentDao.getAppointmentById(id)?.toModel()
    }

    suspend fun insertAppointment(appointment: Appointment) {
        appointmentDao.insert(appointment.toEntity())
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.update(appointment.toEntity())
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.delete(appointment.toEntity())
    }
}

// Mapper functions
private fun com.medical.app.data.entities.Appointment.toModel(): Appointment {
    return Appointment(
        id = this.id,
        patientId = this.patientId,
        doctorId = this.doctorId,
        title = this.title,
        description = this.description,
        dateTime = this.dateTime,
        duration = this.duration,
        status = Appointment.AppointmentStatus.valueOf(this.status.name),
        type = this.type,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

private fun Appointment.toEntity(): com.medical.app.data.entities.Appointment {
    return com.medical.app.data.entities.Appointment(
        id = this.id,
        patientId = this.patientId,
        doctorId = this.doctorId,
        title = this.title,
        description = this.description,
        dateTime = this.dateTime,
        duration = this.duration,
        status = com.medical.app.data.entities.Appointment.AppointmentStatus.valueOf(this.status.name),
        type = this.type,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
