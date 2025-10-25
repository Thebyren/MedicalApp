package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "consultas",
    foreignKeys = [
        ForeignKey(
            entity = Medico::class,
            parentColumns = ["id"],
            childColumns = ["medicoId"]
        ),
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"]
        )
    ],
    indices = [
        Index(value = ["medicoId"]),
        Index(value = ["pacienteId"])
    ]
)
data class Consulta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicoId: Int,
    val pacienteId: Int,
    val fechaConsulta: Date,
    val motivoConsulta: String,
    val diagnostico: String? = null,
    val observaciones: String? = null,
    val proximaCita: Date? = null,
    val fechaCreacion: Date = Date()
) {
    // Constructor secundario para crear una consulta sin ID
    constructor(
        medicoId: Int,
        pacienteId: Int,
        fechaConsulta: Date,
        motivoConsulta: String,
        diagnostico: String? = null,
        observaciones: String? = null,
        proximaCita: Date? = null
    ) : this(0, medicoId, pacienteId, fechaConsulta, motivoConsulta, diagnostico, observaciones, proximaCita, Date())
}
