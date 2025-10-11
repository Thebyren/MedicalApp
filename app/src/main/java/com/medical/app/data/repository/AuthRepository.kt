package com.medical.app.data.repository

import com.medical.app.data.dao.UsuarioDao
import com.medical.app.data.entities.Usuario
import com.medical.app.util.security.PasswordHasher
import com.medical.app.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class AuthRepository @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val passwordHasher: PasswordHasher
) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    fun login(email: String, contrasena: String): Flow<Result<Usuario>> = flow {
        emit(Result.Loading)
        try {
            Log.d(TAG, "Intentando login para: $email")
            val usuario = usuarioDao.getUsuarioByEmail(email)

            if (usuario != null && usuario.salt != null && passwordHasher.verifyPassword(contrasena, usuario.salt, usuario.passwordHash)) {
                if (usuario.activo) {
                    Log.d(TAG, "Login exitoso para: $email")
                    emit(Result.Success(usuario))
                } else {
                    Log.w(TAG, "Cuenta desactivada: $email")
                    emit(Result.Error(Exception("La cuenta está desactivada.")))
                }
            } else {
                Log.w(TAG, "Credenciales inválidas para: $email")
                emit(Result.Error(Exception("Credenciales inválidas.")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login", e)
            emit(Result.Error(e))
        }
    }

    fun registrar(usuario: Usuario, contrasena: String): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            Log.d(TAG, "Iniciando registro para: ${usuario.email}")

            // Verificar si el email ya existe
            if (existeEmail(usuario.email)) {
                Log.w(TAG, "Email ya existe: ${usuario.email}")
                emit(Result.Error(Exception("El correo electrónico ya está registrado.")))
                return@flow
            }

            // Hashear la contraseña
            val (salt, hash) = passwordHasher.hashPassword(contrasena)
            Log.d(TAG, "Contraseña hasheada exitosamente")

            // Crear usuario con hash
            val usuarioConHash = usuario.copy(
                passwordHash = hash,
                salt = salt,
                activo = true // Asegurar que el usuario está activo por defecto
            )

            // Insertar en la base de datos
            val userId = usuarioDao.insert(usuarioConHash)
            Log.d(TAG, "Usuario insertado con ID: $userId")

            emit(Result.Success(Unit))
            Log.d(TAG, "Registro completado exitosamente para: ${usuario.email}")

        } catch (e: Exception) {
            Log.e(TAG, "Error en registro: ${e.message}", e)
            emit(Result.Error(Exception("Error al registrar usuario: ${e.message}", e)))
        }
    }

    fun getUsuario(id: Int): Flow<Result<Usuario?>> = flow {
        emit(Result.Loading)
        try {
            Log.d(TAG, "Obteniendo usuario con ID: $id")
            val usuario = usuarioDao.getUsuarioById(id)
            emit(Result.Success(usuario))
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener usuario", e)
            emit(Result.Error(e))
        }
    }

    fun getUsuariosPorTipo(tipo: String): Flow<Result<List<Usuario>>> {
        return usuarioDao.getUsuariosPorTipo(tipo)
            .map<List<Usuario>, Result<List<Usuario>>> { usuarios ->
                Log.d(TAG, "Obtenidos ${usuarios.size} usuarios del tipo: $tipo")
                Result.Success(usuarios)
            }
            .onStart {
                Log.d(TAG, "Iniciando búsqueda de usuarios por tipo: $tipo")
                emit(Result.Loading)
            }
            .catch { exception ->
                Log.e(TAG, "Error al obtener usuarios por tipo", exception)
                emit(Result.Error(Exception(exception)))
            }
    }

    suspend fun existeEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val count = usuarioDao.existeEmail(email)
            Log.d(TAG, "Verificación de email '$email': ${if (count > 0) "existe" else "no existe"}")
            return@withContext count > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar email", e)
            throw e
        }
    }

    fun desactivarCuenta(id: Int): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            Log.d(TAG, "Desactivando cuenta con ID: $id")
            usuarioDao.actualizarEstado(id, false)
            emit(Result.Success(Unit))
            Log.d(TAG, "Cuenta desactivada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al desactivar cuenta", e)
            emit(Result.Error(e))
        }
    }
}