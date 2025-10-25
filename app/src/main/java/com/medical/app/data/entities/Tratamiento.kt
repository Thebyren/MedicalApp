package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tratamientos",
    foreignKeys = [
        ForeignKey(
            entity = Consulta::class,
            parentColumns = ["id"],
            childColumns = ["consultaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["consultaId"])]
)
data class Tratamiento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val consultaId: Int? = null, // Nullable para permitir prescripciones independientes
    val medicamento: String,
    val dosis: String,
    val frecuencia: String,
    val duracionDias: Int? = null,
    val indicaciones: String? = null
) {
    // Constructor secundario para crear un tratamiento sin ID
    constructor(
        consultaId: Int?,
        medicamento: String,
        dosis: String,
        frecuencia: String,
        duracionDias: Int? = null,
        indicaciones: String? = null
    ) : this(0, consultaId, medicamento, dosis, frecuencia, duracionDias, indicaciones)
}
