package com.medical.app.data.repository

import com.medical.app.data.dao.PacienteDao
import com.medical.app.data.entities.Paciente
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar las operaciones relacionadas con los pacientes.
 * Proporciona una API limpia para acceder a los datos de los pacientes.
 */
@Singleton
class PacienteRepository @Inject constructor(
    private val pacienteDao: PacienteDao
) : BaseRepository<Paciente, Int> {

    override suspend fun insert(paciente: Paciente): Long {
        return pacienteDao.insert(paciente).toLong()
    }

    override suspend fun update(paciente: Paciente): Int {
        return pacienteDao.update(paciente)
    }

    override suspend fun delete(paciente: Paciente): Int {
        return pacienteDao.delete(paciente)
    }

    override suspend fun getById(id: Int): Paciente? {
        return pacienteDao.getPacienteById(id)
    }

    override fun getAll(): Flow<List<Paciente>> {
        return pacienteDao.getAllPacientes()
    }

    // Métodos específicos de Paciente
    
    suspend fun getPacienteByUsuarioId(usuarioId: Int): Paciente? {
        return pacienteDao.getPacienteByUsuarioId(usuarioId)
    }
    
    fun getPacientesPorMedico(medicoId: Int): Flow<List<Paciente>> {
        return pacienteDao.getPacientesPorMedico(medicoId)
    }
    
    suspend fun getPacientePorSeguroSocial(numeroSeguridadSocial: String): Paciente? {
        return pacienteDao.getPacientePorSeguroSocial(numeroSeguridadSocial)
    }
    
    suspend fun existeNumeroSeguridadSocial(numeroSeguridadSocial: String, excludeId: Int = 0): Boolean {
        return pacienteDao.existeNumeroSeguridadSocial(numeroSeguridadSocial, excludeId) > 0
    }
    
    suspend fun buscarPacientes(query: String): List<Paciente> {
        return pacienteDao.buscarPacientes(query)
    }
}
