package com.medical.app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.medical.app.data.model.Patient

@Dao
interface PatientDao {
    // Métodos existentes
    @Query("SELECT * FROM patients WHERE id = :patientId")
    suspend fun getPatientById(patientId: String): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    // Métodos para paginación
    @Query("SELECT * FROM patients ORDER BY firstName, lastName LIMIT :pageSize OFFSET :offset")
    suspend fun getPatientsPaged(offset: Int, pageSize: Int): List<Patient>

    @Query("""
        SELECT * FROM patients 
        WHERE firstName LIKE :query OR lastName LIKE :query 
        ORDER BY firstName, lastName 
        LIMIT :pageSize OFFSET :offset
    """)
    suspend fun searchPatientsPaged(query: String, offset: Int, pageSize: Int): List<Patient>
    
    // Métodos para compatibilidad (pueden ser eliminados después de actualizar todo el código)
    @Query("SELECT * FROM patients ORDER BY firstName, lastName")
    fun getAllPatients(): LiveData<List<Patient>>

    @Query("""
        SELECT * FROM patients 
        WHERE firstName LIKE '%' || :query || '%' 
        OR lastName LIKE '%' || :query || '%'
    """)
    fun searchPatients(query: String): LiveData<List<Patient>>
    
    // PagingSource para Room (opcional, se puede usar en lugar de PagingSource personalizado)
    @Query("SELECT * FROM patients ORDER BY firstName, lastName")
    fun getPatientsPagingSource(): PagingSource<Int, Patient>
}
