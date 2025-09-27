package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.medical.app.data.entities.enums.TipoRegistroHistorial
import java.util.Date

@Entity(
    tableName = "historial_medico",
    foreignKeys = [
        ForeignKey(
            entity = Paciente::class,
            parentColumns = ["id"],
            childColumns = ["pacienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pacienteId"])]
)
data class HistorialMedico(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pacienteId: Int,
    val tipoRegistro: TipoRegistroHistorial,
    val descripcion: String,
    val fechaRegistro: Date = Date(),
    val activo: Boolean = true
) {
    // Constructor secundario para crear un registro de historial sin ID
    constructor(
        pacienteId: Int,
        tipoRegistro: TipoRegistroHistorial,
        descripcion: String,
        activo: Boolean = true
    ) : this(0, pacienteId, tipoRegistro, descripcion, Date(), activo)
}
