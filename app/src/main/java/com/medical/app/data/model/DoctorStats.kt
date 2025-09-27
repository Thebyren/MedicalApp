package com.medical.app.data.model

/**
 * Data class to hold statistics for a doctor.
 */
data class DoctorStats(
    val appointmentsToday: Int,
    val totalPatients: Int,
    val monthlyEarnings: Double
)
