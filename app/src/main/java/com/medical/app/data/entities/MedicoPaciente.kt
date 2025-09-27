package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "medico_paciente",
    primaryKeys = ["medicoId", "pacienteId"],
    foreignKeys = [
        ForeignKey(
            entity = Medico::class,
            parentColumns = ["id"],
            childColumns = ["medicoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("medicoId"),
        Index("pacienteId"),
        Index(value = ["medicoId", "pacienteId"], unique = true)
    ]
)
data class MedicoPaciente(
    val medicoId: Int,
    val pacienteId: Int,
    val fechaAsignacion: Date = Date(),
    val activo: Boolean = true
)
