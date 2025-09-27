package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.medical.app.data.entities.enums.Genero
import com.medical.app.data.model.Patient
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
    indices = [Index(value = ["usuarioId"], unique = true)]
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
    val telefonoEmergencia: String? = null,
    val email: String = "",
    val bloodType: String = "",
    val allergies: String = "",
    val notes: String = ""
)

fun Paciente.toModel() = Patient(
    id = this.id,
    name = this.nombre,
    lastName = this.apellidos,
    dni = this.numeroSeguridadSocial ?: "",
    birthdate = this.fechaNacimiento,
    gender = this.genero?.name ?: "",
    phone = this.telefono ?: "",
    address = this.direccion ?: "",
    email = this.email,
    bloodType = this.bloodType,
    allergies = this.allergies,
    notes = this.notes,
)
