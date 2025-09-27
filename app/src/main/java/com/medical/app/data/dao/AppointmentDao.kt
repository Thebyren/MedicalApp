package com.medical.app.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.medical.app.data.entities.Appointment
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

    @Query("SELECT * FROM appointments WHERE dateTime BETWEEN :startDate AND :endDate")
    fun getAppointmentsForDateRange(startDate: Date, endDate: Date): Flow<List<Appointment>>

}
