package com.medical.app.di

import com.medical.app.util.security.PasswordHasher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para proporcionar dependencias relacionadas con la seguridad.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    /**
     * Proporciona una instancia de [PasswordHasher].
     */
    @Provides
    @Singleton
    fun providePasswordHasher(): PasswordHasher {
        return PasswordHasher()
    }
}
