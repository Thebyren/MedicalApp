package com.medical.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.medical.app.data.entities.Tratamiento

@Dao
interface TratamientoDao : BaseDao<Tratamiento> {
    @Query("SELECT * FROM tratamientos WHERE id = :id")
    suspend fun getTratamientoById(id: Int): Tratamiento?

    @Query("SELECT * FROM tratamientos WHERE consulta_id = :consultaId ORDER BY id DESC")
    fun getTratamientosPorConsulta(consultaId: Int): LiveData<List<Tratamiento>>

    @Query("""
        SELECT t.* FROM tratamientos t
        JOIN consultas c ON t.consulta_id = c.id
        WHERE c.paciente_id = :pacienteId
        ORDER BY c.fecha_consulta DESC
    """)
    fun getTratamientosPorPaciente(pacienteId: Int): LiveData<List<Tratamiento>>

    @Query("""
        SELECT t.* FROM tratamientos t
        JOIN consultas c ON t.consulta_id = c.id
        WHERE c.paciente_id = :pacienteId
        AND c.fecha_consulta >= :fechaInicio
        AND c.fecha_consulta <= :fechaFin
        ORDER BY c.fecha_consulta DESC
    """)
    fun getTratamientosPorPacienteYRango(
        pacienteId: Int,
        fechaInicio: Long,
        fechaFin: Long
    ): LiveData<List<Tratamiento>>
}
