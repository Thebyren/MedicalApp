package com.medical.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.medical.app.data.entities.Paciente

@Dao
interface PacienteDao : BaseDao<Paciente> {
    @Query("SELECT * FROM pacientes WHERE id = :id")
    suspend fun getPacienteById(id: Int): Paciente?

    @Query("SELECT * FROM pacientes WHERE usuario_id = :usuarioId")
    suspend fun getPacienteByUsuarioId(usuarioId: Int): Paciente?

    @Query("SELECT p.* FROM pacientes p " +
           "JOIN medico_paciente mp ON p.id = mp.paciente_id " +
           "WHERE mp.medico_id = :medicoId AND mp.activo = 1")
    fun getPacientesPorMedico(medicoId: Int): LiveData<List<Paciente>>

    @Query("SELECT * FROM pacientes WHERE numero_seguridad_social = :numeroSeguridadSocial")
    suspend fun getPacientePorSeguroSocial(numeroSeguridadSocial: String): Paciente?

    @Query("SELECT * FROM pacientes")
    fun getAllPacientes(): LiveData<List<Paciente>>

    @Query("SELECT COUNT(*) FROM pacientes WHERE numero_seguridad_social = :numeroSeguridadSocial AND id != :excludeId")
    suspend fun existeNumeroSeguridadSocial(numeroSeguridadSocial: String, excludeId: Int = 0): Int

    @Query("""
        SELECT p.* FROM pacientes p
        WHERE p.nombre LIKE '%' || :query || '%' 
        OR p.apellidos LIKE '%' || :query || '%' 
        OR p.numero_seguridad_social LIKE '%' || :query || '%'
    """)
    suspend fun buscarPacientes(query: String): List<Paciente>
}
