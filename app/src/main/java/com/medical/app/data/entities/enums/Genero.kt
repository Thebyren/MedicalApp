package com.medical.app.data.entities.enums

enum class Genero(val value: String) {
    MASCULINO("M"),
    FEMENINO("F"),
    OTRO("O");

    companion object {
        fun fromString(value: String): Genero {
            return when (value.uppercase()) {
                "M" -> MASCULINO
                "F" -> FEMENINO
                "O" -> OTRO
                else -> throw IllegalArgumentException("Género no válido: $value")
            }
        }
    }
}
