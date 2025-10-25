package com.medical.app.data.model

/**
 * Data class to hold statistics for a doctor.
 */
data class DoctorStats(
    val appointmentsToday: Int,
    val totalPatients: Int,
    val monthlyEarnings: Double,
    val dailyEarnings: Double = 0.0,
    val completedAppointmentsToday: Int = 0
)
