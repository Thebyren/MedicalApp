package com.medical.app.data.dao

import androidx.room.*
import com.medical.app.data.entities.MedicoPaciente

@Dao
interface MedicoPacienteDao : BaseDao<MedicoPaciente> {
    @Query("SELECT * FROM medico_paciente WHERE medico_id = :medicoId AND paciente_id = :pacienteId")
    suspend fun getRelacion(medicoId: Int, pacienteId: Int): MedicoPaciente?

    @Query("""
        UPDATE medico_paciente 
        SET activo = :activo 
        WHERE medico_id = :medicoId AND paciente_id = :pacienteId
    """)
    suspend fun actualizarEstadoRelacion(
        medicoId: Int,
        pacienteId: Int,
        activo: Boolean
    ): Int

    @Query("""
        SELECT COUNT(*) 
        FROM medico_paciente 
        WHERE medico_id = :medicoId 
        AND paciente_id = :pacienteId
        AND activo = 1
    """)
    suspend fun existeRelacionActiva(medicoId: Int, pacienteId: Int): Int
}
