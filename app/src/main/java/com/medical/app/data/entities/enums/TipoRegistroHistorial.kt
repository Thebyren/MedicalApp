package com.medical.app.data.entities.enums

enum class TipoRegistroHistorial(val descripcion: String) {
    ALERGIA("Alergia"),
    ENFERMEDAD_CRONICA("Enfermedad Crónica"),
    CIRUGIA("Cirugía"),
    ANTECEDENTE_FAMILIAR("Antecedente Familiar");

    companion object {
        fun fromString(value: String): TipoRegistroHistorial {
            return when (value.uppercase()) {
                "ALERGIA" -> ALERGIA
                "ENFERMEDAD_CRONICA" -> ENFERMEDAD_CRONICA
                "CIRUGIA" -> CIRUGIA
                "ANTECEDENTE_FAMILIAR" -> ANTECEDENTE_FAMILIAR
                else -> throw IllegalArgumentException("Tipo de registro de historial no válido: $value")
            }
        }
    }
}
