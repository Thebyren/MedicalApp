package com.medical.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para rastrear metadatos de sincronización entre Room y Supabase
 */
@Entity(
    tableName = "sync_metadata",
    indices = [
        Index(value = ["entityType", "localId"], unique = true),
        Index(value = ["entityType"]),
        Index(value = ["needsSync"]),
        Index(value = ["isDeleted"])
    ]
)
data class SyncMetadata(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val entityType: String,        // Ej: "pacientes", "appointments", "consultas"
    val localId: Long,              // ID local en Room
    var remoteId: String? = null,   // ID en Supabase (UUID)

    val lastSynced: Long? = null,   // Timestamp de última sincronización exitosa
    val updatedAt: Long = System.currentTimeMillis(),  // Timestamp de última modificación

    val isDeleted: Boolean = false, // Marca de eliminación
    val needsSync: Boolean = true,   // Requiere sincronización

    val syncAttempts: Int = 0,       // Número de intentos de sincronización
    val lastError: String? = null,   // Último error de sincronización
    val lastErrorAt: Long? = null,   // Timestamp del último error

    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Verifica si el registro necesita sincronización
     */
    fun requiresSync(): Boolean {
        return needsSync || remoteId == null
    }
    
    /**
     * Verifica si ha excedido el límite de reintentos
     */
    fun hasExceededRetryLimit(maxRetries: Int = 5): Boolean {
        return syncAttempts >= maxRetries
    }
    
    /**
     * Actualiza el registro después de una sincronización exitosa
     */
    fun markAsSynced(remoteId: String): SyncMetadata {
        return copy(
            remoteId = remoteId,
            lastSynced = System.currentTimeMillis(),
            needsSync = false,
            syncAttempts = 0,
            lastError = null,
            lastErrorAt = null
        )
    }
    
    /**
     * Marca el registro para sincronización
     */
    fun markForSync(): SyncMetadata {
        return copy(
            needsSync = true,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Actualiza el registro después de un error de sincronización
     */
    fun markSyncError(error: String): SyncMetadata {
        return copy(
            syncAttempts = syncAttempts + 1,
            lastError = error,
            lastErrorAt = System.currentTimeMillis()
        )
    }
}

/**
 * Enum para tipos de entidad
 */
enum class EntityType(val value: String) {
    USUARIOS("usuarios"),
    MEDICOS("medicos"),
    PACIENTES("pacientes"),
    MEDICO_PACIENTE("medico_paciente"),
    APPOINTMENTS("appointments"),
    CONSULTAS("consultas"),
    TRATAMIENTOS("tratamientos"),
    HISTORIAL_MEDICO("historial_medico"),
    DAILY_INCOME("daily_income");
    
    companion object {
        fun fromValue(value: String): EntityType? {
            return values().firstOrNull { it.value == value }
        }
    }
}

/**
 * Estado de sincronización
 */
enum class SyncStatus {
    SYNCED,           // Sincronizado
    PENDING_UPLOAD,   // Pendiente de subir
    PENDING_DOWNLOAD, // Pendiente de descargar
    ERROR,           // Error en sincronización
    CONFLICT         // Conflicto que requiere resolución
}
