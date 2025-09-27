package com.medical.app.data.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.medical.app.data.entities.Paciente
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao : BaseDao<Paciente> {
    @Query("SELECT * FROM pacientes WHERE id = :id")
    suspend fun getPacienteById(id: Int): Paciente?

    @Query("SELECT * FROM pacientes WHERE usuarioId = :usuarioId")
    suspend fun getPacienteByUsuarioId(usuarioId: Int): Paciente?

    @Query("SELECT p.* FROM pacientes p " +
           "JOIN medico_paciente mp ON p.id = mp.pacienteId " +
           "WHERE mp.medicoId = :medicoId AND mp.activo = 1")
    fun getPacientesPorMedico(medicoId: Int): Flow<List<Paciente>>

    @Query("SELECT * FROM pacientes WHERE numeroSeguridadSocial = :numeroSeguridadSocial")
    suspend fun getPacientePorSeguroSocial(numeroSeguridadSocial: String): Paciente?

    @Query("SELECT * FROM pacientes")
    fun getAllPacientes(): Flow<List<Paciente>>

    @Query("SELECT COUNT(*) FROM pacientes WHERE numeroSeguridadSocial = :numeroSeguridadSocial AND id != :excludeId")
    suspend fun existeNumeroSeguridadSocial(numeroSeguridadSocial: String, excludeId: Int = 0): Int

    @Query("""
        SELECT * FROM pacientes
        WHERE nombre LIKE '%' || :query || '%'
        OR apellidos LIKE '%' || :query || '%'
        OR numeroSeguridadSocial LIKE '%' || :query || '%'
    """)
    fun getPacientesPagingSource(query: String): PagingSource<Int, Paciente>

}
