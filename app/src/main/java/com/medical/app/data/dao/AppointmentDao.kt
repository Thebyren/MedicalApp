package com.medical.app.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Relation
import com.medical.app.data.entities.Appointment
import com.medical.app.data.entities.Paciente
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface AppointmentDao : BaseDao<Appointment> {
    @Query("SELECT * FROM appointments WHERE patientId = :patientId AND dateTime >= :startDate ORDER BY dateTime ASC")
    fun getUpcomingAppointments(patientId: Long, startDate: Date = Date()): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Long): Appointment?

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM appointments WHERE patientId = :patientId AND dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime ASC")
    fun getAppointmentsInRange(patientId: Long, startDate: Date, endDate: Date): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime ASC")
    fun getAppointmentsForDateRange(startDate: Date, endDate: Date): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointmentsList(): List<Appointment>
    
    @Query("""
        SELECT a.*, p.nombre as patientName, p.apellidos as patientLastName
        FROM appointments a
        LEFT JOIN pacientes p ON a.patientId = p.id
        WHERE a.dateTime BETWEEN :startDate AND :endDate
        ORDER BY a.dateTime ASC
    """)
    fun getAppointmentsWithPatientForDateRange(startDate: Date, endDate: Date): Flow<List<AppointmentWithPatient>>
}

data class AppointmentWithPatient(
    @Embedded val appointment: Appointment,
    val patientName: String?,
    val patientLastName: String?
) {
    val fullPatientName: String
        get() = if (patientName != null && patientLastName != null) {
            "$patientName $patientLastName"
        } else {
            "Paciente desconocido"
        }
}
