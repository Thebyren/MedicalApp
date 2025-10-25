package com.medical.app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.medical.app.data.repository.SyncStatus
import com.medical.app.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para manejar la sincronización de datos
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {
    
    companion object {
        private const val TAG = "SyncViewModel"
    }
    
    // Estado de UI
    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()
    
    // Observar número de cambios no sincronizados
    val unsyncedCount: LiveData<Int> = syncManager.observeUnsyncedCount().asLiveData()
    
    // Observar estado del trabajo de sincronización
    val syncWorkStatus = syncManager.observeSyncWork()
    
    init {
        Log.d(TAG, "SyncViewModel inicializado")
        loadSyncStatus()
    }
    
    /**
     * Carga el estado actual de sincronización
     */
    private fun loadSyncStatus() {
        Log.d(TAG, "Cargando estado de sincronización...")
        viewModelScope.launch {
            try {
                val status = syncManager.getSyncStatus()
                Log.d(TAG, "Estado de sincronización obtenido: isConnected=${status.isConnected}, unsyncedCount=${status.unsyncedCount}")
                
                // Verificar si las credenciales están configuradas
                if (!status.isConnected) {
                    Log.w(TAG, "Supabase no está conectado - verificar credenciales")
                    _uiState.value = _uiState.value.copy(
                        syncStatus = status,
                        isLoading = false,
                        error = """
                            ⚠️ Supabase no está configurado correctamente.
                            
                            Por favor verifica:
                            1. Crea un archivo 'local.properties' en la raíz del proyecto
                            2. Agrega las siguientes líneas:
                               SUPABASE_URL=tu_url_de_supabase
                               SUPABASE_ANON_KEY=tu_key_de_supabase
                            3. Reinicia la aplicación después de configurar
                            
                            Consulta 'local.properties.example' para ver un ejemplo.
                        """.trimIndent()
                    )
                } else {
                    Log.d(TAG, "Supabase conectado correctamente")
                    _uiState.value = _uiState.value.copy(
                        syncStatus = status,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar estado de sincronización", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar estado: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Inicia sincronización manual
     */
    fun startSync() {
        Log.d(TAG, "=== INICIANDO SINCRONIZACIÓN MANUAL ===")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                error = null
            )
            
            try {
                Log.d(TAG, "Llamando a syncManager.syncNow()...")
                val result = syncManager.syncNow()
                
                Log.d(TAG, "Sincronización completada: success=${result.success}, error=${result.error}")
                Log.d(TAG, "Resultados por entidad: ${result.entityResults.size} entidades procesadas")
                result.entityResults.forEach { entityResult ->
                    Log.d(TAG, "  ${entityResult.entityType}: uploaded=${entityResult.uploaded}, downloaded=${entityResult.downloaded}, errors=${entityResult.errors}")
                }
                
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    lastSyncSuccess = result.success,
                    lastSyncMessage = if (result.success) {
                        "Sincronización completada exitosamente"
                    } else {
                        result.error ?: "Error durante la sincronización"
                    },
                    syncResult = result
                )
                
                // Recargar estado
                Log.d(TAG, "Recargando estado de sincronización...")
                loadSyncStatus()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error durante la sincronización", e)
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    error = "Error de sincronización: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Programa sincronización única
     */
    fun scheduleSyncOnce() {
        syncManager.scheduleSyncOnce()
    }
    
    /**
     * Programa sincronización periódica
     */
    fun schedulePeriodicSync() {
        syncManager.schedulePeriodicSync()
    }
    
    /**
     * Cancela todas las sincronizaciones programadas
     */
    fun cancelAllSync() {
        syncManager.cancelAllSync()
    }
    
    /**
     * Cancela sincronización periódica
     */
    fun cancelPeriodicSync() {
        syncManager.cancelPeriodicSync()
    }
    
    /**
     * Verifica si hay sincronización en progreso
     */
    fun checkSyncInProgress(): Boolean {
        return syncManager.isSyncInProgress()
    }
    
    /**
     * Limpia mensajes de error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * Estado de UI para la pantalla de sincronización
 */
data class SyncUiState(
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val syncStatus: SyncStatus? = null,
    val lastSyncSuccess: Boolean? = null,
    val lastSyncMessage: String? = null,
    val error: String? = null,
    val syncResult: com.medical.app.data.repository.SyncResult? = null
)
