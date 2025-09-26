package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicos",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId", unique = true), Index("numeroColegiado", unique = true)]
)
data class Medico(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val apellidos: String,
    val especialidad: String,
    val numeroColegiado: String,
    val telefono: String? = null,
    val hospitalClinica: String? = null
) {
    // Constructor secundario para crear un médico sin ID
    constructor(
        usuarioId: Int,
        nombre: String,
        apellidos: String,
        especialidad: String,
        numeroColegiado: String,
        telefono: String? = null,
        hospitalClinica: String? = null
    ) : this(0, usuarioId, nombre, apellidos, especialidad, numeroColegiado, telefono, hospitalClinica)
}
