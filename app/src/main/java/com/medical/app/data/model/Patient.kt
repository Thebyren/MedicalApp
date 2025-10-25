package com.medical.app.data.model

import com.medical.app.data.entities.Paciente
import com.medical.app.data.entities.enums.Genero
import java.util.Date

data class Patient(
    val id: Long = 0,
    val name: String,
    val lastName: String,
    val dni: String,
    val birthdate: Date,
    val gender: String,
    val phone: String,
    val address: String,
    val email: String,
    val bloodType: String,
    val allergies: String,
    val notes: String
)

fun Patient.toEntity(userId: Int) = Paciente(
    id = this.id,
    usuarioId = userId, // This needs to be provided when converting back
    nombre = this.name,
    apellidos = this.lastName,
    fechaNacimiento = this.birthdate,
    genero = when (this.gender.uppercase()) {
        "MASCULINO", "MALE", "M" -> Genero.MASCULINO
        "FEMENINO", "FEMALE", "F" -> Genero.FEMENINO
        "OTRO", "OTHER", "O" -> Genero.OTRO
        else -> try {
            Genero.valueOf(this.gender.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    },
    telefono = this.phone,
    direccion = this.address,
    numeroSeguridadSocial = this.dni,
    email = this.email,
    bloodType = this.bloodType,
    allergies = this.allergies,
    notes = this.notes
)
