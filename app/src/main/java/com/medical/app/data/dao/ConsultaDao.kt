package com.medical.app.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.medical.app.data.entities.Consulta
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ConsultaDao : BaseDao<Consulta> {
    @Query("SELECT * FROM consultas WHERE id = :id")
    suspend fun getConsultaById(id: Long): Consulta?

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.pacienteId = :patientId
        ORDER BY c.fechaConsulta DESC
    """)
    fun getConsultasByPatient(patientId: Long): Flow<List<Consulta>>

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.medicoId = :medicoId
        ORDER BY c.fechaConsulta DESC
    """)
    fun getConsultasPorMedico(medicoId: Long): Flow<List<Consulta>>

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.pacienteId = :pacienteId 
        AND date(c.fechaConsulta/1000, 'unixepoch') = date('now')
        ORDER BY c.fechaConsulta DESC
    """)
    fun getConsultasHoyPorPaciente(pacienteId: Long): Flow<List<Consulta>>

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.medicoId = :medicoId 
        AND date(c.fechaConsulta/1000, 'unixepoch') = date('now')
        ORDER BY c.fechaConsulta
    """)
    fun getCitasHoyPorMedico(medicoId: Long): Flow<List<Consulta>>

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.proximaCita IS NOT NULL 
        AND c.pacienteId = :pacienteId
        AND c.proximaCita >= :hoy
        ORDER BY c.proximaCita ASC
        LIMIT 1
    """)
    suspend fun getProximaCita(pacienteId: Long, hoy: Date = Date()): Consulta?

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.medicoId = :medicoId
        AND c.fechaConsulta BETWEEN :desde AND :hasta
        ORDER BY c.fechaConsulta
    """)
    fun getConsultasPorMedicoYRango(
        medicoId: Long,
        desde: Date,
        hasta: Date
    ): Flow<List<Consulta>>

    @Query("DELETE FROM consultas WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM consultas ORDER BY fechaConsulta DESC")
    fun getConsultasPagingSource(): PagingSource<Int, Consulta>
    
    @Query("SELECT * FROM consultas")
    suspend fun getAllConsultasList(): List<Consulta>
}
