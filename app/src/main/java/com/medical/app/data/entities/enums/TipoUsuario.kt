package com.medical.app.data.entities.enums

enum class TipoUsuario {
    MEDICO,
    PACIENTE;

    companion object {
        fun fromString(value: String): TipoUsuario {
            return when (value.uppercase()) {
                "MEDICO" -> MEDICO
                "PACIENTE" -> PACIENTE
                else -> throw IllegalArgumentException("Tipo de usuario no válido: $value")
            }
        }
    }
}
