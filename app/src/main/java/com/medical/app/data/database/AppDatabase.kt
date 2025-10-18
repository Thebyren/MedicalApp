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
        DailyIncome::class  // PASO 1: Descomentado
    ],
    version = 5, // Added cost/isPaid to Appointment and DailyIncome entity
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

    companion object {
        // Nombre de la base de datos
        private const val DATABASE_NAME = "medical_app_database.db"

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
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE usuarios ADD COLUMN salt TEXT")
            }
        }

        /**
         * Migración de la versión 2 a la 3:
         * - Agrega índices a las columnas patientId y doctorId en la tabla appointments
         *   para mejorar el rendimiento de las consultas con claves foráneas
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_patientId ON appointments(patientId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_appointments_doctorId ON appointments(doctorId)")
            }
        }
        
        /**
         * Migración de la versión 3 a la 4:
         * - Hace que consultaId sea nullable en la tabla tratamientos
         *   para permitir prescripciones independientes sin consulta asociada
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla temporal con la nueva estructura
                database.execSQL("""
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
                database.execSQL("""
                    INSERT INTO tratamientos_new (id, consultaId, medicamento, dosis, frecuencia, duracionDias, indicaciones)
                    SELECT id, consultaId, medicamento, dosis, frecuencia, duracionDias, indicaciones
                    FROM tratamientos
                """)
                
                // Eliminar tabla antigua
                database.execSQL("DROP TABLE tratamientos")
                
                // Renombrar tabla nueva
                database.execSQL("ALTER TABLE tratamientos_new RENAME TO tratamientos")
                
                // Recrear índice
                database.execSQL("CREATE INDEX IF NOT EXISTS index_tratamientos_consultaId ON tratamientos(consultaId)")
            }
        }

        /**
         * Migración de la versión 4 a la 5:
         * - Agrega columnas cost e isPaid a appointments
         * - Crea la tabla daily_income para tracking de ingresos
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columnas cost e isPaid a appointments
                database.execSQL("ALTER TABLE appointments ADD COLUMN cost REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE appointments ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
                
                // Crear tabla daily_income - PASO 1: Descomentado
                database.execSQL("""
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
                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_daily_income_doctorId_date 
                    ON daily_income(doctorId, date)
                """)
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
                MIGRATION_4_5
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
