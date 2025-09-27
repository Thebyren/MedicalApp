package com.medical.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*
import com.medical.app.data.entities.Paciente
import com.medical.app.data.entities.Medico

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Medico::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val doctorId: Long? = null,
    val title: String,
    val description: String? = null,
    val dateTime: Date,
    val duration: Int = 30, // en minutos
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val type: String = "Consulta General",
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    enum class AppointmentStatus {
        SCHEDULED,    // Programada
        CONFIRMED,    // Confirmada
        IN_PROGRESS,  // En progreso
        COMPLETED,    // Completada
        CANCELLED,    // Cancelada
        NO_SHOW       // No se presentó
    }
}
