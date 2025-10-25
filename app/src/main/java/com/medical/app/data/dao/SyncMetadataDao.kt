package com.medical.app.data.dao

import androidx.room.*
import com.medical.app.data.entities.SyncMetadata
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de sincronización
 */
@Dao
interface SyncMetadataDao {
    
    // =================== QUERIES ===================
    
    /**
     * Obtiene metadatos de sincronización por tipo de entidad y ID local
     */
    @Query("SELECT * FROM sync_metadata WHERE entityType = :entityType AND localId = :localId LIMIT 1")
    suspend fun getMetadata(entityType: String, localId: Long): SyncMetadata?
    
    /**
     * Obtiene todos los registros que necesitan sincronización
     */
    @Query("SELECT * FROM sync_metadata WHERE needsSync = 1 ORDER BY updatedAt ASC")
    suspend fun getUnsyncedChanges(): List<SyncMetadata>
    
    /**
     * Obtiene registros no sincronizados por tipo de entidad
     */
    @Query("SELECT * FROM sync_metadata WHERE entityType = :entityType AND needsSync = 1")
    suspend fun getUnsyncedByType(entityType: String): List<SyncMetadata>
    
    /**
     * Obtiene registros con errores de sincronización
     */
    @Query("SELECT * FROM sync_metadata WHERE lastError IS NOT NULL ORDER BY lastErrorAt DESC")
    suspend fun getErrorRecords(): List<SyncMetadata>
    
    /**
     * Obtiene registros marcados como eliminados
     */
    @Query("SELECT * FROM sync_metadata WHERE isDeleted = 1 AND needsSync = 1")
    suspend fun getDeletedRecords(): List<SyncMetadata>
    
    /**
     * Cuenta registros pendientes de sincronización
     */
    @Query("SELECT COUNT(*) FROM sync_metadata WHERE needsSync = 1")
    suspend fun countUnsyncedChanges(): Int
    
    /**
     * Observa cambios en registros no sincronizados
     */
    @Query("SELECT COUNT(*) FROM sync_metadata WHERE needsSync = 1")
    fun observeUnsyncedCount(): Flow<Int>
    
    /**
     * Obtiene el timestamp de la última sincronización exitosa
     */
    @Query("SELECT MAX(lastSynced) FROM sync_metadata WHERE entityType = :entityType")
    suspend fun getLastSyncTime(entityType: String): Long?
    
    /**
     * Obtiene todos los metadatos por tipo de entidad
     */
    @Query("SELECT * FROM sync_metadata WHERE entityType = :entityType")
    suspend fun getAllByType(entityType: String): List<SyncMetadata>
    
    /**
     * Busca por ID remoto
     */
    @Query("SELECT * FROM sync_metadata WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): SyncMetadata?
    
    // =================== INSERTS ===================
    
    /**
     * Inserta o actualiza metadatos de sincronización
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: SyncMetadata): Long
    
    /**
     * Inserta múltiples metadatos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(metadata: List<SyncMetadata>)
    
    // =================== UPDATES ===================
    
    /**
     * Actualiza metadatos
     */
    @Update
    suspend fun update(metadata: SyncMetadata)
    
    /**
     * Marca un registro para sincronización
     */
    @Query("UPDATE sync_metadata SET needsSync = 1, updatedAt = :timestamp WHERE entityType = :entityType AND localId = :localId")
    suspend fun markForSync(entityType: String, localId: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Marca como sincronizado exitosamente
     */
    @Query("""
        UPDATE sync_metadata 
        SET needsSync = 0, 
            remoteId = :remoteId,
            lastSynced = :timestamp,
            syncAttempts = 0,
            lastError = NULL,
            lastErrorAt = NULL
        WHERE entityType = :entityType AND localId = :localId
    """)
    suspend fun markAsSynced(entityType: String, localId: Long, remoteId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Marca como eliminado
     */
    @Query("UPDATE sync_metadata SET isDeleted = 1, needsSync = 1, updatedAt = :timestamp WHERE entityType = :entityType AND localId = :localId")
    suspend fun markAsDeleted(entityType: String, localId: Long, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Actualiza el error de sincronización
     */
    @Query("""
        UPDATE sync_metadata 
        SET lastError = :error,
            lastErrorAt = :timestamp,
            syncAttempts = syncAttempts + 1
        WHERE entityType = :entityType AND localId = :localId
    """)
    suspend fun updateSyncError(entityType: String, localId: Long, error: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Resetea los intentos de sincronización
     */
    @Query("UPDATE sync_metadata SET syncAttempts = 0, lastError = NULL, lastErrorAt = NULL WHERE entityType = :entityType")
    suspend fun resetSyncAttempts(entityType: String)
    
    // =================== DELETES ===================
    
    /**
     * Elimina metadatos
     */
    @Delete
    suspend fun delete(metadata: SyncMetadata)
    
    /**
     * Elimina metadatos por tipo de entidad e ID local
     */
    @Query("DELETE FROM sync_metadata WHERE entityType = :entityType AND localId = :localId")
    suspend fun deleteByEntityAndLocalId(entityType: String, localId: Long)
    
    /**
     * Elimina todos los metadatos de un tipo de entidad
     */
    @Query("DELETE FROM sync_metadata WHERE entityType = :entityType")
    suspend fun deleteAllByType(entityType: String)
    
    /**
     * Elimina registros sincronizados y marcados como eliminados
     */
    @Query("DELETE FROM sync_metadata WHERE isDeleted = 1 AND needsSync = 0")
    suspend fun cleanupDeletedSynced()
    
    /**
     * Limpia todos los metadatos
     */
    @Query("DELETE FROM sync_metadata")
    suspend fun deleteAll()
    
    // =================== TRANSACTIONS ===================
    
    /**
     * Crea o actualiza metadatos para sincronización
     */
    @Transaction
    suspend fun upsertForSync(entityType: String, localId: Long) {
        val existing = getMetadata(entityType, localId)
        if (existing != null) {
            markForSync(entityType, localId)
        } else {
            insert(
                SyncMetadata(
                    entityType = entityType,
                    localId = localId,
                    needsSync = true
                )
            )
        }
    }
}
