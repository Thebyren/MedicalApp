package com.medical.app.data.dao

import androidx.room.*
import com.medical.app.data.entities.DailyIncome
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface DailyIncomeDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dailyIncome: DailyIncome): Long
    
    @Update
    suspend fun update(dailyIncome: DailyIncome)
    
    @Query("SELECT * FROM daily_income WHERE doctorId = :doctorId AND date = :date LIMIT 1")
    suspend fun getByDoctorAndDate(doctorId: Long, date: Date): DailyIncome?
    
    @Query("SELECT * FROM daily_income WHERE doctorId = :doctorId ORDER BY date DESC")
    fun getAllByDoctor(doctorId: Long): Flow<List<DailyIncome>>
    
    @Query("SELECT * FROM daily_income WHERE doctorId = :doctorId AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getByDateRange(doctorId: Long, startDate: Date, endDate: Date): Flow<List<DailyIncome>>
    
    @Query("SELECT SUM(totalIncome) FROM daily_income WHERE doctorId = :doctorId AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalIncomeByDateRange(doctorId: Long, startDate: Date, endDate: Date): Double?
    
    @Query("SELECT SUM(completedAppointments) FROM daily_income WHERE doctorId = :doctorId AND date >= :startDate AND date <= :endDate")
    suspend fun getTotalAppointmentsByDateRange(doctorId: Long, startDate: Date, endDate: Date): Int?
    
    @Query("SELECT * FROM daily_income WHERE doctorId = :doctorId ORDER BY date DESC LIMIT :limit")
    fun getRecentIncome(doctorId: Long, limit: Int): Flow<List<DailyIncome>>
}
