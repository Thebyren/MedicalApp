package com.medical.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.medical.app.data.entities.Consulta
import java.util.*

@Dao
interface ConsultaDao : BaseDao<Consulta> {
    @Query("SELECT * FROM consultas WHERE id = :id")
    suspend fun getConsultaById(id: Int): Consulta?

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.paciente_id = :pacienteId
        ORDER BY c.fecha_consulta DESC
    """)
    fun getConsultasPorPaciente(pacienteId: Int): LiveData<List<Consulta>>

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.medico_id = :medicoId
        ORDER BY c.fecha_consulta DESC
    """)
    fun getConsultasPorMedico(medicoId: Int): LiveData<List<Consulta>>

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.paciente_id = :pacienteId 
        AND date(c.fecha_consulta/1000, 'unixepoch') = date('now')
        ORDER BY c.fecha_consulta DESC
    """)
    fun getConsultasHoyPorPaciente(pacienteId: Int): LiveData<List<Consulta>>

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.medico_id = :medicoId 
        AND date(c.fecha_consulta/1000, 'unixepoch') = date('now')
        ORDER BY c.fecha_consulta
    """)
    fun getCitasHoyPorMedico(medicoId: Int): LiveData<List<Consulta>>

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.proxima_cita IS NOT NULL 
        AND c.paciente_id = :pacienteId
        AND c.proxima_cita >= :hoy
        ORDER BY c.proxima_cita ASC
        LIMIT 1
    """)
    suspend fun getProximaCita(pacienteId: Int, hoy: Date = Date()): Consulta?

    @Query("""
        SELECT c.* FROM consultas c
        WHERE c.medico_id = :medicoId
        AND c.fecha_consulta BETWEEN :desde AND :hasta
        ORDER BY c.fecha_consulta
    """)
    fun getConsultasPorMedicoYRango(
        medicoId: Int,
        desde: Date,
        hasta: Date
    ): LiveData<List<Consulta>>
}
