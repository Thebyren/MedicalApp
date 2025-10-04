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

class AuthRepository @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val passwordHasher: PasswordHasher
) {

    fun login(email: String, contrasena: String): Flow<Result<Usuario>> = flow {
        emit(Result.loading())
        try {
            val usuario = usuarioDao.getUsuarioByEmail(email)
            if (usuario != null && usuario.salt != null && passwordHasher.verifyPassword(contrasena, usuario.salt, usuario.passwordHash)) {
                if (usuario.activo) {
                    emit(Result.success(usuario))
                } else {
                    emit(Result.error(Exception("La cuenta está desactivada.")))
                }
            } else {
                emit(Result.error(Exception("Credenciales inválidas.")))
            }
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }

    fun registrar(usuario: Usuario, contrasena: String): Flow<Result<Unit>> = flow {
        emit(Result.loading())
        try {
            val (salt, hash) = passwordHasher.hashPassword(contrasena)
            val usuarioConHash = usuario.copy(passwordHash = hash, salt = salt)
            usuarioDao.insert(usuarioConHash)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }

    fun getUsuario(id: Int): Flow<Result<Usuario?>> = flow {
        emit(Result.loading())
        try {
            val usuario = usuarioDao.getUsuarioById(id)
            emit(Result.success(usuario))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }

    fun getUsuariosPorTipo(tipo: String): Flow<Result<List<Usuario>>> {
        return usuarioDao.getUsuariosPorTipo(tipo)
            .map<List<Usuario>, Result<List<Usuario>>> { usuarios -> Result.Success(usuarios) }
            .onStart { emit(Result.Loading) }
            .catch { exception -> emit(Result.Error(Exception(exception))) }
    }

    suspend fun existeEmail(email: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext usuarioDao.existeEmail(email) > 0
    }

    fun desactivarCuenta(id: Int): Flow<Result<Unit>> = flow {
        emit(Result.loading())
        try {
            usuarioDao.actualizarEstado(id, false)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }
}
