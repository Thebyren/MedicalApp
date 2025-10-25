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
        WHERE pacienteId = :pacienteId 
        AND activo = 1
        ORDER BY FechaRegistro DESC
    """)
    fun getHistorialCompleto(pacienteId: Int): LiveData<List<HistorialMedico>>

    @Query("""
        SELECT * FROM historial_medico 
        WHERE pacienteId = :pacienteId 
        AND tipoRegistro = :tipoRegistro
        AND activo = 1
        ORDER BY FechaRegistro DESC
    """)
    fun getHistorialPorTipo(
        pacienteId: Int,
        tipoRegistro: TipoRegistroHistorial
    ): LiveData<List<HistorialMedico>>

    @Query("""
        SELECT DISTINCT tipoRegistro 
        FROM historial_medico 
        WHERE pacienteId = :pacienteId
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
        WHERE pacienteId = :pacienteId
        AND (descripcion LIKE '%' || :query || '%')
        AND activo = 1
        ORDER BY FechaRegistro DESC
    """)
    fun buscarEnHistorial(
        pacienteId: Int,
        query: String
    ): LiveData<List<HistorialMedico>>
    
    @Query("SELECT * FROM historial_medico")
    suspend fun getAllHistorialList(): List<HistorialMedico>
}
