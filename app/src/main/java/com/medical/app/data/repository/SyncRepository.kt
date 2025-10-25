package com.medical.app.data.repository

import android.util.Log
import com.medical.app.data.dao.*
import com.medical.app.data.entities.*
import com.medical.app.data.remote.SupabaseClientProvider
import com.medical.app.data.sync.dto.*
import com.medical.app.data.sync.mapper.EntityMappers.toDto
import com.medical.app.data.sync.mapper.EntityMappers.toEntity
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar la sincronización entre Room y Supabase
 */
@Singleton
class SyncRepository @Inject constructor(
    private val supabaseClient: SupabaseClientProvider,
    private val syncMetadataDao: SyncMetadataDao,
    private val usuarioDao: UsuarioDao,
    private val medicoDao: MedicoDao,
    private val pacienteDao: PacienteDao,
    private val appointmentDao: AppointmentDao,
    private val consultaDao: ConsultaDao,
    private val tratamientoDao: TratamientoDao,
    private val historialMedicoDao: HistorialMedicoDao,
    private val dailyIncomeDao: DailyIncomeDao
) {

    companion object {
        private const val TAG = "SyncRepository"
        private const val SYNC_TIMEOUT_MS = 30000L // 30 segundos de timeout general
        private const val REQUEST_TIMEOUT_MS = 15000L // 15 segundos por petición
    }

    /**
     * Sincroniza todos los datos entre Room y Supabase
     */
    suspend fun syncAll(): SyncResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "=== INICIANDO SINCRONIZACIÓN ===")
        val results = mutableListOf<EntitySyncResult>()

        try {
            // Verificar conexión con Supabase
            Log.d(TAG, "Verificando conexión con Supabase...")
            if (!supabaseClient.isConnected()) {
                Log.e(TAG, "No hay conexión con Supabase")
                return@withContext SyncResult(
                    success = false,
                    error = "No hay conexión con Supabase. Verifica las credenciales.",
                    entityResults = results
                )
            }
            Log.d(TAG, "Conexión con Supabase establecida")

            // Sincronizar cada tipo de entidad con timeout
            withTimeout(SYNC_TIMEOUT_MS) {
                // Sincronizar entidades independientes en paralelo
                val independentSyncs = listOf(
                    async { syncUsuarios() },
                    async { syncMedicos() },
                    async { syncPacientes() },
                    async { syncAppointments() }
                )

                // Sincronizar entidades dependientes después de las independientes
                val dependentSyncs = listOf(
                    async { syncConsultas() },
                    async { syncTratamientos() },
                    async { syncHistorialMedico() },
                    async { syncDailyIncome() }
                )

                // Esperar a que terminen las independientes
                independentSyncs.awaitAll().forEach { result ->
                    results.add(result)
                }

                // Esperar a que terminen las dependientes
                dependentSyncs.awaitAll().forEach { result ->
                    results.add(result)
                }
            }

            // Limpiar registros eliminados que ya fueron sincronizados
            syncMetadataDao.cleanupDeletedSynced()

            val hasErrors = results.any { !it.success }

            return@withContext SyncResult(
                success = !hasErrors,
                error = if (hasErrors) "Algunos elementos no pudieron sincronizarse" else null,
                entityResults = results
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout en la sincronización general", e)
            return@withContext SyncResult(
                success = false,
                error = "Timeout: La sincronización tardó demasiado tiempo.",
                entityResults = results
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error general de sincronización: ${e.message}", e)
            return@withContext SyncResult(
                success = false,
                error = "Error general de sincronización: ${e.message}",
                entityResults = results
            )
        }
    }

    /**
     * Sincroniza usuarios
     */
    private suspend fun syncUsuarios(): EntitySyncResult = coroutineScope {
        Log.d(TAG, "Sincronizando USUARIOS...")
        try {
            val entityType = EntityType.USUARIOS.value

            // Descargar usuarios desde Supabase (solo descarga, no subir por seguridad)
            Log.d(TAG, "Descargando usuarios desde Supabase...")

            val remoteUsuarios = withTimeout(REQUEST_TIMEOUT_MS) {
                supabaseClient.client
                    .from("usuarios")
                    .select(Columns.ALL)
                    .decodeList<UsuarioDto>()
            }

            Log.d(TAG, "Descargados ${remoteUsuarios.size} usuarios desde Supabase")
            var downloaded = 0
            var downloadErrors = 0

            remoteUsuarios.forEach { dto ->
                try {
                    Log.d(TAG, "Procesando usuario remoto ID: ${dto.id}, email: ${dto.email}")
                    val localEntity = dto.toEntity()

                    val existingMetadata = dto.id?.let {
                        syncMetadataDao.getByRemoteId(it.toString())
                    }

                    if (existingMetadata == null) {
                        // Verificar si ya existe un usuario con ese ID
                        val existingUsuario = usuarioDao.getUsuarioById(dto.id?.toInt() ?: 0)
                        if (existingUsuario == null) {
                            // Nueva entidad desde Supabase
                            Log.d(TAG, "Insertando nuevo usuario desde Supabase...")
                            val localId = usuarioDao.insert(localEntity)
                            Log.d(TAG, "Usuario insertado con ID local: $localId")

                            syncMetadataDao.insert(
                                SyncMetadata(
                                    entityType = entityType,
                                    localId = localId,
                                    remoteId = dto.id.toString(),
                                    lastSynced = System.currentTimeMillis(),
                                    needsSync = false
                                )
                            )
                            downloaded++
                            Log.d(TAG, "✅ Usuario descargado exitosamente")
                        } else {
                            Log.d(TAG, "Usuario ya existe localmente, omitiendo...")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al descargar usuario ID ${dto.id}: ${e.message}", e)
                    downloadErrors++
                }
            }

            return@coroutineScope EntitySyncResult(
                entityType = entityType,
                success = downloadErrors == 0,
                uploaded = 0, // No subimos usuarios por seguridad
                downloaded = downloaded,
                errors = downloadErrors
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout al sincronizar usuarios", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.USUARIOS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = "Timeout"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de usuarios: ${e.message}", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.USUARIOS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = e.message
            )
        }
    }

    /**
     * Sincroniza médicos
     */
    private suspend fun syncMedicos(): EntitySyncResult = coroutineScope {
        Log.d(TAG, "Sincronizando MÉDICOS...")
        try {
            val entityType = EntityType.MEDICOS.value

            // Descargar médicos desde Supabase
            Log.d(TAG, "Descargando médicos desde Supabase...")

            val remoteMedicos = withTimeout(REQUEST_TIMEOUT_MS) {
                supabaseClient.client
                    .from("medicos")
                    .select(Columns.ALL)
                    .decodeList<MedicoDto>()
            }

            Log.d(TAG, "Descargados ${remoteMedicos.size} médicos desde Supabase")
            var downloaded = 0
            var downloadErrors = 0

            remoteMedicos.forEach { dto ->
                try {
                    Log.d(TAG, "Procesando médico remoto ID: ${dto.id}")
                    val localEntity = dto.toEntity()

                    val existingMetadata = dto.id?.let {
                        syncMetadataDao.getByRemoteId(it.toString())
                    }

                    if (existingMetadata == null) {
                        // Verificar si ya existe un médico con ese ID
                        val existingMedico = medicoDao.getMedicoById(dto.id?.toInt() ?: 0)
                        if (existingMedico == null) {
                            // Nueva entidad desde Supabase
                            Log.d(TAG, "Insertando nuevo médico desde Supabase...")
                            val localId = medicoDao.insert(localEntity)
                            Log.d(TAG, "Médico insertado con ID local: $localId")

                            syncMetadataDao.insert(
                                SyncMetadata(
                                    entityType = entityType,
                                    localId = localId,
                                    remoteId = dto.id.toString(),
                                    lastSynced = System.currentTimeMillis(),
                                    needsSync = false
                                )
                            )
                            downloaded++
                            Log.d(TAG, "✅ Médico descargado exitosamente")
                        } else {
                            Log.d(TAG, "Médico ya existe localmente, omitiendo...")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al descargar médico ID ${dto.id}: ${e.message}", e)
                    downloadErrors++
                }
            }

            return@coroutineScope EntitySyncResult(
                entityType = entityType,
                success = downloadErrors == 0,
                uploaded = 0,
                downloaded = downloaded,
                errors = downloadErrors
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout al sincronizar médicos", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.MEDICOS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = "Timeout"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de médicos: ${e.message}", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.MEDICOS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = e.message
            )
        }
    }

    /**
     * Sincroniza pacientes
     */
    private suspend fun syncPacientes(): EntitySyncResult = coroutineScope {
        Log.d(TAG, "Sincronizando PACIENTES...")
        try {
            val entityType = EntityType.PACIENTES.value

            // 1. Subir cambios locales
            Log.d(TAG, "Buscando cambios locales de pacientes...")
            val localChanges = syncMetadataDao.getUnsyncedByType(entityType)
            Log.d(TAG, "Encontrados ${localChanges.size} pacientes para sincronizar")
            var uploaded = 0
            var uploadErrors = 0

            localChanges.forEach { metadata ->
                Log.d(TAG, "Procesando paciente local ID: ${metadata.localId}")
                try {
                    withTimeout(REQUEST_TIMEOUT_MS) {
                        if (metadata.isDeleted) {
                            // Eliminar en Supabase
                            if (metadata.remoteId != null) {
                                supabaseClient.client
                                    .from("pacientes")
                                    .delete {
                                        filter {
                                            eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                        }
                                    }
                                syncMetadataDao.deleteByEntityAndLocalId(entityType, metadata.localId)
                            }
                        } else {
                            // Obtener entidad local y subir/actualizar
                            val paciente = pacienteDao.getPacienteById(metadata.localId.toInt())
                            if (paciente != null) {
                                val dto = paciente.toDto()

                                if (metadata.remoteId != null) {
                                    // Actualizar en Supabase
                                    supabaseClient.client
                                        .from("pacientes")
                                        .update(dto) {
                                            filter {
                                                eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                            }
                                        }
                                } else {
                                    // Insertar en Supabase
                                    Log.d(TAG, "Insertando nuevo paciente en Supabase: ${dto.nombre} ${dto.apellidos}")
                                    val result = supabaseClient.client
                                        .from("pacientes")
                                        .insert(dto)
                                        .decodeSingle<PacienteDto>()

                                    Log.d(TAG, "Paciente insertado exitosamente con ID remoto: ${result.id}")
                                    // Actualizar remoteId en metadata
                                    metadata.remoteId = result.id.toString()
                                }

                                syncMetadataDao.markAsSynced(
                                    entityType,
                                    metadata.localId,
                                    metadata.remoteId!!
                                )
                                uploaded++
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al sincronizar paciente ${metadata.localId}: ${e.message}", e)
                    syncMetadataDao.updateSyncError(
                        entityType,
                        metadata.localId,
                        e.message ?: "Error desconocido"
                    )
                    uploadErrors++
                }
            }

            // 2. Descargar cambios desde Supabase
            Log.d(TAG, "Descargando pacientes desde Supabase...")
            val remotePacientes = withTimeout(REQUEST_TIMEOUT_MS) {
                supabaseClient.client
                    .from("pacientes")
                    .select(Columns.ALL)
                    .decodeList<PacienteDto>()
            }

            Log.d(TAG, "Descargados ${remotePacientes.size} pacientes desde Supabase")
            var downloaded = 0
            var downloadErrors = 0

            remotePacientes.forEach { dto ->
                try {
                    Log.d(TAG, "Procesando paciente remoto ID: ${dto.id}, nombre: ${dto.nombre} ${dto.apellidos}")
                    val localEntity = dto.toEntity()
                    Log.d(TAG, "Paciente convertido a entidad local")

                    val existingMetadata = dto.id?.let {
                        syncMetadataDao.getByRemoteId(it.toString())
                    }

                    if (existingMetadata == null) {
                        // Nueva entidad desde Supabase
                        Log.d(TAG, "Insertando nuevo paciente desde Supabase...")
                        val localId = pacienteDao.insert(localEntity)
                        Log.d(TAG, "Paciente insertado con ID local: $localId")

                        syncMetadataDao.insert(
                            SyncMetadata(
                                entityType = entityType,
                                localId = localId,
                                remoteId = dto.id.toString(),
                                lastSynced = System.currentTimeMillis(),
                                needsSync = false
                            )
                        )
                        downloaded++
                        Log.d(TAG, "✅ Paciente descargado exitosamente")
                    } else if (!existingMetadata.needsSync) {
                        // Actualizar entidad existente si no hay cambios locales pendientes
                        Log.d(TAG, "Actualizando paciente existente con ID local: ${existingMetadata.localId}")
                        pacienteDao.update(localEntity.copy(id = existingMetadata.localId))
                        syncMetadataDao.markAsSynced(
                            entityType,
                            existingMetadata.localId,
                            dto.id.toString()
                        )
                        downloaded++
                        Log.d(TAG, "✅ Paciente actualizado exitosamente")
                    } else {
                        Log.d(TAG, "⏭️ Paciente omitido: tiene cambios locales pendientes")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al descargar paciente ID ${dto.id}: ${e.message}", e)
                    Log.e(TAG, "DTO problemático: nombre=${dto.nombre}, apellidos=${dto.apellidos}, fechaNacimiento=${dto.fechaNacimiento}")
                    downloadErrors++
                }
            }

            return@coroutineScope EntitySyncResult(
                entityType = entityType,
                success = uploadErrors == 0 && downloadErrors == 0,
                uploaded = uploaded,
                downloaded = downloaded,
                errors = uploadErrors + downloadErrors
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout al sincronizar pacientes", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.PACIENTES.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = "Timeout"
            )
        } catch (e: Exception) {
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.PACIENTES.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = e.message
            )
        }
    }

    /**
     * Sincroniza appointments (citas)
     */
    private suspend fun syncAppointments(): EntitySyncResult = coroutineScope {
        try {
            val entityType = EntityType.APPOINTMENTS.value

            // Similar a syncPacientes pero con appointments
            val localChanges = syncMetadataDao.getUnsyncedByType(entityType)
            var uploaded = 0
            var uploadErrors = 0

            localChanges.forEach { metadata ->
                try {
                    withTimeout(REQUEST_TIMEOUT_MS) {
                        if (metadata.isDeleted) {
                            if (metadata.remoteId != null) {
                                supabaseClient.client
                                    .from("appointments")
                                    .delete {
                                        filter {
                                            eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                        }
                                    }
                                syncMetadataDao.deleteByEntityAndLocalId(entityType, metadata.localId)
                            }
                        } else {
                            val appointment = appointmentDao.getAppointmentById(metadata.localId)
                            if (appointment != null) {
                                val dto = appointment.toDto()

                                if (metadata.remoteId != null) {
                                    supabaseClient.client
                                        .from("appointments")
                                        .update(dto) {
                                            filter {
                                                eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                            }
                                        }
                                } else {
                                    val result = supabaseClient.client
                                        .from("appointments")
                                        .insert(dto)
                                        .decodeSingle<AppointmentDto>()

                                    metadata.remoteId = result.id.toString()
                                }

                                syncMetadataDao.markAsSynced(
                                    entityType,
                                    metadata.localId,
                                    metadata.remoteId!!
                                )
                                uploaded++
                            }
                        }
                    }
                } catch (e: Exception) {
                    syncMetadataDao.updateSyncError(
                        entityType,
                        metadata.localId,
                        e.message ?: "Error desconocido"
                    )
                    uploadErrors++
                }
            }

            // Descargar cambios desde Supabase
            val remoteAppointments = withTimeout(REQUEST_TIMEOUT_MS) {
                supabaseClient.client
                    .from("appointments")
                    .select(Columns.ALL)
                    .decodeList<AppointmentDto>()
            }

            var downloaded = 0
            var downloadErrors = 0

            remoteAppointments.forEach { dto ->
                try {
                    val localEntity = dto.toEntity()
                    val existingMetadata = dto.id?.let {
                        syncMetadataDao.getByRemoteId(it.toString())
                    }

                    if (existingMetadata == null) {
                        val localId = appointmentDao.insert(localEntity)
                        syncMetadataDao.insert(
                            SyncMetadata(
                                entityType = entityType,
                                localId = localId,
                                remoteId = dto.id.toString(),
                                lastSynced = System.currentTimeMillis(),
                                needsSync = false
                            )
                        )
                        downloaded++
                    } else if (!existingMetadata.needsSync) {
                        appointmentDao.update(localEntity.copy(id = existingMetadata.localId))
                        syncMetadataDao.markAsSynced(
                            entityType,
                            existingMetadata.localId,
                            dto.id.toString()
                        )
                        downloaded++
                    }
                } catch (e: Exception) {
                    downloadErrors++
                }
            }

            return@coroutineScope EntitySyncResult(
                entityType = entityType,
                success = uploadErrors == 0 && downloadErrors == 0,
                uploaded = uploaded,
                downloaded = downloaded,
                errors = uploadErrors + downloadErrors
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout al sincronizar appointments", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.APPOINTMENTS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = "Timeout"
            )
        } catch (e: Exception) {
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.APPOINTMENTS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = e.message
            )
        }
    }

    /**
     * Sincroniza consultas
     * Implementación similar a syncPacientes y syncAppointments
     */
    private suspend fun syncConsultas(): EntitySyncResult = coroutineScope {
        Log.d(TAG, "Sincronizando CONSULTAS...")
        try {
            val entityType = EntityType.CONSULTAS.value
            var uploaded = 0
            var uploadErrors = 0

            // 1. Subir cambios locales
            val localChanges = syncMetadataDao.getUnsyncedByType(entityType)
            Log.d(TAG, "Encontrados ${localChanges.size} consultas para sincronizar")

            localChanges.forEach { metadata ->
                try {
                    withTimeout(REQUEST_TIMEOUT_MS) {
                        if (metadata.isDeleted) {
                            if (metadata.remoteId != null) {
                                supabaseClient.client
                                    .from("consultas")
                                    .delete {
                                        filter {
                                            eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                        }
                                    }
                                syncMetadataDao.deleteByEntityAndLocalId(entityType, metadata.localId)
                            }
                        } else {
                            val consulta = consultaDao.getConsultaById(metadata.localId)
                            if (consulta != null) {
                                val dto = consulta.toDto()

                                if (metadata.remoteId != null) {
                                    supabaseClient.client
                                        .from("consultas")
                                        .update(dto) {
                                            filter {
                                                eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                            }
                                        }
                                } else {
                                    val result = supabaseClient.client
                                        .from("consultas")
                                        .insert(dto)
                                        .decodeSingle<ConsultaDto>()
                                    metadata.remoteId = result.id.toString()
                                }

                                syncMetadataDao.markAsSynced(
                                    entityType,
                                    metadata.localId,
                                    metadata.remoteId!!
                                )
                                uploaded++
                                Log.d(TAG, "✅ Consulta subida exitosamente")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al sincronizar consulta ${metadata.localId}: ${e.message}", e)
                    syncMetadataDao.updateSyncError(
                        entityType,
                        metadata.localId,
                        e.message ?: "Error desconocido"
                    )
                    uploadErrors++
                }
            }

            // 2. Descargar cambios desde Supabase
            Log.d(TAG, "Descargando consultas desde Supabase...")
            val remoteConsultas = withTimeout(REQUEST_TIMEOUT_MS) {
                supabaseClient.client
                    .from("consultas")
                    .select(Columns.ALL)
                    .decodeList<ConsultaDto>()
            }

            Log.d(TAG, "Descargadas ${remoteConsultas.size} consultas desde Supabase")
            var downloaded = 0
            var downloadErrors = 0

            remoteConsultas.forEach { dto ->
                try {
                    val localEntity = dto.toEntity()
                    val existingMetadata = dto.id?.let {
                        syncMetadataDao.getByRemoteId(it.toString())
                    }

                    if (existingMetadata == null) {
                        val localId = consultaDao.insert(localEntity)
                        syncMetadataDao.insert(
                            SyncMetadata(
                                entityType = entityType,
                                localId = localId,
                                remoteId = dto.id.toString(),
                                lastSynced = System.currentTimeMillis(),
                                needsSync = false
                            )
                        )
                        downloaded++
                        Log.d(TAG, "✅ Consulta descargada exitosamente")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al descargar consulta ID ${dto.id}: ${e.message}", e)
                    downloadErrors++
                }
            }

            return@coroutineScope EntitySyncResult(
                entityType = entityType,
                success = uploadErrors == 0 && downloadErrors == 0,
                uploaded = uploaded,
                downloaded = downloaded,
                errors = uploadErrors + downloadErrors
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout al sincronizar consultas", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.CONSULTAS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = "Timeout"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de consultas: ${e.message}", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.CONSULTAS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = e.message
            )
        }
    }

    /**
     * Sincroniza tratamientos
     */
    private suspend fun syncTratamientos(): EntitySyncResult = coroutineScope {
        Log.d(TAG, "Sincronizando TRATAMIENTOS...")
        try {
            val entityType = EntityType.TRATAMIENTOS.value
            var uploaded = 0
            var uploadErrors = 0

            // 1. Subir cambios locales
            val localChanges = syncMetadataDao.getUnsyncedByType(entityType)
            Log.d(TAG, "Encontrados ${localChanges.size} tratamientos para sincronizar")

            localChanges.forEach { metadata ->
                try {
                    withTimeout(REQUEST_TIMEOUT_MS) {
                        if (metadata.isDeleted) {
                            if (metadata.remoteId != null) {
                                supabaseClient.client
                                    .from("tratamientos")
                                    .delete {
                                        filter {
                                            eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                        }
                                    }
                                syncMetadataDao.deleteByEntityAndLocalId(entityType, metadata.localId)
                            }
                        } else {
                            val tratamiento = tratamientoDao.getTratamientoById(metadata.localId.toInt())
                            if (tratamiento != null) {
                                val dto = tratamiento.toDto()

                                if (metadata.remoteId != null) {
                                    supabaseClient.client
                                        .from("tratamientos")
                                        .update(dto) {
                                            filter {
                                                eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                            }
                                        }
                                } else {
                                    val result = supabaseClient.client
                                        .from("tratamientos")
                                        .insert(dto)
                                        .decodeSingle<TratamientoDto>()
                                    metadata.remoteId = result.id.toString()
                                }

                                syncMetadataDao.markAsSynced(
                                    entityType,
                                    metadata.localId,
                                    metadata.remoteId!!
                                )
                                uploaded++
                                Log.d(TAG, "✅ Tratamiento subido exitosamente")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al sincronizar tratamiento ${metadata.localId}: ${e.message}", e)
                    syncMetadataDao.updateSyncError(
                        entityType,
                        metadata.localId,
                        e.message ?: "Error desconocido"
                    )
                    uploadErrors++
                }
            }

            // 2. Descargar cambios desde Supabase
            Log.d(TAG, "Descargando tratamientos desde Supabase...")
            val remoteTratamientos = withTimeout(REQUEST_TIMEOUT_MS) {
                supabaseClient.client
                    .from("tratamientos")
                    .select(Columns.ALL)
                    .decodeList<TratamientoDto>()
            }

            Log.d(TAG, "Descargados ${remoteTratamientos.size} tratamientos desde Supabase")
            var downloaded = 0
            var downloadErrors = 0

            remoteTratamientos.forEach { dto ->
                try {
                    val localEntity = dto.toEntity()
                    val existingMetadata = dto.id?.let {
                        syncMetadataDao.getByRemoteId(it.toString())
                    }

                    if (existingMetadata == null) {
                        val localId = tratamientoDao.insert(localEntity)
                        syncMetadataDao.insert(
                            SyncMetadata(
                                entityType = entityType,
                                localId = localId,
                                remoteId = dto.id.toString(),
                                lastSynced = System.currentTimeMillis(),
                                needsSync = false
                            )
                        )
                        downloaded++
                        Log.d(TAG, "✅ Tratamiento descargado exitosamente")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al descargar tratamiento ID ${dto.id}: ${e.message}", e)
                    downloadErrors++
                }
            }

            return@coroutineScope EntitySyncResult(
                entityType = entityType,
                success = uploadErrors == 0 && downloadErrors == 0,
                uploaded = uploaded,
                downloaded = downloaded,
                errors = uploadErrors + downloadErrors
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout al sincronizar tratamientos", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.TRATAMIENTOS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = "Timeout"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de tratamientos: ${e.message}", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.TRATAMIENTOS.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = e.message
            )
        }
    }

    /**
     * Sincroniza historial médico
     */
    private suspend fun syncHistorialMedico(): EntitySyncResult = coroutineScope {
        Log.d(TAG, "Sincronizando HISTORIAL MÉDICO...")
        try {
            val entityType = EntityType.HISTORIAL_MEDICO.value
            var uploaded = 0
            var uploadErrors = 0

            // 1. Subir cambios locales
            val localChanges = syncMetadataDao.getUnsyncedByType(entityType)
            Log.d(TAG, "Encontrados ${localChanges.size} historiales médicos para sincronizar")

            localChanges.forEach { metadata ->
                try {
                    withTimeout(REQUEST_TIMEOUT_MS) {
                        if (metadata.isDeleted) {
                            if (metadata.remoteId != null) {
                                supabaseClient.client
                                    .from("historial_medico")
                                    .delete {
                                        filter {
                                            eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                        }
                                    }
                                syncMetadataDao.deleteByEntityAndLocalId(entityType, metadata.localId)
                            }
                        } else {
                            val historial = historialMedicoDao.getRegistroById(metadata.localId.toInt())
                            if (historial != null) {
                                val dto = historial.toDto()

                                if (metadata.remoteId != null) {
                                    supabaseClient.client
                                        .from("historial_medico")
                                        .update(dto) {
                                            filter {
                                                eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                            }
                                        }
                                } else {
                                    val result = supabaseClient.client
                                        .from("historial_medico")
                                        .insert(dto)
                                        .decodeSingle<HistorialMedicoDto>()
                                    metadata.remoteId = result.id.toString()
                                }

                                syncMetadataDao.markAsSynced(
                                    entityType,
                                    metadata.localId,
                                    metadata.remoteId!!
                                )
                                uploaded++
                                Log.d(TAG, "✅ Historial médico subido exitosamente")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al sincronizar historial médico ${metadata.localId}: ${e.message}", e)
                    syncMetadataDao.updateSyncError(
                        entityType,
                        metadata.localId,
                        e.message ?: "Error desconocido"
                    )
                    uploadErrors++
                }
            }

            // 2. Descargar cambios desde Supabase
            Log.d(TAG, "Descargando historiales médicos desde Supabase...")
            val remoteHistoriales = withTimeout(REQUEST_TIMEOUT_MS) {
                supabaseClient.client
                    .from("historial_medico")
                    .select(Columns.ALL)
                    .decodeList<HistorialMedicoDto>()
            }

            Log.d(TAG, "Descargados ${remoteHistoriales.size} historiales médicos desde Supabase")
            var downloaded = 0
            var downloadErrors = 0

            remoteHistoriales.forEach { dto ->
                try {
                    val localEntity = dto.toEntity()
                    val existingMetadata = dto.id?.let {
                        syncMetadataDao.getByRemoteId(it.toString())
                    }

                    if (existingMetadata == null) {
                        val localId = historialMedicoDao.insert(localEntity)
                        syncMetadataDao.insert(
                            SyncMetadata(
                                entityType = entityType,
                                localId = localId,
                                remoteId = dto.id.toString(),
                                lastSynced = System.currentTimeMillis(),
                                needsSync = false
                            )
                        )
                        downloaded++
                        Log.d(TAG, "✅ Historial médico descargado exitosamente")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al descargar historial médico ID ${dto.id}: ${e.message}", e)
                    downloadErrors++
                }
            }

            return@coroutineScope EntitySyncResult(
                entityType = entityType,
                success = uploadErrors == 0 && downloadErrors == 0,
                uploaded = uploaded,
                downloaded = downloaded,
                errors = uploadErrors + downloadErrors
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout al sincronizar historial médico", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.HISTORIAL_MEDICO.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = "Timeout"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de historial médico: ${e.message}", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.HISTORIAL_MEDICO.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = e.message
            )
        }
    }

    /**
     * Sincroniza ingresos diarios
     */
    private suspend fun syncDailyIncome(): EntitySyncResult = coroutineScope {
        Log.d(TAG, "Sincronizando INGRESOS DIARIOS...")
        try {
            val entityType = EntityType.DAILY_INCOME.value
            var uploaded = 0
            var uploadErrors = 0

            // 1. Subir cambios locales
            val localChanges = syncMetadataDao.getUnsyncedByType(entityType)
            Log.d(TAG, "Encontrados ${localChanges.size} ingresos diarios para sincronizar")

            localChanges.forEach { metadata ->
                try {
                    withTimeout(REQUEST_TIMEOUT_MS) {
                        if (metadata.isDeleted) {
                            if (metadata.remoteId != null) {
                                supabaseClient.client
                                    .from("daily_income")
                                    .delete {
                                        filter {
                                            eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                        }
                                    }
                                syncMetadataDao.deleteByEntityAndLocalId(entityType, metadata.localId)
                            }
                        } else {
                            // Modificado: Usar getByDoctorAndDate (necesitamos doctorId)
                            val dailyIncomes = dailyIncomeDao.getAllDailyIncomes() // Necesitamos implementar este método
                            val dailyIncome = dailyIncomes.find { it.id == metadata.localId }

                            if (dailyIncome != null) {
                                val dto = dailyIncome.toDto()

                                if (metadata.remoteId != null) {
                                    supabaseClient.client
                                        .from("daily_income")
                                        .update(dto) {
                                            filter {
                                                eq("id", metadata.remoteId!!.toLongOrNull() ?: 0)
                                            }
                                        }
                                } else {
                                    // Aseguramos el tipo genérico
                                    val result = supabaseClient.client
                                        .from("daily_income")
                                        .insert(dto)
                                        .decodeSingle<DailyIncomeDto>() // Asegúrate de importar el DTO
                                    metadata.remoteId = result.id.toString()
                                }

                                syncMetadataDao.markAsSynced(
                                    entityType,
                                    metadata.localId,
                                    metadata.remoteId!!
                                )
                                uploaded++
                                Log.d(TAG, "✅ Ingreso diario subido exitosamente")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al sincronizar ingreso diario ${metadata.localId}: ${e.message}", e)
                    syncMetadataDao.updateSyncError(
                        entityType,
                        metadata.localId,
                        e.message ?: "Error desconocido"
                    )
                    uploadErrors++
                }
            }

            // 2. Descargar cambios desde Supabase
            Log.d(TAG, "Descargando ingresos diarios desde Supabase...")
            val remoteDailyIncomes = withTimeout(REQUEST_TIMEOUT_MS) {
                supabaseClient.client
                    .from("daily_income")
                    .select(Columns.ALL)
                    .decodeList<DailyIncomeDto>() // Asegúrate de importar el DTO
            }

            Log.d(TAG, "Descargados ${remoteDailyIncomes.size} ingresos diarios desde Supabase")
            var downloaded = 0
            var downloadErrors = 0

            remoteDailyIncomes.forEach { dto ->
                try {
                    val localEntity = dto.toEntity()
                    val existingMetadata = dto.id?.let {
                        syncMetadataDao.getByRemoteId(it.toString())
                    }

                    if (existingMetadata == null) {
                        val localId = dailyIncomeDao.insert(localEntity)
                        syncMetadataDao.insert(
                            SyncMetadata(
                                entityType = entityType,
                                localId = localId,
                                remoteId = dto.id.toString(),
                                lastSynced = System.currentTimeMillis(),
                                needsSync = false
                            )
                        )
                        downloaded++
                        Log.d(TAG, "✅ Ingreso diario descargado exitosamente")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al descargar ingreso diario ID ${dto.id}: ${e.message}", e)
                    downloadErrors++
                }
            }

            return@coroutineScope EntitySyncResult(
                entityType = entityType,
                success = uploadErrors == 0 && downloadErrors == 0,
                uploaded = uploaded,
                downloaded = downloaded,
                errors = uploadErrors + downloadErrors
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Timeout al sincronizar ingresos diarios", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.DAILY_INCOME.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = "Timeout"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización de ingresos diarios: ${e.message}", e)
            return@coroutineScope EntitySyncResult(
                entityType = EntityType.DAILY_INCOME.value,
                success = false,
                uploaded = 0,
                downloaded = 0,
                errors = 1,
                errorMessage = e.message
            )
        }
    }

    /**
     * Inicializa metadata para datos existentes que no tienen registro de sincronización
     */
    suspend fun initializeExistingData() = withContext(Dispatchers.IO) {
        Log.d(TAG, "=== INICIALIZANDO METADATA PARA DATOS EXISTENTES ===")
        try {
            // Inicializar pacientes
            val pacientes = pacienteDao.getAllPacientesList()
            Log.d(TAG, "Encontrados ${pacientes.size} pacientes locales")
            var newMetadata = 0

            pacientes.forEach { paciente ->
                val exists = syncMetadataDao.getMetadata(
                    EntityType.PACIENTES.value,
                    paciente.id
                )
                if (exists == null) {
                    Log.d(TAG, "Creando metadata para paciente ID: ${paciente.id} - ${paciente.nombre}")
                    syncMetadataDao.insert(
                        SyncMetadata(
                            entityType = EntityType.PACIENTES.value,
                            localId = paciente.id,
                            remoteId = null,
                            lastSynced = null,
                            needsSync = true
                        )
                    )
                    newMetadata++
                }
            }
            Log.d(TAG, "Creados $newMetadata nuevos registros de metadata para pacientes")

            // Inicializar appointments
            val appointments = appointmentDao.getAllAppointmentsList()
            appointments.forEach { appointment ->
                val exists = syncMetadataDao.getMetadata(
                    EntityType.APPOINTMENTS.value,
                    appointment.id
                )
                if (exists == null) {
                    syncMetadataDao.insert(
                        SyncMetadata(
                            entityType = EntityType.APPOINTMENTS.value,
                            localId = appointment.id,
                            remoteId = null,
                            lastSynced = null,
                            needsSync = true
                        )
                    )
                }
            }

            // Inicializar consultas
            val consultas = consultaDao.getAllConsultasList()
            consultas.forEach { consulta ->
                val exists = syncMetadataDao.getMetadata(
                    EntityType.CONSULTAS.value,
                    consulta.id
                )
                if (exists == null) {
                    syncMetadataDao.insert(
                        SyncMetadata(
                            entityType = EntityType.CONSULTAS.value,
                            localId = consulta.id,
                            remoteId = null,
                            lastSynced = null,
                            needsSync = true
                        )
                    )
                }
            }

            // Inicializar tratamientos
            val tratamientos = tratamientoDao.getAllTratamientosList()
            tratamientos.forEach { tratamiento ->
                val exists = syncMetadataDao.getMetadata(
                    EntityType.TRATAMIENTOS.value,
                    tratamiento.id.toLong()
                )
                if (exists == null) {
                    syncMetadataDao.insert(
                        SyncMetadata(
                            entityType = EntityType.TRATAMIENTOS.value,
                            localId = tratamiento.id.toLong(),
                            remoteId = null,
                            lastSynced = null,
                            needsSync = true
                        )
                    )
                }
            }

            // Inicializar historial médico
            val historiales = historialMedicoDao.getAllHistorialList()
            historiales.forEach { historial ->
                val exists = syncMetadataDao.getMetadata(
                    EntityType.HISTORIAL_MEDICO.value,
                    historial.id.toLong()
                )
                if (exists == null) {
                    syncMetadataDao.insert(
                        SyncMetadata(
                            entityType = EntityType.HISTORIAL_MEDICO.value,
                            localId = historial.id.toLong(),
                            remoteId = null,
                            lastSynced = null,
                            needsSync = true
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Marca una entidad para sincronización
     */
    suspend fun markForSync(entityType: EntityType, localId: Long) {
        syncMetadataDao.upsertForSync(entityType.value, localId)
    }

    /**
     * Observa el número de cambios no sincronizados
     */
    fun observeUnsyncedCount(): Flow<Int> = syncMetadataDao.observeUnsyncedCount()

    /**
     * Obtiene el estado actual de sincronización
     */
    suspend fun getSyncStatus(): SyncStatus = withContext(Dispatchers.IO) {
        val unsyncedCount = syncMetadataDao.countUnsyncedChanges()
        val errorCount = syncMetadataDao.getErrorRecords().size

        SyncStatus(
            hasUnsyncedChanges = unsyncedCount > 0,
            unsyncedCount = unsyncedCount,
            errorCount = errorCount,
            isConnected = supabaseClient.isConnected()
        )
    }

    /**
     * Resetea los intentos de sincronización para un tipo de entidad
     */
    suspend fun resetSyncAttempts(entityType: EntityType) {
        syncMetadataDao.resetSyncAttempts(entityType.value)
    }
}

/**
 * Resultado de sincronización general
 */
data class SyncResult(
    val success: Boolean,
    val error: String? = null,
    val entityResults: List<EntitySyncResult> = emptyList()
)

/**
 * Resultado de sincronización por entidad
 */
data class EntitySyncResult(
    val entityType: String,
    val success: Boolean,
    val uploaded: Int,
    val downloaded: Int,
    val errors: Int,
    val errorMessage: String? = null
)

/**
 * Estado actual de sincronización
 */
data class SyncStatus(
    val hasUnsyncedChanges: Boolean,
    val unsyncedCount: Int,
    val errorCount: Int,
    val isConnected: Boolean
)