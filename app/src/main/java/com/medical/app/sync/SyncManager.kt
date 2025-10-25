package com.medical.app.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.medical.app.data.entities.EntityType
import com.medical.app.data.repository.SyncRepository
import com.medical.app.data.repository.SyncResult
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Administrador de sincronización que coordina las operaciones de sincronización
 * entre la base de datos local y Supabase
 */
@Singleton
class SyncManager @Inject constructor(
    private val context: Context,
    private val syncRepository: SyncRepository
) {
    
    companion object {
        const val SYNC_WORK_NAME = "medical_app_sync"
        const val SYNC_TAG = "sync_work"
        const val PERIODIC_SYNC_WORK_NAME = "medical_app_periodic_sync"
        
        // Intervalo de sincronización periódica (en minutos)
        // WorkManager tiene un mínimo de 15 minutos para trabajos periódicos
        const val SYNC_INTERVAL_MINUTES = 15L
        
        private const val TAG = "SyncManager"
    }
    
    /**
     * Inicializa metadata para datos existentes
     */
    suspend fun initializeExistingData(): Boolean {
        return syncRepository.initializeExistingData()
    }
    
    /**
     * Inicia la sincronización manual
     */
    suspend fun syncNow(): SyncResult {
        // Primero inicializar metadata para datos existentes
        initializeExistingData()
        // Luego sincronizar
        return syncRepository.syncAll()
    }
    
    /**
     * Programa una sincronización única
     */
    fun scheduleSyncOnce() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SYNC_TAG)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
    }
    
    /**
     * Programa sincronización periódica
     */
    fun schedulePeriodicSync() {
        Log.d(TAG, "Programando sincronización periódica cada $SYNC_INTERVAL_MINUTES minutos")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(SYNC_TAG)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
        
        Log.d(TAG, "Sincronización periódica programada exitosamente")
    }
    
    /**
     * Cancela todas las sincronizaciones programadas
     */
    fun cancelAllSync() {
        WorkManager.getInstance(context).cancelAllWorkByTag(SYNC_TAG)
    }
    
    /**
     * Cancela la sincronización periódica
     */
    fun cancelPeriodicSync() {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC_WORK_NAME)
    }
    
    /**
     * Obtiene el estado de la sincronización
     */
    suspend fun getSyncStatus() = syncRepository.getSyncStatus()
    
    /**
     * Observa el número de cambios no sincronizados
     */
    fun observeUnsyncedCount(): Flow<Int> = syncRepository.observeUnsyncedCount()
    
    /**
     * Marca una entidad para sincronización
     */
    suspend fun markForSync(entityType: EntityType, localId: Long) {
        syncRepository.markForSync(entityType, localId)
        // Programa una sincronización si hay conexión
        scheduleSyncOnce()
    }
    
    /**
     * Resetea los intentos de sincronización
     */
    suspend fun resetSyncAttempts(entityType: EntityType) {
        syncRepository.resetSyncAttempts(entityType)
    }
    
    /**
     * Verifica si hay sincronización en progreso
     */
    fun isSyncInProgress(): Boolean {
        val workInfo = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(SYNC_WORK_NAME)
            .get()
        
        return workInfo.any { it.state == WorkInfo.State.RUNNING }
    }
    
    /**
     * Observa el estado de la sincronización
     */
    fun observeSyncWork(): LiveData<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData(SYNC_TAG)
    }
}
