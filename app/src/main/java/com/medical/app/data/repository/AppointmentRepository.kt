package com.medical.app.data.repository

import com.medical.app.data.dao.AppointmentDao
import com.medical.app.data.dao.AppointmentWithPatient
import com.medical.app.data.entities.Appointment
import com.medical.app.data.entities.EntityType
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val dailyIncomeRepository: DailyIncomeRepository,  // PASO 3: Descomentado
    private val syncRepository: SyncRepository
) {

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

    suspend fun insertAppointment(appointment: Appointment): Long {
        val id = appointmentDao.insert(appointment)
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.APPOINTMENTS, id)
        return id
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.update(appointment)
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.APPOINTMENTS, appointment.id)
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.delete(appointment)
        // Marcar como eliminado para sincronización
        syncRepository.markForSync(EntityType.APPOINTMENTS, appointment.id)
    }
    
    /**
     * Confirma una cita y actualiza los ingresos diarios si tiene costo
     */
    suspend fun confirmAppointment(appointmentId: Long, doctorId: Long) {
        val appointment = appointmentDao.getAppointmentById(appointmentId) ?: return
        
        // Actualizar estado de la cita a CONFIRMED
        val confirmedAppointment = appointment.copy(
            status = Appointment.AppointmentStatus.CONFIRMED,
            isPaid = true,
            updatedAt = Date()
        )
        appointmentDao.update(confirmedAppointment)
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.APPOINTMENTS, confirmedAppointment.id)
        
        // Si la cita tiene costo, actualizar los ingresos diarios
        if (confirmedAppointment.cost > 0) {
            dailyIncomeRepository.updateDailyIncome(
                doctorId = doctorId,
                date = confirmedAppointment.dateTime,
                amount = confirmedAppointment.cost
            )
        }
    }
    
    /**
     * Completa una cita y actualiza los ingresos diarios si no se había confirmado antes
     */
    suspend fun completeAppointment(appointmentId: Long, doctorId: Long) {
        val appointment = appointmentDao.getAppointmentById(appointmentId) ?: return
        
        // Actualizar estado de la cita a COMPLETED
        val completedAppointment = appointment.copy(
            status = Appointment.AppointmentStatus.COMPLETED,
            updatedAt = Date()
        )
        appointmentDao.update(completedAppointment)
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.APPOINTMENTS, completedAppointment.id)
        
        // Si la cita tiene costo y no estaba pagada, actualizar los ingresos diarios
        if (completedAppointment.cost > 0 && !appointment.isPaid) {
            val paidAppointment = completedAppointment.copy(isPaid = true)
            appointmentDao.update(paidAppointment)
            // Marcar para sincronización
            syncRepository.markForSync(EntityType.APPOINTMENTS, paidAppointment.id)
            
            dailyIncomeRepository.updateDailyIncome(
                doctorId = doctorId,
                date = completedAppointment.dateTime,
                amount = completedAppointment.cost
            )
        }
    }
    
    /**
     * Cancela una cita
     */
    suspend fun cancelAppointment(appointmentId: Long) {
        val appointment = appointmentDao.getAppointmentById(appointmentId) ?: return
        
        val cancelledAppointment = appointment.copy(
            status = Appointment.AppointmentStatus.CANCELLED,
            updatedAt = Date()
        )
        appointmentDao.update(cancelledAppointment)
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.APPOINTMENTS, cancelledAppointment.id)
    }
}
