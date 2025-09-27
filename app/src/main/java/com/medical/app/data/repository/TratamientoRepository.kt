package com.medical.app.data.repository

import com.medical.app.data.dao.TratamientoDao
import com.medical.app.data.entities.Tratamiento
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TratamientoRepository @Inject constructor(
    private val tratamientoDao: TratamientoDao
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
        return tratamientoDao.insert(tratamiento)
    }

    suspend fun update(tratamiento: Tratamiento) {
        tratamientoDao.update(tratamiento)
    }

    suspend fun delete(tratamiento: Tratamiento) {
        tratamientoDao.delete(tratamiento)
    }
}
