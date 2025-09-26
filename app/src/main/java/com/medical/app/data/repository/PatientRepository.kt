package com.medical.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.medical.app.data.local.dao.PatientDao
import com.medical.app.data.model.Patient
import com.medical.app.data.paging.PatientPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio que maneja las operaciones de datos para los pacientes.
 * Incluye soporte para paginación a través de Paging 3.
 */
@Singleton
class PatientRepository @Inject constructor(
    private val patientDao: PatientDao
) {
    // Tamaño de página para la paginación
    companion object {
        private const val PAGE_SIZE = 20
    }
    
    /**
     * Obtiene un paciente por su ID
     * @param patientId ID del paciente a buscar
     * @return El paciente si existe, null en caso contrario
     */
    suspend fun getPatientById(patientId: String): Patient? = patientDao.getPatientById(patientId)
    
    /**
     * Guarda un paciente en la base de datos
     * @param patient Paciente a guardar
     */
    suspend fun savePatient(patient: Patient) {
        if (patient.id.isBlank()) {
            // Si el ID está vacío, es un nuevo paciente
            patientDao.insertPatient(patient.copy(id = java.util.UUID.randomUUID().toString()))
        } else {
            // Si el ID existe, actualizamos el paciente
            patientDao.updatePatient(patient)
        }
    }
    
    /**
     * Elimina un paciente de la base de datos
     * @param patient Paciente a eliminar
     */
    suspend fun deletePatient(patient: Patient) = patientDao.deletePatient(patient)
    
    /**
     * Obtiene los pacientes de forma paginada
     * @param query Término de búsqueda opcional
     * @return Flow con los datos paginados
     */
    fun getPatientsPaged(query: String = ""): Flow<PagingData<Patient>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = PAGE_SIZE * 2
            ),
            pagingSourceFactory = { 
                PatientPagingSource(
                    patientDao = patientDao,
                    query = query
                )
            }
        ).flow
    }
    
    /**
     * Obtiene una página de pacientes
     * @param page Número de página (basado en 0)
     * @param pageSize Tamaño de la página
     * @return Lista de pacientes para la página solicitada
     */
    suspend fun getPatientsPage(page: Int, pageSize: Int): List<Patient> {
        val offset = page * pageSize
        return patientDao.getPatientsPaged(offset, pageSize)
    }
    
    suspend fun searchPatientsPage(query: String, page: Int, pageSize: Int): List<Patient> {
        val offset = page * pageSize
        return patientDao.searchPatientsPaged("%$query%", offset, pageSize)
    }
    
    // Métodos de búsqueda sin paginación (para compatibilidad)
    fun searchPatients(query: String) = patientDao.searchPatients("%$query%")
    fun getAllPatients() = patientDao.getAllPatients()
}
