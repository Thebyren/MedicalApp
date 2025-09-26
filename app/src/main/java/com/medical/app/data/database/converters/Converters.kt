package com.medical.app.data.database.converters

import androidx.room.TypeConverter
import com.medical.app.data.entities.enums.Genero
import com.medical.app.data.entities.enums.TipoRegistroHistorial
import com.medical.app.data.entities.enums.TipoUsuario
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTipoUsuario(value: String?): TipoUsuario? {
        return value?.let { TipoUsuario.fromString(it) }
    }

    @TypeConverter
    fun tipoUsuarioToString(tipo: TipoUsuario?): String? {
        return tipo?.name
    }

    @TypeConverter
    fun fromGenero(value: String?): Genero? {
        return value?.let { Genero.fromString(it) }
    }

    @TypeConverter
    fun generoToString(genero: Genero?): String? {
        return genero?.value
    }

    @TypeConverter
    fun fromTipoRegistroHistorial(value: String?): TipoRegistroHistorial? {
        return value?.let { TipoRegistroHistorial.fromString(it) }
    }

    @TypeConverter
    fun tipoRegistroHistorialToString(tipo: TipoRegistroHistorial?): String? {
        return tipo?.name
    }
}
