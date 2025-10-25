package com.medical.app.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medical.app.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker que ejecuta la sincronización en segundo plano
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "SyncWorker"
        const val KEY_FORCE_SYNC = "force_sync"
        const val KEY_ENTITY_TYPE = "entity_type"
        const val KEY_SYNC_RESULT = "sync_result"
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔄 SyncWorker ejecutándose... (intento ${runAttemptCount + 1})")
        
        try {
            // Verificar si es una sincronización forzada
            val forceSync = inputData.getBoolean(KEY_FORCE_SYNC, false)
            val entityType = inputData.getString(KEY_ENTITY_TYPE)
            
            Log.d(TAG, "Configuración: forceSync=$forceSync, entityType=$entityType")
            
            // Inicializar metadata para datos existentes
            Log.d(TAG, "Inicializando metadata para datos existentes...")
            syncRepository.initializeExistingData()
            
            // Realizar sincronización
            Log.d(TAG, "Ejecutando sincronización completa...")
            val syncResult = if (entityType != null) {
                // Sincronizar tipo específico de entidad (por implementar)
                syncRepository.syncAll()
            } else {
                // Sincronizar todo
                syncRepository.syncAll()
            }
            
            Log.d(TAG, "Resultado de sincronización: success=${syncResult.success}, error=${syncResult.error}")
            
            // Devolver resultado basado en el éxito de la sincronización
            if (syncResult.success) {
                Log.d(TAG, "✅ Sincronización automática completada exitosamente")
                Result.success()
            } else {
                if (runAttemptCount < 3) {
                    Log.w(TAG, "⚠️ Sincronización falló, reintentando... (intento ${runAttemptCount + 1}/3)")
                    // Reintentar si no hemos alcanzado el límite
                    Result.retry()
                } else {
                    Log.e(TAG, "❌ Sincronización falló después de 3 intentos")
                    // Fallar después de 3 intentos
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error durante la sincronización automática", e)
            
            // Decidir si reintentar basado en el tipo de error y número de intentos
            if (runAttemptCount < 3) {
                Log.w(TAG, "Reintentando sincronización... (intento ${runAttemptCount + 1}/3)")
                Result.retry()
            } else {
                Log.e(TAG, "Sincronización falló definitivamente después de 3 intentos")
                Result.failure()
            }
        }
    }
}
