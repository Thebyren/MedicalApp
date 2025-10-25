# Guía de Integración de Sincronización

Esta guía explica cómo integrar la funcionalidad de sincronización con Supabase en los repositorios existentes de la aplicación.

## Pasos de Integración

### 1. Actualizar Repositorios Existentes

Para cada repositorio que maneje datos que necesiten sincronización, sigue estos pasos:

#### Ejemplo: PacienteRepository

```kotlin
class PacienteRepository @Inject constructor(
    private val pacienteDao: PacienteDao,
    private val syncRepository: SyncRepository  // Añadir esta dependencia
) {
    
    // INSERT - Crear nuevo paciente
    suspend fun insert(paciente: Paciente): Long {
        val id = pacienteDao.insert(paciente)
        
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.PACIENTES, id)
        
        return id
    }
    
    // UPDATE - Actualizar paciente existente
    suspend fun update(paciente: Paciente) {
        pacienteDao.update(paciente)
        
        // Marcar para sincronización
        syncRepository.markForSync(EntityType.PACIENTES, paciente.id)
    }
    
    // DELETE - Eliminar paciente
    suspend fun delete(paciente: Paciente) {
        pacienteDao.delete(paciente)
        
        // Marcar como eliminado para sincronización
        syncRepository.markForSync(EntityType.PACIENTES, paciente.id)
    }
}
```

### 2. Actualizar ViewModels

Agrega funcionalidad de sincronización a los ViewModels:

```kotlin
@HiltViewModel
class PacienteViewModel @Inject constructor(
    private val pacienteRepository: PacienteRepository,
    private val syncManager: SyncManager  // Añadir SyncManager
) : ViewModel() {
    
    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus
    
    // Observar cambios no sincronizados
    val unsyncedCount = syncManager.observeUnsyncedCount()
        .asLiveData()
    
    fun savePaciente(paciente: Paciente) {
        viewModelScope.launch {
            try {
                if (paciente.id == 0L) {
                    pacienteRepository.insert(paciente)
                } else {
                    pacienteRepository.update(paciente)
                }
                
                // Sincronizar inmediatamente si hay conexión
                syncManager.scheduleSyncOnce()
                
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    fun syncData() {
        viewModelScope.launch {
            _syncStatus.value = syncManager.getSyncStatus()
            
            val result = syncManager.syncNow()
            
            if (result.success) {
                // Mostrar éxito
                _syncStatus.value = syncManager.getSyncStatus()
            } else {
                // Mostrar error: result.error
            }
        }
    }
}
```

### 3. Actualizar la UI

#### Agregar indicador de sincronización

```xml
<!-- res/layout/fragment_pacientes.xml -->
<androidx.coordinatorlayout.widget.CoordinatorLayout>
    
    <!-- Tu contenido existente -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        ... />
    
    <!-- Indicador de sincronización -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/syncStatusCard"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:visibility="gone">
        
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="8dp">
            
            <ProgressBar
                android:id="@+id/syncProgress"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:visibility="gone"/>
            
            <TextView
                android:id="@+id/syncStatusText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="8dp"
                android:text="0 cambios pendientes"/>
            
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
    
    <!-- FAB para sincronización manual -->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabSync"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|start"
        android:layout_margin="16dp"
        android:src="@drawable/ic_sync"/>
        
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

#### Implementar en Fragment

```kotlin
class PacientesFragment : Fragment() {
    
    private val viewModel: PacienteViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observar estado de sincronización
        viewModel.unsyncedCount.observe(viewLifecycleOwner) { count ->
            if (count > 0) {
                binding.syncStatusCard.visibility = View.VISIBLE
                binding.syncStatusText.text = "$count cambios pendientes"
            } else {
                binding.syncStatusCard.visibility = View.GONE
            }
        }
        
        // Botón de sincronización manual
        binding.fabSync.setOnClickListener {
            binding.syncProgress.visibility = View.VISIBLE
            viewModel.syncData()
        }
        
        // Observar estado de sincronización
        viewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            binding.syncProgress.visibility = View.GONE
            
