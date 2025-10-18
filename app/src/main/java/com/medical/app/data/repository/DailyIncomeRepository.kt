package com.medical.app.data.repository

import com.medical.app.data.dao.DailyIncomeDao
import com.medical.app.data.entities.DailyIncome
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyIncomeRepository @Inject constructor(
    private val dailyIncomeDao: DailyIncomeDao
) {
    
    suspend fun updateDailyIncome(doctorId: Long, date: Date, amount: Double) {
        // Normalizar la fecha (solo día, sin hora)
        val normalizedDate = normalizeDate(date)
        
        // Obtener el registro existente o crear uno nuevo
        val existing = dailyIncomeDao.getByDoctorAndDate(doctorId, normalizedDate)
        
        if (existing != null) {
            // Actualizar el registro existente
            val updated = existing.copy(
                totalIncome = existing.totalIncome + amount,
                completedAppointments = existing.completedAppointments + 1,
                updatedAt = Date()
            )
            dailyIncomeDao.update(updated)
        } else {
            // Crear nuevo registro
            val newIncome = DailyIncome(
                doctorId = doctorId,
                date = normalizedDate,
                totalIncome = amount,
                completedAppointments = 1,
                updatedAt = Date()
            )
            dailyIncomeDao.insert(newIncome)
        }
    }
    
    suspend fun getTodayIncome(doctorId: Long): DailyIncome? {
        val today = normalizeDate(Date())
        return dailyIncomeDao.getByDoctorAndDate(doctorId, today)
    }
    
    fun getAllByDoctor(doctorId: Long): Flow<List<DailyIncome>> {
        return dailyIncomeDao.getAllByDoctor(doctorId)
    }
    
    fun getByDateRange(doctorId: Long, startDate: Date, endDate: Date): Flow<List<DailyIncome>> {
        return dailyIncomeDao.getByDateRange(
            doctorId,
            normalizeDate(startDate),
            normalizeDate(endDate)
        )
    }
    
    suspend fun getTotalIncomeByDateRange(doctorId: Long, startDate: Date, endDate: Date): Double {
        return dailyIncomeDao.getTotalIncomeByDateRange(
            doctorId,
            normalizeDate(startDate),
            normalizeDate(endDate)
        ) ?: 0.0
    }
    
    suspend fun getTotalAppointmentsByDateRange(doctorId: Long, startDate: Date, endDate: Date): Int {
        return dailyIncomeDao.getTotalAppointmentsByDateRange(
            doctorId,
            normalizeDate(startDate),
            normalizeDate(endDate)
        ) ?: 0
    }
    
    fun getRecentIncome(doctorId: Long, limit: Int = 7): Flow<List<DailyIncome>> {
        return dailyIncomeDao.getRecentIncome(doctorId, limit)
    }
    
    /**
     * Normaliza una fecha para que solo contenga día/mes/año (sin hora)
     */
    private fun normalizeDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
