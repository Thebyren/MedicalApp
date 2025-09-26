package com.medical.app.data.repository

import com.medical.app.data.dao.UsuarioDao
import com.medical.app.data.entities.Usuario
import com.medical.app.data.entities.enums.TipoUsuario
import com.medical.app.util.Result
import com.medical.app.util.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar la autenticación de usuarios.
 * Encapsula la lógica de negocio relacionada con el inicio de sesión y registro.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val passwordHasher: PasswordHasher
) {
    
    /**
     * Inicia sesión con correo electrónico y contraseña.
     * @param email Correo electrónico del usuario
     * @param password Contraseña del usuario (ya hasheada)
     * @return Resultado con el usuario si las credenciales son correctas, o un error
     */
    suspend fun login(email: String, password: String): Result<Usuario> = withContext(Dispatchers.IO) {
        try {
            val usuario = usuarioDao.getUsuarioByEmail(email)
            
            if (usuario == null) {
                return@withContext Result.Error(Exception("Credenciales inválidas"))
            }
            
            // Verificar la contraseña
            val isPasswordValid = passwordHasher.verifyPassword(
                password = password,
                salt = usuario.salt ?: return@withContext Result.Error(Exception("Error en la autenticación")),
                expectedHash = usuario.passwordHash
            )
            
            if (isPasswordValid) {
                Result.Success(usuario)
            } else {
                Result.Error(Exception("Credenciales inválidas"))
            }
        } catch (e: Exception) {
            Result.Error(Exception("Error al intentar iniciar sesión: ${e.message}"))
        }
    }
    
    /**
     * Registra un nuevo usuario en el sistema.
     * @param email Correo electrónico del usuario
     * @param password Contraseña ya hasheada
     * @param fullName Nombre completo del usuario
     * @param tipoUsuario Tipo de usuario (MEDICO o PACIENTE)
     * @return Resultado con el ID del usuario creado o un error
     */
    suspend fun registrarUsuario(
        email: String,
        password: String,
        fullName: String,
        tipoUsuario: TipoUsuario
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Verificar si el correo ya está registrado
            if (usuarioDao.existeEmail(email) > 0) {
                return@withContext Result.Error(Exception("El correo electrónico ya está registrado"))
            }
            
            // Generar hash seguro de la contraseña
            val (salt, hashedPassword) = passwordHasher.hashPassword(password)
            
            val nuevoUsuario = Usuario(
                email = email,
                passwordHash = hashedPassword,
                salt = salt,
                nombreCompleto = fullName,
                tipoUsuario = tipoUsuario
            )
            
            val id = usuarioDao.insert(nuevoUsuario)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    /**
     * Verifica si un correo electrónico ya está registrado.
     * @param email Correo electrónico a verificar
     * @return true si el correo ya está registrado, false en caso contrario
     */
    suspend fun existeEmail(email: String): Boolean {
        return usuarioDao.existeEmail(email) > 0
    }
    
    /**
     * Obtiene un usuario por su ID.
     * @param id ID del usuario a buscar
     * @return Usuario si se encuentra, null en caso contrario
     */
    suspend fun getUsuarioById(id: Int): Usuario? {
        return usuarioDao.getUsuarioById(id)
    }
    
    /**
     * Actualiza la contraseña de un usuario.
     * @param usuarioId ID del usuario
     * @param nuevaPassword Nueva contraseña ya hasheada
     * @return true si se actualizó correctamente, false en caso contrario
     */
    suspend fun actualizarPassword(usuarioId: Int, nuevaPassword: String): Boolean {
        val usuario = getUsuarioById(usuarioId) ?: return false
        val usuarioActualizado = usuario.copy(passwordHash = nuevaPassword)
        return usuarioDao.update(usuarioActualizado) > 0
    }
    
    /**
     * Obtiene todos los usuarios de un tipo específico.
     * @param tipoUsuario Tipo de usuario a filtrar
     * @return Flow con la lista de usuarios
     */
    fun getUsuariosPorTipo(tipoUsuario: TipoUsuario): Flow<List<Usuario>> {
        return usuarioDao.getUsuariosPorTipo(tipoUsuario)
    }
    
    /**
     * Desactiva una cuenta de usuario.
     * @param usuarioId ID del usuario a desactivar
     * @return true si se desactivó correctamente, false en caso contrario
     */
    suspend fun desactivarCuenta(usuarioId: Int): Boolean {
        return usuarioDao.actualizarEstado(usuarioId, false) > 0
    }
}
