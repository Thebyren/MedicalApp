package com.medical.app.data.repository

import com.medical.app.data.dao.MedicoDao
import com.medical.app.data.entities.Medico
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar las operaciones relacionadas con los médicos.
 * Actúa como una capa intermedia entre la fuente de datos y el ViewModel.
 */
@Singleton
class MedicoRepository @Inject constructor(
    private val medicoDao: MedicoDao
) : BaseRepository<Medico, Int> {

    override suspend fun insert(medico: Medico): Long {
        return medicoDao.insert(medico)
    }

    override suspend fun update(medico: Medico): Int {
        return medicoDao.update(medico)
    }

    override suspend fun delete(medico: Medico): Int {
        return medicoDao.delete(medico)
    }

    override suspend fun getById(id: Int): Medico? {
        return medicoDao.getMedicoById(id)
    }

    override fun getAll(): Flow<List<Medico>> {
        return medicoDao.getAllMedicos()
    }

    // Métodos específicos de Medico
    
    suspend fun getMedicoByUsuarioId(usuarioId: Int): Medico? {
        return medicoDao.getMedicoByUsuarioId(usuarioId)
    }
    
    suspend fun getMedicoByNumeroColegiado(numeroColegiado: String): Medico? {
        return medicoDao.getMedicoByNumeroColegiado(numeroColegiado)
    }
    
    fun getMedicosPorPaciente(pacienteId: Int): Flow<List<Medico>> {
        return medicoDao.getMedicosPorPaciente(pacienteId)
    }
    
    fun getMedicosPorEspecialidad(especialidad: String): Flow<List<Medico>> {
        return medicoDao.getMedicosPorEspecialidad(especialidad)
    }
    
    suspend fun existeNumeroColegiado(numeroColegiado: String, excludeId: Int = 0): Boolean {
        return medicoDao.existeNumeroColegiado(numeroColegiado, excludeId) > 0
    }
}
