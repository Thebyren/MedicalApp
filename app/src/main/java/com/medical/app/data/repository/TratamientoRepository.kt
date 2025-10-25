package com.medical.app.data.repository

import com.medical.app.data.dao.TratamientoDao
import com.medical.app.data.entities.EntityType
import com.medical.app.data.entities.Tratamiento
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TratamientoRepository @Inject constructor(
    private val tratamientoDao: TratamientoDao,
    private val syncRepository: SyncRepository
) {

    suspend fun getById(id: Int): Tratamiento? {
        return tratamientoDao.getTratamientoById(id)
    }

    fun getByConsultaId(consultaId: Int): Flow<List<Tratamiento>> {
        return tratamientoDao.getTratamientosPorConsultaAsFlow(consultaId)
    }

    fun getByPacienteId(pacienteId: Int): Flow<List<Tratamiento>> {
        return tratamientoDao.getTratamientosPorPacienteAsFlow(pacienteId)
    }

    suspend fun insert(tratamiento: Tratamiento): Long {
        val id = tratamientoDao.insert(tratamiento)
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.TRATAMIENTOS, id)
        return id
    }

    suspend fun update(tratamiento: Tratamiento) {
        tratamientoDao.update(tratamiento)
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.TRATAMIENTOS, tratamiento.id.toLong())
    }

    suspend fun delete(tratamiento: Tratamiento) {
        tratamientoDao.delete(tratamiento)
        // Marcar como eliminado para sincronización
        syncRepository.markForSync(EntityType.TRATAMIENTOS, tratamiento.id.toLong())
    }
    
    fun getAllTratamientos(): Flow<List<Tratamiento>> {
        return tratamientoDao.getAllTratamientos()
    }
    
    fun getIndependentPrescriptions(): Flow<List<Tratamiento>> {
        return tratamientoDao.getIndependentPrescriptions()
    }
}
