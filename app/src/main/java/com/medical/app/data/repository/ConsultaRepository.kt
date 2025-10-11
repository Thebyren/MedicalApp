package com.medical.app.data.repository

import com.medical.app.data.dao.ConsultaDao
import com.medical.app.data.entities.Consulta
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsultaRepository @Inject constructor(
    private val consultaDao: ConsultaDao
) {
    fun getConsultasByPatient(pacienteId: Long): Flow<List<Consulta>> {
        return consultaDao.getConsultasByPatient(pacienteId)
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

    fun getConsultasPager(): Flow<PagingData<Consulta>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { consultaDao.getConsultasPagingSource() }
        ).flow
    }
}