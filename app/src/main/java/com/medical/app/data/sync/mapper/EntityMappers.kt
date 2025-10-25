package com.medical.app.data.sync.mapper

import com.medical.app.data.entities.*
import com.medical.app.data.entities.Appointment.AppointmentStatus
import com.medical.app.data.entities.enums.Genero
import com.medical.app.data.entities.enums.TipoRegistroHistorial
import com.medical.app.data.entities.enums.TipoUsuario
import com.medical.app.data.sync.dto.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mappers para convertir entre entidades locales (Room) y DTOs (Supabase)
 */
object EntityMappers {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    // =================== UTILITIES ===================
    
    private fun Long.toIsoString(): String {
        return dateFormat.format(Date(this))
    }
    
    private fun String.toTimestamp(): Long {
        return try {
            dateFormat.parse(this)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    private fun Date.toIsoDateString(): String {
        return dateOnlyFormat.format(this)
    }
    
    private fun String.toDate(): Date {
        return try {
            dateOnlyFormat.parse(this) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
    
    // =================== USUARIO ===================
    
    fun Usuario.toDto(): UsuarioDto {
        return UsuarioDto(
            id = id.toLong(),
            nombreCompleto = nombreCompleto,
            email = email,
            passwordHash = passwordHash,
            salt = salt,
            tipoUsuario = tipoUsuario.name,
            fechaCreacion = fechaCreacion.time.toIsoString(),
            activo = activo,
            createdAt = null,
            updatedAt = null
        )
    }
    
    fun UsuarioDto.toEntity(): Usuario {
        return Usuario(
            id = (id ?: 0).toInt(),
            nombreCompleto = nombreCompleto,
            email = email,
            passwordHash = passwordHash,
            salt = salt,
            tipoUsuario = TipoUsuario.valueOf(tipoUsuario),
            fechaCreacion = fechaCreacion?.let { Date(it.toTimestamp()) } ?: Date(),
            activo = activo
        )
    }
    
    // =================== MEDICO ===================
    
    fun Medico.toDto(): MedicoDto {
        return MedicoDto(
            id = id.toLong(),
            usuarioId = usuarioId.toLong(),
            nombre = nombre,
            apellidos = apellidos,
            especialidad = especialidad,
            numeroColegiado = numeroColegiado,
            telefono = telefono,
            hospitalClinica = hospitalClinica,
            createdAt = null,
            updatedAt = null
        )
    }
    
    fun MedicoDto.toEntity(): Medico {
        return Medico(
            id = (id ?: 0).toInt(),
            usuarioId = usuarioId.toInt(),
            nombre = nombre,
            apellidos = apellidos,
            especialidad = especialidad,
            numeroColegiado = numeroColegiado,
            telefono = telefono,
            hospitalClinica = hospitalClinica
        )
    }
    
    // =================== PACIENTE ===================
    
    fun Paciente.toDto(): PacienteDto {
        return PacienteDto(
            id = id,
            usuarioId = usuarioId?.toLong(),
            nombre = nombre,
            apellidos = apellidos,
            fechaNacimiento = fechaNacimiento.toIsoDateString(),
            genero = genero?.name,
            telefono = telefono,
            direccion = direccion,
            numeroSeguridadSocial = numeroSeguridadSocial,
            contactoEmergencia = contactoEmergencia,
            telefonoEmergencia = telefonoEmergencia,
            email = email,
            bloodType = bloodType,
            allergies = allergies,
            notes = notes,
            createdAt = null,
            updatedAt = null
        )
    }
    
    fun PacienteDto.toEntity(): Paciente {
        return Paciente(
            id = id ?: 0,
            usuarioId = usuarioId?.toInt(),
            nombre = nombre,
            apellidos = apellidos,
            fechaNacimiento = fechaNacimiento.toDate(),
            genero = genero?.let { Genero.valueOf(it) },
            telefono = telefono,
            direccion = direccion,
            numeroSeguridadSocial = numeroSeguridadSocial,
            contactoEmergencia = contactoEmergencia,
            telefonoEmergencia = telefonoEmergencia,
            email = email ?: "",
            bloodType = bloodType ?: "",
            allergies = allergies ?: "",
            notes = notes ?: ""
        )
    }
    
    // =================== MEDICO_PACIENTE ===================
    
    fun MedicoPaciente.toDto(): MedicoPacienteDto {
        return MedicoPacienteDto(
            medicoId = medicoId.toLong(),
            pacienteId = pacienteId.toLong(),
            fechaAsignacion = fechaAsignacion.time.toIsoString(),
            activo = activo,
            createdAt = null,
            updatedAt = null
        )
    }
    
    fun MedicoPacienteDto.toEntity(): MedicoPaciente {
        return MedicoPaciente(
            medicoId = medicoId.toInt(),
            pacienteId = pacienteId.toInt(),
            fechaAsignacion = fechaAsignacion?.let { Date(it.toTimestamp()) } ?: Date(),
            activo = activo
        )
    }
    
    // =================== APPOINTMENT ===================
    
    fun Appointment.toDto(): AppointmentDto {
        return AppointmentDto(
            id = id,
            patientId = patientId,
            doctorId = doctorId,
            title = title,
            description = description,
            dateTime = dateTime.time.toIsoString(),
            duration = duration,
            status = status.name,
            type = type,
            notes = notes,
            cost = cost,
            isPaid = isPaid,
            createdAt = createdAt.time.toIsoString(),
            updatedAt = updatedAt.time.toIsoString()
        )
    }
    
    fun AppointmentDto.toEntity(): Appointment {
        return Appointment(
            id = id ?: 0,
            patientId = patientId,
            doctorId = doctorId,
            title = title,
            description = description,
            dateTime = Date(dateTime.toTimestamp()),
            duration = duration,
            status = AppointmentStatus.valueOf(status),
            type = type,
            notes = notes,
            cost = cost,
            isPaid = isPaid,
            createdAt = createdAt?.let { Date(it.toTimestamp()) } ?: Date(),
            updatedAt = updatedAt?.let { Date(it.toTimestamp()) } ?: Date()
        )
    }
    
    // =================== CONSULTA ===================
    
    fun Consulta.toDto(): ConsultaDto {
        return ConsultaDto(
            id = id,
            medicoId = medicoId.toLong(),
            pacienteId = pacienteId.toLong(),
            fechaConsulta = fechaConsulta.time.toIsoString(),
            motivoConsulta = motivoConsulta,
            diagnostico = diagnostico,
            observaciones = observaciones,
            proximaCita = proximaCita?.time?.toIsoString(),
            fechaCreacion = fechaCreacion.time.toIsoString(),
            createdAt = null,
            updatedAt = null
        )
    }
    
    fun ConsultaDto.toEntity(): Consulta {
        return Consulta(
            id = id ?: 0,
            medicoId = medicoId.toInt(),
            pacienteId = pacienteId.toInt(),
            fechaConsulta = Date(fechaConsulta.toTimestamp()),
            motivoConsulta = motivoConsulta,
            diagnostico = diagnostico,
            observaciones = observaciones,
            proximaCita = proximaCita?.let { Date(it.toTimestamp()) },
            fechaCreacion = fechaCreacion?.let { Date(it.toTimestamp()) } ?: Date()
        )
    }
    
    // =================== TRATAMIENTO ===================
    
    fun Tratamiento.toDto(): TratamientoDto {
        return TratamientoDto(
            id = id.toLong(),
            consultaId = consultaId?.toLong(),
            medicamento = medicamento,
            dosis = dosis,
            frecuencia = frecuencia,
            duracionDias = duracionDias,
            indicaciones = indicaciones,
            createdAt = null,
            updatedAt = null
        )
    }
    
    fun TratamientoDto.toEntity(): Tratamiento {
        return Tratamiento(
            id = (id ?: 0).toInt(),
            consultaId = consultaId?.toInt(),
            medicamento = medicamento,
            dosis = dosis,
            frecuencia = frecuencia,
            duracionDias = duracionDias,
            indicaciones = indicaciones
        )
    }
    
    // =================== HISTORIAL_MEDICO ===================
    
    fun HistorialMedico.toDto(): HistorialMedicoDto {
        return HistorialMedicoDto(
            id = id.toLong(),
            pacienteId = pacienteId.toLong(),
            tipoRegistro = tipoRegistro.name,
            descripcion = descripcion,
            fechaRegistro = fechaRegistro.time.toIsoString(),
            activo = activo,
            createdAt = null,
            updatedAt = null
        )
    }
    
    fun HistorialMedicoDto.toEntity(): HistorialMedico {
        return HistorialMedico(
            id = (id ?: 0).toInt(),
            pacienteId = pacienteId.toInt(),
            tipoRegistro = TipoRegistroHistorial.valueOf(tipoRegistro),
            descripcion = descripcion,
            fechaRegistro = fechaRegistro?.let { Date(it.toTimestamp()) } ?: Date(),
            activo = activo
        )
    }
    
    // =================== DAILY_INCOME ===================
    
    fun DailyIncome.toDto(): DailyIncomeDto {
        return DailyIncomeDto(
            id = id,
            doctorId = doctorId,
            date = date.toIsoDateString(),
            totalIncome = totalIncome,
            completedAppointments = completedAppointments,
            createdAt = null,
            updatedAt = updatedAt.time.toIsoString()
        )
    }
    
    fun DailyIncomeDto.toEntity(): DailyIncome {
        return DailyIncome(
            id = id ?: 0,
            doctorId = doctorId,
            date = date.toDate(),
            totalIncome = totalIncome,
            completedAppointments = completedAppointments,
            updatedAt = updatedAt?.let { Date(it.toTimestamp()) } ?: Date()
        )
    }
    
    // =================== SYNC_METADATA ===================
    
    fun SyncMetadata.toDto(): SyncMetadataDto {
        return SyncMetadataDto(
            id = id,
            entityType = entityType,
            localId = localId,
            supabaseId = remoteId,
            syncedAt = lastSynced?.toIsoString(),
            updatedAt = updatedAt.toIsoString(),
            isDeleted = isDeleted,
            needsSync = needsSync,
            syncAttempts = syncAttempts,
            lastError = lastError,
            lastErrorAt = lastErrorAt?.toIsoString(),
            createdAt = createdAt.toIsoString()
        )
    }
    
    fun SyncMetadataDto.toEntity(): SyncMetadata {
        return SyncMetadata(
            id = id ?: 0,
            entityType = entityType,
            localId = localId,
            remoteId = supabaseId,
            lastSynced = syncedAt?.toTimestamp(),
            updatedAt = updatedAt?.toTimestamp() ?: System.currentTimeMillis(),
            isDeleted = isDeleted,
            needsSync = needsSync,
            syncAttempts = syncAttempts,
            lastError = lastError,
            lastErrorAt = lastErrorAt?.toTimestamp(),
            createdAt = createdAt?.toTimestamp() ?: System.currentTimeMillis()
        )
    }
}
