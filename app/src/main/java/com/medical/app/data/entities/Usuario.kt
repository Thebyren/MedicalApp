package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.medical.app.data.entities.enums.TipoUsuario
import java.util.Date

/**
 * Entidad que representa un usuario en el sistema.
 * @property id Identificador único del usuario (autogenerado)
 * @property nombreCompleto Nombre completo del usuario
 * @property email Correo electrónico del usuario (debe ser único)
 * @property passwordHash Hash de la contraseña del usuario (con sal)
 * @property salt Sal utilizada para el hash de la contraseña
 * @property tipoUsuario Tipo de usuario (MEDICO o PACIENTE)
 * @property fechaCreacion Fecha de creación del usuario
 * @property activo Indica si el usuario está activo en el sistema
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombreCompleto: String,
    val email: String,
    val passwordHash: String,
    val salt: String? = null,
    val tipoUsuario: TipoUsuario,
    val fechaCreacion: Date = Date(),
    val activo: Boolean = true
) {
    /**
     * Constructor secundario para crear un usuario sin ID (útil para inserciones).
     * @param nombreCompleto Nombre completo del usuario
     * @param email Correo electrónico del usuario
     * @param passwordHash Hash de la contraseña del usuario (con sal)
     * @param salt Sal utilizada para el hash de la contraseña
     * @param tipoUsuario Tipo de usuario (MEDICO o PACIENTE)
     * @param activo Indica si el usuario está activo (por defecto true)
     */
    constructor(
        nombreCompleto: String,
        email: String,
        passwordHash: String,
        salt: String? = null,
        tipoUsuario: TipoUsuario,
        activo: Boolean = true
    ) : this(0, nombreCompleto, email, passwordHash, salt, tipoUsuario, Date(), activo)
}
