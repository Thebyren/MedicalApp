package com.medical.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.medical.app.data.dao.PacienteDao
import com.medical.app.data.entities.EntityType
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
    private val pacienteDao: PacienteDao,
    private val syncRepository: SyncRepository
) : BaseRepository<Paciente, Int> {

    override suspend fun insert(entity: Paciente): Long {
        val id = pacienteDao.insert(entity).toLong()
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.PACIENTES, id)
        return id
    }

    override suspend fun update(entity: Paciente): Int {
        val result = pacienteDao.update(entity)
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.PACIENTES, entity.id.toLong())
        return result
    }

    override suspend fun delete(entity: Paciente): Int {
        val result = pacienteDao.delete(entity)
        // Marcar como eliminado para sincronización
        syncRepository.markForSync(EntityType.PACIENTES, entity.id.toLong())
        return result
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
