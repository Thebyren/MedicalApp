package com.medical.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.medical.app.data.entities.Medico

@Dao
interface MedicoDao : BaseDao<Medico> {
    @Query("SELECT * FROM medicos WHERE id = :id")
    suspend fun getMedicoById(id: Int): Medico?

    @Query("SELECT * FROM medicos WHERE usuario_id = :usuarioId")
    suspend fun getMedicoByUsuarioId(usuarioId: Int): Medico?

    @Query("SELECT * FROM medicos WHERE numero_colegiado = :numeroColegiado")
    suspend fun getMedicoByNumeroColegiado(numeroColegiado: String): Medico?

    @Query("SELECT m.* FROM medicos m " +
           "JOIN medico_paciente mp ON m.id = mp.medico_id " +
           "WHERE mp.paciente_id = :pacienteId AND mp.activo = 1")
    fun getMedicosPorPaciente(pacienteId: Int): LiveData<List<Medico>>

    @Query("SELECT * FROM medicos WHERE especialidad = :especialidad")
    fun getMedicosPorEspecialidad(especialidad: String): LiveData<List<Medico>>

    @Query("SELECT * FROM medicos")
    fun getAllMedicos(): LiveData<List<Medico>>

    @Query("SELECT COUNT(*) FROM medicos WHERE numero_colegiado = :numeroColegiado AND id != :excludeId")
    suspend fun existeNumeroColegiado(numeroColegiado: String, excludeId: Int = 0): Int
}
