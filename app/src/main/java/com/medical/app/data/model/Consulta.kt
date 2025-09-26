package com.medical.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    tableName = "consultas",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Consulta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val fecha: Date,
    val motivo: String,
    val sintomas: String,
    val diagnostico: String,
    val tratamiento: String,
    val notas: String,
    val proximaCita: Date? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
