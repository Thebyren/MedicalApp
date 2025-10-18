package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "daily_income",
    indices = [
        Index(value = ["doctorId", "date"], unique = true)
    ]
)
data class DailyIncome(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val doctorId: Long,
    val date: Date, // Solo la fecha (sin hora)
    val totalIncome: Double = 0.0, // Total de ingresos del día
    val completedAppointments: Int = 0, // Número de citas completadas
    val updatedAt: Date = Date()
)
