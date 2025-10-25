package com.medical.app.data.sync.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs para comunicación con Supabase
 * Todos los modelos deben ser @Serializable para trabajar con Supabase
 */

// =================== USUARIOS ===================

@Serializable
data class UsuarioDto(
    val id: Long? = null,
    @SerialName("nombre_completo")
    val nombreCompleto: String,
    val email: String,
    @SerialName("password_hash")
    val passwordHash: String,
    val salt: String? = null,
    @SerialName("tipo_usuario")
    val tipoUsuario: String, // "MEDICO" o "PACIENTE"
    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null,
    val activo: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== MEDICOS ===================

@Serializable
data class MedicoDto(
    val id: Long? = null,
    @SerialName("usuario_id")
    val usuarioId: Long,
    val nombre: String,
    val apellidos: String,
    val especialidad: String,
    @SerialName("numero_colegiado")
    val numeroColegiado: String,
    val telefono: String? = null,
    @SerialName("hospital_clinica")
    val hospitalClinica: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== PACIENTES ===================

@Serializable
data class PacienteDto(
    val id: Long? = null,
    @SerialName("usuario_id")
    val usuarioId: Long?,
    val nombre: String,
    val apellidos: String,
    @SerialName("fecha_nacimiento")
    val fechaNacimiento: String, // ISO 8601 format
    val genero: String? = null, // "MASCULINO", "FEMENINO", "OTRO"
    val telefono: String? = null,
    val direccion: String? = null,
    @SerialName("numero_seguridad_social")
    val numeroSeguridadSocial: String? = null,
    @SerialName("contacto_emergencia")
    val contactoEmergencia: String? = null,
    @SerialName("telefono_emergencia")
    val telefonoEmergencia: String? = null,
    val email: String? = "",
    @SerialName("blood_type")
    val bloodType: String? = "",
    val allergies: String? = "",
    val notes: String? = "",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== MEDICO_PACIENTE ===================

@Serializable
data class MedicoPacienteDto(
    @SerialName("medico_id")
    val medicoId: Long,
    @SerialName("paciente_id")
    val pacienteId: Long,
    @SerialName("fecha_asignacion")
    val fechaAsignacion: String? = null,
    val activo: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== APPOINTMENTS ===================

@Serializable
data class AppointmentDto(
    val id: Long? = null,
    @SerialName("patient_id")
    val patientId: Long,
    @SerialName("doctor_id")
    val doctorId: Long? = null,
    val title: String,
    val description: String? = null,
    @SerialName("date_time")
    val dateTime: String, // ISO 8601 format
    val duration: Int = 30,
    val status: String = "SCHEDULED", // SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    val type: String = "Consulta General",
    val notes: String? = null,
    val cost: Double = 0.0,
    @SerialName("is_paid")
    val isPaid: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== CONSULTAS ===================

@Serializable
data class ConsultaDto(
    val id: Long? = null,
    @SerialName("medico_id")
    val medicoId: Long,
    @SerialName("paciente_id")
    val pacienteId: Long,
    @SerialName("fecha_consulta")
    val fechaConsulta: String, // ISO 8601 format
    @SerialName("motivo_consulta")
    val motivoConsulta: String,
    val diagnostico: String? = null,
    val observaciones: String? = null,
    @SerialName("proxima_cita")
    val proximaCita: String? = null, // ISO 8601 format
    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== TRATAMIENTOS ===================

@Serializable
data class TratamientoDto(
    val id: Long? = null,
    @SerialName("consulta_id")
    val consultaId: Long? = null,
    val medicamento: String,
    val dosis: String,
    val frecuencia: String,
    @SerialName("duracion_dias")
    val duracionDias: Int? = null,
    val indicaciones: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== HISTORIAL_MEDICO ===================

@Serializable
data class HistorialMedicoDto(
    val id: Long? = null,
    @SerialName("paciente_id")
    val pacienteId: Long,
    @SerialName("tipo_registro")
    val tipoRegistro: String, // ALERGIA, ENFERMEDAD_CRONICA, CIRUGIA, ANTECEDENTE_FAMILIAR
    val descripcion: String,
    @SerialName("fecha_registro")
    val fechaRegistro: String? = null,
    val activo: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== DAILY_INCOME ===================

@Serializable
data class DailyIncomeDto(
    val id: Long? = null,
    @SerialName("doctor_id")
    val doctorId: Long,
    val date: String, // ISO 8601 date format
    @SerialName("total_income")
    val totalIncome: Double = 0.0,
    @SerialName("completed_appointments")
    val completedAppointments: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

// =================== SYNC_METADATA ===================

@Serializable
data class SyncMetadataDto(
    val id: Long? = null,
    @SerialName("entity_type")
    val entityType: String,
    @SerialName("local_id")
    val localId: Long,
    @SerialName("supabase_id")
    val supabaseId: String? = null,
    @SerialName("synced_at")
    val syncedAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false,
    @SerialName("needs_sync")
    val needsSync: Boolean = true,
    @SerialName("sync_attempts")
    val syncAttempts: Int = 0,
    @SerialName("last_error")
    val lastError: String? = null,
    @SerialName("last_error_at")
    val lastErrorAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

// =================== RESPONSE WRAPPERS ===================

/**
 * Respuesta genérica de Supabase
 */
@Serializable
data class SupabaseResponse<T>(
    val data: T? = null,
    val error: SupabaseError? = null
)

/**
 * Error de Supabase
 */
@Serializable
data class SupabaseError(
    val message: String,
    val code: String? = null,
    val details: String? = null,
    val hint: String? = null
)
