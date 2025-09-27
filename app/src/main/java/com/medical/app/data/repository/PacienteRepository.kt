package com.medical.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.medical.app.data.dao.PacienteDao
import com.medical.app.data.entities.Paciente
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar las operaciones relacionadas con los pacientes.
 * Proporciona una API limpia para acceder a los datos de los pacientes.
 */
@Singleton
class PacienteRepository @Inject constructor(
    private val pacienteDao: PacienteDao
) : BaseRepository<Paciente, Int> {

    override suspend fun insert(entity: Paciente): Long {
        return pacienteDao.insert(entity).toLong()
    }

    override suspend fun update(entity: Paciente): Int {
        return pacienteDao.update(entity)
    }

    override suspend fun delete(entity: Paciente): Int {
        return pacienteDao.delete(entity)
    }

    override suspend fun getById(id: Int): Paciente? {
        return pacienteDao.getPacienteById(id)
    }

    override fun getAll(): Flow<List<Paciente>> {
        return pacienteDao.getAllPacientes()
    }

    fun getPacientesPaginados(query: String): Flow<PagingData<Paciente>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { pacienteDao.getPacientesPagingSource(query) }
        ).flow
    }

    // Métodos específicos de Paciente
    
    suspend fun getPacienteByUsuarioId(usuarioId: Int): Paciente? {
        return pacienteDao.getPacienteByUsuarioId(usuarioId)
    }
    
    fun getPacientesPorMedico(medicoId: Int): Flow<List<Paciente>> {
        return pacienteDao.getPacientesPorMedico(medicoId)
    }
    
    suspend fun getPacientePorSeguroSocial(numeroSeguridadSocial: String): Paciente? {
        return pacienteDao.getPacientePorSeguroSocial(numeroSeguridadSocial)
    }
    
    suspend fun existeNumeroSeguridadSocial(numeroSeguridadSocial: String, excludeId: Int = 0): Boolean {
        return pacienteDao.existeNumeroSeguridadSocial(numeroSeguridadSocial, excludeId) > 0
    }
}
