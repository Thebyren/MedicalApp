package com.medical.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medical.app.data.dao.*
import com.medical.app.data.database.converters.Converters
import com.medical.app.data.entities.*

/**
 * Clase principal de la base de datos de la aplicación.
 * Utiliza Room para la persistencia de datos.
 */
@Database(
    entities = [
        Usuario::class,
        Medico::class,
        Paciente::class,
        MedicoPaciente::class,
        Consulta::class,
        Tratamiento::class,
        HistorialMedico::class,
        Appointment::class,
        DailyIncome::class,  // PASO 1: Descomentado
        SyncMetadata::class  // Tabla para sincronización con Supabase
    ],
    version = 7, // Made usuarioId nullable in pacientes table for Supabase sync
    exportSchema = true // Habilitado para mantener un historial de migraciones
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun usuarioDao(): UsuarioDao
    abstract fun medicoDao(): MedicoDao
    abstract fun pacienteDao(): PacienteDao
    abstract fun medicoPacienteDao(): MedicoPacienteDao
    abstract fun consultaDao(): ConsultaDao
    abstract fun tratamientoDao(): TratamientoDao
    abstract fun historialMedicoDao(): HistorialMedicoDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun dailyIncomeDao(): DailyIncomeDao  // PASO 1: Descomentado
    abstract fun syncMetadataDao(): SyncMetadataDao  // DAO para sincronización

    companion object {
        // Nombre de la base de datos
        private const val DATABASE_NAME = "medical_app_db.db"

        // Instancia única de la base de datos (patrón Singleton)
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Bloqueo para sincronización de hilos
        private val LOCK = Any()

        /**
         * Migración de la versión 1 a la 2:
         * - Agrega la columna 'salt' a la tabla 'usuarios' para almacenar la sal del hash de la contraseña
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN salt TEXT")
            }
        }

        /**
         * Migración de la versión 2 a la 3:
         * - Agrega índices a las columnas patientId y doctorId en la tabla appointments
         *   para mejorar el rendimiento de las consultas con claves foráneas
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_patientId ON appointments(patientId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_doctorId ON appointments(doctorId)")
            }
        }
        
        /**
         * Migración de la versión 3 a la 4:
         * - Hace que consultaId sea nullable en la tabla tratamientos
         *   para permitir prescripciones independientes sin consulta asociada
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear tabla temporal con la nueva estructura
                db.execSQL("""
                    CREATE TABLE tratamientos_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        consultaId INTEGER,
                        medicamento TEXT NOT NULL,
                        dosis TEXT NOT NULL,
                        frecuencia TEXT NOT NULL,
                        duracionDias INTEGER,
                        indicaciones TEXT,
                        FOREIGN KEY(consultaId) REFERENCES consultas(id) ON DELETE CASCADE
                    )
                """)
                
                // Copiar datos de la tabla antigua a la nueva
                db.execSQL("""
                    INSERT INTO tratamientos_new (id, consultaId, medicamento, dosis, frecuencia, duracionDias, indicaciones)
                    SELECT id, consultaId, medicamento, dosis, frecuencia, duracionDias, indicaciones
                    FROM tratamientos
                """)
                
                // Eliminar tabla antigua
                db.execSQL("DROP TABLE tratamientos")
                
                // Renombrar tabla nueva
                db.execSQL("ALTER TABLE tratamientos_new RENAME TO tratamientos")
                
                // Recrear índice
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tratamientos_consultaId ON tratamientos(consultaId)")
            }
        }

        /**
         * Migración de la versión 4 a la 5:
         * - Agrega columnas cost e isPaid a appointments
         * - Crea la tabla daily_income para tracking de ingresos
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Agregar columnas cost e isPaid a appointments
                db.execSQL("ALTER TABLE appointments ADD COLUMN cost REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE appointments ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
                
                // Crear tabla daily_income - PASO 1: Descomentado
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_income (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        doctorId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        totalIncome REAL NOT NULL DEFAULT 0.0,
                        completedAppointments INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                
                // Crear índice único para doctorId y date
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_daily_income_doctorId_date 
                    ON daily_income(doctorId, date)
                """)
            }
        }

        /**
         * Migración de la versión 5 a la 6:
         * - Crea la tabla sync_metadata para sincronización con Supabase
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear tabla sync_metadata
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sync_metadata (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entityType TEXT NOT NULL,
                        localId INTEGER NOT NULL,
                        remoteId TEXT,
                        lastSynced INTEGER,
                        updatedAt INTEGER NOT NULL,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        needsSync INTEGER NOT NULL DEFAULT 1,
                        syncAttempts INTEGER NOT NULL DEFAULT 0,
                        lastError TEXT,
                        lastErrorAt INTEGER,
                        createdAt INTEGER NOT NULL
                    )
                """)
                
                // Crear índices para optimización
                db.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_sync_metadata_entityType_localId 
                    ON sync_metadata(entityType, localId)
                """)
                
                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_sync_metadata_entityType 
                    ON sync_metadata(entityType)
                """)
                
                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_sync_metadata_needsSync 
                    ON sync_metadata(needsSync)
                """)
                
                db.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_sync_metadata_isDeleted 
                    ON sync_metadata(isDeleted)
                """)
            }
        }

        /**
         * Migración de la versión 6 a la 7:
         * - Hace el campo usuarioId nullable en la tabla pacientes para permitir sincronización desde Supabase
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQLite no soporta ALTER COLUMN directamente, necesitamos recrear la tabla
                
                // 1. Crear nueva tabla temporal con usuarioId nullable
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pacientes_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        usuarioId INTEGER,
                        nombre TEXT NOT NULL,
                        apellidos TEXT NOT NULL,
                        fechaNacimiento INTEGER NOT NULL,
                        genero TEXT,
                        telefono TEXT,
                        direccion TEXT,
                        numeroSeguridadSocial TEXT,
                        contactoEmergencia TEXT,
                        telefonoEmergencia TEXT,
                        email TEXT NOT NULL DEFAULT '',
                        bloodType TEXT NOT NULL DEFAULT '',
                        allergies TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        FOREIGN KEY(usuarioId) REFERENCES usuarios(id) ON DELETE CASCADE
                    )
                """)
                
                // 2. Copiar datos de la tabla antigua a la nueva
                db.execSQL("""
                    INSERT INTO pacientes_new (
                        id, usuarioId, nombre, apellidos, fechaNacimiento, genero, telefono,
                        direccion, numeroSeguridadSocial, contactoEmergencia, telefonoEmergencia,
                        email, bloodType, allergies, notes
                    )
                    SELECT 
                        id, usuarioId, nombre, apellidos, fechaNacimiento, genero, telefono,
                        direccion, numeroSeguridadSocial, contactoEmergencia, telefonoEmergencia,
                        email, bloodType, allergies, notes
                    FROM pacientes
                """)
                
                // 3. Eliminar tabla antigua
                db.execSQL("DROP TABLE pacientes")
                
                // 4. Renombrar tabla nueva
                db.execSQL("ALTER TABLE pacientes_new RENAME TO pacientes")
                
                // 5. Recrear índices
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pacientes_usuarioId ON pacientes(usuarioId)")
            }
        }

        /**
         * Obtiene la instancia de la base de datos.
         * Si no existe, la crea.
         * 
         * @param context Contexto de la aplicación
         * @return Instancia de AppDatabase
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(LOCK) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        /**
         * Construye la base de datos con la configuración adecuada.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Aquí puedes ejecutar código después de crear la base de datos
                    // por ejemplo, para insertar datos iniciales
                }
            })
            .addMigrations(
                // Aquí se agregan las migraciones en orden de versión
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7
                // Agregar más migraciones según sea necesario
            )
            .fallbackToDestructiveMigration() // TEMPORAL: Descomentar solo en desarrollo
            .build()
        }


        /**
         * Cierra la base de datos y limpia la instancia.
         * Útil para pruebas o para forzar una nueva instancia.
         */
        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
