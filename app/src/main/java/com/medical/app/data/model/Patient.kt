package com.medical.app.data.model

import com.medical.app.data.entities.Paciente
import com.medical.app.data.entities.enums.Genero
import java.util.Date

data class Patient(
    val id: Int = 0,
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
    val notes: String,
    val firstName: String
)

fun Patient.toEntity(userId: Int) = Paciente(
    id = this.id,
    usuarioId = userId, // This needs to be provided when converting back
    nombre = this.name,
    apellidos = this.lastName,
    fechaNacimiento = this.birthdate,
    genero = Genero.valueOf(this.gender),
    telefono = this.phone,
    direccion = this.address,
    numeroSeguridadSocial = this.dni,
    email = this.email,
    bloodType = this.bloodType,
    allergies = this.allergies,
    notes = this.notes
)
