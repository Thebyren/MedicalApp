package com.medical.app.di

import android.content.Context
import com.medical.app.data.dao.AppointmentDao
import com.medical.app.data.dao.ConsultaDao
import com.medical.app.data.dao.DailyIncomeDao
import com.medical.app.data.dao.HistorialMedicoDao
import com.medical.app.data.dao.MedicoDao
import com.medical.app.data.dao.MedicoPacienteDao
import com.medical.app.data.dao.PacienteDao
import com.medical.app.data.dao.TratamientoDao
import com.medical.app.data.dao.UsuarioDao
import com.medical.app.data.database.AppDatabase
import com.medical.app.data.repository.AppointmentRepository
import com.medical.app.data.repository.AuthRepository
import com.medical.app.data.repository.ConsultaRepository
import com.medical.app.data.repository.DailyIncomeRepository
import com.medical.app.data.repository.MedicoRepository
import com.medical.app.data.repository.PacienteRepository
import com.medical.app.data.repository.TratamientoRepository
import com.medical.app.util.security.PasswordHasher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para la inyección de dependencias relacionadas con la base de datos.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    // DAOs
    @Provides
    fun provideUsuarioDao(database: AppDatabase): UsuarioDao = database.usuarioDao()

    @Provides
    fun provideMedicoDao(database: AppDatabase): MedicoDao = database.medicoDao()

    @Provides
    fun providePacienteDao(database: AppDatabase): PacienteDao = database.pacienteDao()

    @Provides
    fun provideMedicoPacienteDao(database: AppDatabase): MedicoPacienteDao = database.medicoPacienteDao()

    @Provides
    fun provideConsultaDao(database: AppDatabase): ConsultaDao = database.consultaDao()

    @Provides
    fun provideTratamientoDao(database: AppDatabase): TratamientoDao = database.tratamientoDao()

    @Provides
    fun provideHistorialMedicoDao(database: AppDatabase): HistorialMedicoDao = database.historialMedicoDao()

    @Provides
    fun provideAppointmentDao(database: AppDatabase): AppointmentDao = database.appointmentDao()

    @Provides
    fun provideDailyIncomeDao(database: AppDatabase): DailyIncomeDao = database.dailyIncomeDao()  // PASO 2: Descomentado

    // Repositorios
    @Provides
    @Singleton
    fun provideAuthRepository(
        usuarioDao: UsuarioDao, 
        pacienteDao: PacienteDao,
        passwordHasher: PasswordHasher
    ): AuthRepository {
        return AuthRepository(
            usuarioDao,
            pacienteDao,
            passwordHasher
        )
    }

    @Provides
    @Singleton
    fun provideConsultaRepository(consultaDao: ConsultaDao): ConsultaRepository {
        return ConsultaRepository(consultaDao)
    }

    @Provides
    @Singleton
    fun provideMedicoRepository(medicoDao: MedicoDao): MedicoRepository {
        return MedicoRepository(medicoDao)
    }

    @Provides
    @Singleton
    fun providePacienteRepository(pacienteDao: PacienteDao): PacienteRepository {
        return PacienteRepository(pacienteDao)
    }

    @Provides
    @Singleton
    fun provideTratamientoRepository(tratamientoDao: TratamientoDao): TratamientoRepository {
        return TratamientoRepository(tratamientoDao)
    }

    @Provides
    @Singleton
    fun provideDailyIncomeRepository(dailyIncomeDao: DailyIncomeDao): DailyIncomeRepository {
        return DailyIncomeRepository(dailyIncomeDao)
    }  // PASO 3: Descomentado

    @Provides
    @Singleton
    fun provideAppointmentRepository(
        appointmentDao: AppointmentDao,
        dailyIncomeRepository: DailyIncomeRepository  // PASO 3: Descomentado
    ): AppointmentRepository {
        return AppointmentRepository(appointmentDao, dailyIncomeRepository)
    }
}
