package com.medical.app.data.dao

import androidx.room.*
import com.medical.app.data.entities.Medico
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicoDao : BaseDao<Medico> {
    @Query("SELECT * FROM medicos WHERE id = :id")
    suspend fun getMedicoById(id: Int): Medico?

    @Query("SELECT * FROM medicos WHERE usuarioId = :usuarioId")
    suspend fun getMedicoByUsuarioId(usuarioId: Int): Medico?

    @Query("SELECT * FROM medicos WHERE numeroColegiado = :numeroColegiado")
    suspend fun getMedicoByNumeroColegiado(numeroColegiado: String): Medico?

    @Query("SELECT m.* FROM medicos m " +
           "JOIN medico_paciente mp ON m.id = mp.medicoId " +
           "WHERE mp.pacienteId = :pacienteId AND mp.activo = 1")
    fun getMedicosPorPaciente(pacienteId: Int): Flow<List<Medico>>

    @Query("SELECT * FROM medicos WHERE especialidad = :especialidad")
    fun getMedicosPorEspecialidad(especialidad: String): Flow<List<Medico>>

    @Query("SELECT * FROM medicos")
    fun getAllMedicos(): Flow<List<Medico>>

    @Query("SELECT COUNT(*) FROM medicos WHERE numeroColegiado = :numeroColegiado AND id != :excludeId")
    suspend fun existeNumeroColegiado(numeroColegiado: String, excludeId: Int = 0): Int
}
