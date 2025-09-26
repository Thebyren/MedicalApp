package com.medical.app.data.dao

import androidx.room.*
import com.medical.app.data.entities.Usuario
import com.medical.app.data.entities.enums.TipoUsuario
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad Usuario.
 * Proporciona métodos para acceder a los datos de los usuarios en la base de datos.
 */
@Dao
interface UsuarioDao : BaseDao<Usuario> {
    
    /**
     * Obtiene un usuario por su ID.
     * @param id ID del usuario a buscar
     * @return Flujo que emite el usuario si se encuentra, o null en caso contrario
     */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun getUsuarioById(id: Int): Flow<Usuario?>
    
    /**
     * Obtiene un usuario por su correo electrónico de forma síncrona.
     * @param email Correo electrónico del usuario a buscar
     * @return Usuario si se encuentra, o null en caso contrario
     */
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun getUsuarioByEmail(email: String): Usuario?
    
    /**
     * Obtiene un flujo del usuario por su correo electrónico.
     * @param email Correo electrónico del usuario a buscar
     * @return Flujo que emite el usuario si se encuentra, o null en caso contrario
     */
    @Query("SELECT * FROM usuarios WHERE email = :email")
    fun observeUsuarioByEmail(email: String): Flow<Usuario?>
    
    /**
     * Verifica si existe un usuario con el correo electrónico especificado.
     * @param email Correo electrónico a verificar
     * @return Número de usuarios con el correo electrónico especificado (0 o 1)
     */
    @Query("SELECT COUNT(*) FROM usuarios WHERE email = :email")
    suspend fun existeEmail(email: String): Int
    
    /**
     * Obtiene todos los usuarios de un tipo específico.
     * @param tipoUsuario Tipo de usuario a filtrar
     * @return Flujo que emite una lista de usuarios del tipo especificado
     */
    @Query("SELECT * FROM usuarios WHERE tipo_usuario = :tipoUsuario AND activo = 1")
    fun getUsuariosByTipo(tipoUsuario: TipoUsuario): Flow<List<Usuario>>
    
    /**
     * Actualiza la contraseña de un usuario.
     * @param id ID del usuario
     * @param newPasswordHash Nuevo hash de la contraseña
     * @param newSalt Nueva sal utilizada para el hash
     * @return Número de filas actualizadas (debería ser 1)
     */
    @Query("UPDATE usuarios SET password_hash = :newPasswordHash, salt = :newSalt WHERE id = :id")
    suspend fun updatePassword(id: Int, newPasswordHash: String, newSalt: String): Int
    
    /**
     * Desactiva un usuario (borrado lógico).
     * @param id ID del usuario a desactivar
     * @return Número de filas actualizadas (debería ser 1)
     */
    @Query("UPDATE usuarios SET activo = 0 WHERE id = :id")
    suspend fun deactivateUser(id: Int): Int
}