            if (status.hasUnsyncedChanges) {
                binding.fabSync.setImageResource(R.drawable.ic_sync_problem)
            } else {
                binding.fabSync.setImageResource(R.drawable.ic_sync)
            }
        }
    }
}
```

## Integración por Entidad

### Pacientes

```kotlin
// En PacienteRepository
suspend fun insertPaciente(paciente: Paciente): Long {
    val id = pacienteDao.insert(paciente)
    syncRepository.markForSync(EntityType.PACIENTES, id)
    return id
}
```

### Citas (Appointments)

```kotlin
// En AppointmentRepository
suspend fun saveAppointment(appointment: Appointment): Long {
    val id = if (appointment.id == 0L) {
        appointmentDao.insert(appointment)
    } else {
        appointmentDao.update(appointment)
        appointment.id
    }
    
    syncRepository.markForSync(EntityType.APPOINTMENTS, id)
    
    // Actualizar ingresos diarios si la cita está completada
    if (appointment.status == AppointmentStatus.COMPLETED) {
        updateDailyIncome(appointment)
    }
    
    return id
}
```

### Consultas

```kotlin
// En ConsultaRepository
suspend fun saveConsulta(consulta: Consulta): Long {
    val id = consultaDao.insert(consulta)
    syncRepository.markForSync(EntityType.CONSULTAS, id)
    return id
}
```

### Tratamientos

```kotlin
// En TratamientoRepository
suspend fun saveTratamiento(tratamiento: Tratamiento): Long {
    val id = tratamientoDao.insert(tratamiento)
    syncRepository.markForSync(EntityType.TRATAMIENTOS, id)
    return id
}
```

### Historial Médico

```kotlin
// En HistorialMedicoRepository
suspend fun addHistorial(historial: HistorialMedico): Long {
    val id = historialMedicoDao.insert(historial)
    syncRepository.markForSync(EntityType.HISTORIAL_MEDICO, id)
    return id
}
```

## Manejo de Errores

### En Repositorios

```kotlin
suspend fun savePacienteWithSync(paciente: Paciente): Result<Long> {
    return try {
        val id = pacienteDao.insert(paciente)
        syncRepository.markForSync(EntityType.PACIENTES, id)
        
        // Intentar sincronización inmediata
        try {
            syncManager.syncNow()
        } catch (e: Exception) {
            // La sincronización falló pero el dato se guardó localmente
            // Se sincronizará más tarde automáticamente
        }
        
        Result.success(id)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### En ViewModels

```kotlin
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState

fun saveData(paciente: Paciente) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        
        pacienteRepository.savePacienteWithSync(paciente)
            .fold(
                onSuccess = { id ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            message = "Guardado exitosamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
            )
    }
}
```

## Configuración de Sincronización Automática

### En MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var syncManager: SyncManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Programar sincronización periódica
        syncManager.schedulePeriodicSync()
        
        // Sincronizar al iniciar si hay cambios pendientes
        lifecycleScope.launch {
            val status = syncManager.getSyncStatus()
            if (status.hasUnsyncedChanges && status.isConnected) {
                syncManager.scheduleSyncOnce()
            }
        }
    }
}
```

### En Application

```kotlin
@HiltAndroidApp
class MedicalApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var syncManager: SyncManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Configurar sincronización periódica global
        // (Solo si quieres que se ejecute aunque la app no esté abierta)
        syncManager.schedulePeriodicSync()
    }
}
```

## Pruebas

### Prueba de Sincronización Manual

```kotlin
@Test
fun testSyncPaciente() = runTest {
    // Arrange
    val paciente = Paciente(nombre = "Test", apellidos = "User")
    
    // Act
    val id = repository.insert(paciente)
    val syncResult = syncManager.syncNow()
    
    // Assert
    assertTrue(syncResult.success)
    assertEquals(0, syncRepository.getSyncStatus().unsyncedCount)
}
```

### Prueba de Conflictos

```kotlin
@Test
fun testConflictResolution() = runTest {
    // Crear dato local
    val localPaciente = Paciente(id = 1, nombre = "Local")
    repository.insert(localPaciente)
    
    // Simular cambio remoto
    val remotePaciente = PacienteDto(id = 1, nombre = "Remote")
    // ... código para insertar en Supabase
    
    // Sincronizar
    val result = syncManager.syncNow()
    
    // Verificar resolución de conflicto
    val resolved = repository.getPacienteById(1)
    // El más reciente debería ganar
    assertEquals("Remote", resolved.nombre)
}
```

## Monitoreo

### Logs de Sincronización

```kotlin
class SyncLogger {
    fun logSync(result: SyncResult) {
        if (BuildConfig.DEBUG) {
            Log.d("Sync", "Resultado: ${result.success}")
            result.entityResults.forEach { entity ->
                Log.d("Sync", "${entity.entityType}: " +
                    "↑${entity.uploaded} ↓${entity.downloaded} " +
                    "❌${entity.errors}")
            }
        }
    }
}
```

### Métricas

```kotlin
data class SyncMetrics(
    val totalSyncs: Int,
    val successfulSyncs: Int,
    val failedSyncs: Int,
    val averageSyncTime: Long,
    val lastSyncTime: Long
)
```

## Optimizaciones

### Sincronización Diferida

```kotlin
// Solo sincronizar cuando hay WiFi
fun scheduleWifiOnlySync() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .build()
    
    // ... configurar WorkRequest con constraints
}
```

### Batch de Operaciones

```kotlin
// Acumular cambios antes de sincronizar
class BatchSyncManager {
    private val pendingChanges = mutableListOf<SyncChange>()
    
    fun addChange(change: SyncChange) {
        pendingChanges.add(change)
        
        if (pendingChanges.size >= BATCH_SIZE) {
            triggerSync()
        }
    }
}
```

## Troubleshooting

### Problema: Datos duplicados
**Solución**: Verificar que los índices únicos estén configurados correctamente en ambas bases de datos.

### Problema: Sincronización lenta
**Solución**: Implementar paginación y sincronización incremental.

### Problema: Conflictos frecuentes
**Solución**: Revisar la estrategia de resolución de conflictos y considerar usar timestamps más precisos.

### Problema: Pérdida de datos
**Solución**: Asegurarse de que todos los cambios locales se marquen para sincronización antes de sobrescribir con datos remotos.
