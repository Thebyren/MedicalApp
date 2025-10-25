# Configuración de Supabase para Sincronización

## Requisitos Previos

1. Una cuenta en Supabase (https://supabase.com)
2. Un proyecto creado en Supabase
3. Las credenciales del proyecto (URL y Anon Key)

## Configuración del Proyecto

### 1. Configuración de Credenciales

Crea un archivo `local.properties` en la raíz del proyecto (si no existe) y agrega:

```properties
# Credenciales de Supabase
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_ANON_KEY=tu-clave-anonima-aqui
```

**IMPORTANTE**: Nunca subas este archivo al control de versiones. Ya está incluido en `.gitignore`.

### 2. Estructura de la Base de Datos

La base de datos en Supabase ya debe estar configurada con el script SQL proporcionado. Las tablas principales son:

- `usuarios` - Usuarios del sistema
- `medicos` - Información de médicos
- `pacientes` - Información de pacientes
- `appointments` - Citas médicas
- `consultas` - Consultas médicas
- `tratamientos` - Tratamientos y prescripciones
- `historial_medico` - Historial médico de pacientes
- `daily_income` - Ingresos diarios
- `sync_metadata` - Metadatos de sincronización

### 3. Políticas de Seguridad (RLS)

Las políticas RLS ya están configuradas en el script SQL. Por defecto, permiten acceso completo para desarrollo. En producción, deberás ajustarlas según tus requisitos de seguridad.

## Arquitectura de Sincronización

### Componentes Principales

1. **SupabaseClient**: Cliente configurado para comunicarse con Supabase
2. **SyncMetadata**: Entidad que rastrea el estado de sincronización
3. **SyncRepository**: Maneja la lógica de sincronización
4. **SyncManager**: Coordina las operaciones de sincronización
5. **SyncWorker**: Ejecuta sincronización en segundo plano

### Flujo de Sincronización

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│  Room DB    │◄─────►│ SyncRepository│◄─────►│  Supabase   │
└─────────────┘       └──────────────┘       └─────────────┘
      ▲                      ▲
      │                      │
      ▼                      ▼
┌─────────────┐       ┌──────────────┐
│SyncMetadata │       │  SyncManager  │
└─────────────┘       └──────────────┘
                             ▲
                             │
                             ▼
                      ┌──────────────┐
                      │  SyncWorker   │
                      └──────────────┘
```

## Uso de la Sincronización

### Sincronización Manual

```kotlin
class TuViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {
    
    fun sincronizar() {
        viewModelScope.launch {
            val resultado = syncManager.syncNow()
            if (resultado.success) {
                // Sincronización exitosa
            } else {
                // Manejar error: resultado.error
            }
        }
    }
}
```

### Sincronización Automática

La sincronización automática se programa al iniciar la aplicación:

```kotlin
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var syncManager: SyncManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Programar sincronización periódica (cada 60 minutos)
        syncManager.schedulePeriodicSync()
    }
}
```

### Marcar Entidades para Sincronización

Cuando creas o modificas una entidad local, márcala para sincronización:

```kotlin
class PacienteRepository @Inject constructor(
    private val pacienteDao: PacienteDao,
    private val syncManager: SyncManager
) {
    
    suspend fun insertPaciente(paciente: Paciente) {
        val id = pacienteDao.insert(paciente)
        
        // Marcar para sincronización
        syncManager.markForSync(EntityType.PACIENTES, id)
    }
    
    suspend fun updatePaciente(paciente: Paciente) {
        pacienteDao.update(paciente)
        
        // Marcar para sincronización
        syncManager.markForSync(EntityType.PACIENTES, paciente.id)
    }
}
```

### Observar Estado de Sincronización

```kotlin
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {
    
    // Observar número de cambios no sincronizados
    val unsyncedCount = syncManager.observeUnsyncedCount()
        .asLiveData()
    
    // Observar estado del trabajo de sincronización
    val syncWorkStatus = syncManager.observeSyncWork()
    
    suspend fun getSyncStatus() = syncManager.getSyncStatus()
}
```

## Resolución de Problemas

### Error: "No hay conexión con Supabase"
- Verifica que las credenciales en `local.properties` sean correctas
- Asegúrate de tener conexión a Internet
- Verifica que el proyecto de Supabase esté activo

### Error: "Error de sincronización"
- Revisa los logs para ver el error específico
- Verifica que las estructuras de las tablas coincidan entre Room y Supabase
- Asegúrate de que las políticas RLS permitan las operaciones necesarias

### Datos no se sincronizan
- Verifica que los datos estén marcados con `needsSync = true`
- Revisa si hay errores en `sync_metadata` tabla
- Asegúrate de que WorkManager esté ejecutándose

## Configuración Avanzada

### Cambiar Intervalo de Sincronización

Modifica la constante en `SyncManager.kt`:

```kotlin
companion object {
    const val SYNC_INTERVAL_MINUTES = 30L  // Cambiar a 30 minutos
}
```

### Sincronización Selectiva

Para sincronizar solo ciertas entidades, modifica `SyncRepository.syncAll()`:

```kotlin
suspend fun syncAll(): SyncResult {
    // Sincronizar solo pacientes y citas
    val results = mutableListOf<EntitySyncResult>()
    results.add(syncPacientes())
    results.add(syncAppointments())
    // Omitir otras entidades
    return SyncResult(...)
}
```

### Manejo de Conflictos Personalizado

Implementa tu propia lógica en `SyncRepository`:

```kotlin
private suspend fun resolveConflict(
    local: Entity,
    remote: EntityDto
): Entity {
    // Tu lógica de resolución de conflictos
    return if (local.updatedAt > remote.updatedAt) {
        local  // Mantener cambio local
    } else {
        remote.toEntity()  // Usar versión remota
    }
}
```

## Seguridad

### En Desarrollo
- Las políticas RLS actuales permiten acceso completo
- Útil para pruebas y desarrollo rápido

### En Producción
1. Implementa autenticación con Supabase Auth
2. Actualiza las políticas RLS para usar `auth.uid()`
3. Usa Row Level Security para limitar acceso por usuario
4. Considera usar Service Role Key para operaciones administrativas

## Monitoreo

### Dashboard de Supabase
- Revisa el uso de la base de datos
- Monitorea las consultas lentas
- Verifica los logs de errores

### Logs Locales
- Los errores de sincronización se guardan en `sync_metadata`
- Usa Android Studio Logcat para debug
- Implementa crashlytics para producción

## Optimización

1. **Paginación**: Para grandes conjuntos de datos
2. **Sincronización Incremental**: Solo sincronizar cambios recientes
3. **Compresión**: Usar gzip para reducir transferencia de datos
4. **Caché**: Implementar caché para consultas frecuentes
5. **Batch Operations**: Agrupar operaciones para reducir llamadas a la API
