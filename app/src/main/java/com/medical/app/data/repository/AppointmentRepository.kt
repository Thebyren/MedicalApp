package com.medical.app.data.repository

import com.medical.app.data.dao.AppointmentDao
import com.medical.app.data.entities.Appointment
import com.medical.app.data.model.Appointment
import com.medical.app.data.entities.Appointment as AppointmentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

class AppointmentRepository @Inject constructor(private val appointmentDao: AppointmentDao) {

    fun getUpcomingAppointments(patientId:Long,date: Date): Flow<List<AppointmentEntity>> {
        return appointmentDao.getUpcomingAppointments(patientId,date).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getAppointmentsForDateRange(startDate: Date, endDate: Date): Flow<List<AppointmentEntity>> {
        return appointmentDao.getAppointmentsForDateRange(startDate, endDate).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun getAppointmentById(id: Long): AppointmentEntity? {
        return appointmentDao.getAppointmentById(id)?.toModel()
    }

    suspend fun insertAppointment(appointment: AppointmentEntity) {
        appointmentDao.insert(appointment.toEntity())
    }

    suspend fun updateAppointment(appointment: AppointmentEntity) {
        appointmentDao.update(appointment.toEntity())
    }

    suspend fun deleteAppointment(appointment: AppointmentEntity) {
        appointmentDao.delete(appointment.toEntity())
    }
}

// Mapper functions
private fun AppointmentEntity.toModel(): AppointmentEntity {
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

private fun Appointment.toEntity(): AppointmentEntity {
    return AppointmentEntity(
        id = this.id,
        patientId = this.patientId,
        doctorId = this.doctorId,
        title = this.title,
        description = this.description,
        dateTime = this.dateTime,
        duration = this.duration,
        status = AppointmentEntity.AppointmentStatus.valueOf(this.status.name),
        type = this.type,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
