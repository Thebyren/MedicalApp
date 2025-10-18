package com.medical.app.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.medical.app.data.entities.Tratamiento
import kotlinx.coroutines.flow.Flow

@Dao
interface TratamientoDao : BaseDao<Tratamiento> {
    @Query("SELECT * FROM tratamientos WHERE id = :id")
    suspend fun getTratamientoById(id: Int): Tratamiento?

    @Query("SELECT * FROM tratamientos WHERE consultaId = :consultaId ORDER BY id DESC")
    fun getTratamientosPorConsultaAsFlow(consultaId: Int): Flow<List<Tratamiento>>

    @Query("""
        SELECT t.* FROM tratamientos t
        JOIN consultas c ON t.consultaId = c.id
        WHERE c.pacienteId = :pacienteId
        ORDER BY c.fechaConsulta DESC
    """)
    fun getTratamientosPorPacienteAsFlow(pacienteId: Int): Flow<List<Tratamiento>>

    @Query("""
        SELECT t.* FROM tratamientos t
        JOIN consultas c ON t.consultaId = c.id
        WHERE c.pacienteId = :pacienteId
        AND c.fechaConsulta >= :fechaInicio
        AND c.fechaConsulta <= :fechaFin
        ORDER BY c.fechaConsulta DESC
    """)
    fun getTratamientosPorPacienteYRangoAsFlow(
        pacienteId: Int,
        fechaInicio: Long,
        fechaFin: Long
    ): Flow<List<Tratamiento>>
    
    @Query("SELECT * FROM tratamientos ORDER BY id DESC")
    fun getAllTratamientos(): Flow<List<Tratamiento>>
    
    @Query("SELECT * FROM tratamientos WHERE consultaId IS NULL ORDER BY id DESC")
    fun getIndependentPrescriptions(): Flow<List<Tratamiento>>
}
