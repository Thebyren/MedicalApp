package com.medical.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.medical.app.sync.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Clase de aplicación personalizada para inicializar componentes globales.
 * Hilt generará código para esta clase anotada con @HiltAndroidApp.
 */
@HiltAndroidApp
class MedicalApplication : Application(), Configuration.Provider {
    
    companion object {
        private const val TAG = "MedicalApplication"
    }
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var syncManager: SyncManager
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Aplicación inicializada")
        
        // Programar sincronización automática periódica
        Log.d(TAG, "Programando sincronización automática periódica...")
        syncManager.schedulePeriodicSync()
        Log.d(TAG, "Sincronización automática configurada cada ${SyncManager.SYNC_INTERVAL_MINUTES} minutos")
    }
    
    /**
     * Configuración de WorkManager con HiltWorkerFactory
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    override fun onTerminate() {
        super.onTerminate()
        // La base de datos se cerrará automáticamente cuando la aplicación se cierre
    }
}
