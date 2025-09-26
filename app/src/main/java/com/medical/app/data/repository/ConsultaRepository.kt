package com.medical.app.data.repository

import com.medical.app.data.local.dao.ConsultaDao
import com.medical.app.data.model.Consulta
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsultaRepository @Inject constructor(
    private val consultaDao: ConsultaDao
) {
    fun getConsultasByPatient(patientId: Long): Flow<List<Consulta>> {
        return consultaDao.getConsultasByPatient(patientId)
    }

    suspend fun getConsultaById(id: Long): Consulta? {
        return consultaDao.getConsultaById(id)
    }

    suspend fun insertConsulta(consulta: Consulta): Long {
        return consultaDao.insert(consulta)
    }

    suspend fun updateConsulta(consulta: Consulta) {
        consultaDao.update(consulta)
    }

    suspend fun deleteConsulta(consulta: Consulta) {
        consultaDao.delete(consulta)
    }

    suspend fun deleteConsultaById(id: Long) {
        consultaDao.deleteById(id)
    }
}
