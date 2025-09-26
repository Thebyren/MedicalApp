package com.medical.app.data.local.dao

import androidx.room.*
import com.medical.app.data.model.Appointment
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE patientId = :patientId AND dateTime >= :startDate ORDER BY dateTime ASC")
    fun getUpcomingAppointments(patientId: Long, startDate: Date = Date()): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Long): Appointment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM appointments WHERE patientId = :patientId AND dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime ASC")
    fun getAppointmentsInRange(patientId: Long, startDate: Date, endDate: Date): Flow<List<Appointment>>
}
