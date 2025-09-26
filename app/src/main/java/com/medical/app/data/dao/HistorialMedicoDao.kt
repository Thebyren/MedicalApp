package com.medical.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.medical.app.data.entities.HistorialMedico
import com.medical.app.data.entities.enums.TipoRegistroHistorial

@Dao
interface HistorialMedicoDao : BaseDao<HistorialMedico> {
    @Query("SELECT * FROM historial_medico WHERE id = :id")
    suspend fun getRegistroById(id: Int): HistorialMedico?

    @Query("""
        SELECT * FROM historial_medico 
        WHERE paciente_id = :pacienteId 
        AND activo = 1
        ORDER BY fecha_registro DESC
    """)
    fun getHistorialCompleto(pacienteId: Int): LiveData<List<HistorialMedico>>

    @Query("""
        SELECT * FROM historial_medico 
        WHERE paciente_id = :pacienteId 
        AND tipo_registro = :tipoRegistro
        AND activo = 1
        ORDER BY fecha_registro DESC
    """)
    fun getHistorialPorTipo(
        pacienteId: Int,
        tipoRegistro: TipoRegistroHistorial
    ): LiveData<List<HistorialMedico>>

    @Query("""
        SELECT DISTINCT tipo_registro 
        FROM historial_medico 
        WHERE paciente_id = :pacienteId
        AND activo = 1
    """)
    fun getTiposRegistroActivos(pacienteId: Int): LiveData<List<String>>

    @Query("""
        UPDATE historial_medico 
        SET activo = 0 
        WHERE id = :registroId
    """)
    suspend fun desactivarRegistro(registroId: Int): Int

    @Query("""
        SELECT * FROM historial_medico
        WHERE paciente_id = :pacienteId
        AND (descripcion LIKE '%' || :query || '%')
        AND activo = 1
        ORDER BY fecha_registro DESC
    """)
    fun buscarEnHistorial(
        pacienteId: Int,
        query: String
    ): LiveData<List<HistorialMedico>>
}
