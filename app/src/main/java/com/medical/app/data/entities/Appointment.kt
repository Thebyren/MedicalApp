package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.medical.app.data.entities.Medico
import com.medical.app.data.entities.Paciente
import java.util.*

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
    ],
    indices = [
        Index(value = ["patientId"]),
        Index(value = ["doctorId"])
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
    val cost: Double = 0.0, // Costo de la cita
    val isPaid: Boolean = false, // Si ya fue pagada
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
