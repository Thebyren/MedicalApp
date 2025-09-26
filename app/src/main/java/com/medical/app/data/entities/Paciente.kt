package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.medical.app.data.entities.enums.Genero
import java.util.Date

@Entity(
    tableName = "pacientes",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId", unique = true)]
)
data class Paciente(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val apellidos: String,
    val fechaNacimiento: Date,
    val genero: Genero? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val numeroSeguridadSocial: String? = null,
    val contactoEmergencia: String? = null,
    val telefonoEmergencia: String? = null
) {
    // Constructor secundario para crear un paciente sin ID
    constructor(
        usuarioId: Int,
        nombre: String,
        apellidos: String,
        fechaNacimiento: Date,
        genero: Genero? = null,
        telefono: String? = null,
        direccion: String? = null,
        numeroSeguridadSocial: String? = null,
        contactoEmergencia: String? = null,
        telefonoEmergencia: String? = null
    ) : this(0, usuarioId, nombre, apellidos, fechaNacimiento, genero, telefono, 
            direccion, numeroSeguridadSocial, contactoEmergencia, telefonoEmergencia)
}
