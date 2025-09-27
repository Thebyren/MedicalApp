package com.medical.app.data.repository

import com.medical.app.data.dao.UsuarioDao
import com.medical.app.data.entities.Usuario
import com.medical.app.util.security.PasswordHasher
import com.medical.app.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

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

    fun getUsuariosPorTipo(tipo: String): Flow<Result<List<Usuario>>> = flow {
        emit(Result.loading())
        try {
            val usuarios = usuarioDao.getUsuariosPorTipo(tipo)
            emit(Result.success(usuarios) as Result<List<Usuario>>)
        } catch (e: Exception) {
            emit(Result.error(e))
        }
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
