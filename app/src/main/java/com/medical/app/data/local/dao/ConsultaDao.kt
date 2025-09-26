package com.medical.app.data.local.dao

import androidx.room.*
import com.medical.app.data.model.Consulta
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ConsultaDao {
    @Query("SELECT * FROM consultas WHERE patientId = :patientId ORDER BY fecha DESC")
    fun getConsultasByPatient(patientId: Long): Flow<List<Consulta>>

    @Query("SELECT * FROM consultas WHERE id = :id")
    suspend fun getConsultaById(id: Long): Consulta?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(consulta: Consulta): Long

    @Update
    suspend fun update(consulta: Consulta)

    @Delete
    suspend fun delete(consulta: Consulta)

    @Query("DELETE FROM consultas WHERE id = :id")
    suspend fun deleteById(id: Long)
}
