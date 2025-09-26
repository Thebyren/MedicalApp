package com.medical.app.di

import com.medical.app.util.AuthManager
import com.medical.app.util.security.PasswordHasher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para proporcionar dependencias relacionadas con la autenticación.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /**
     * Proporciona una instancia de [AuthManager].
     */
    @Provides
    @Singleton
    fun provideAuthManager(
        sessionManager: com.medical.app.data.local.SessionManager,
        passwordHasher: PasswordHasher
    ): AuthManager {
        return AuthManager(sessionManager)
    }
}
