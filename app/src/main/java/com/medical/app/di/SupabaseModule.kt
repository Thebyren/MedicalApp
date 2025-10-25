package com.medical.app.di

import com.medical.app.data.remote.SupabaseClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar dependencias relacionadas con Supabase
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    
    /**
     * Proporciona una instancia única de SupabaseClientProvider
     */
    @Provides
    @Singleton
    fun provideSupabaseClientProvider(): SupabaseClientProvider {
        return SupabaseClientProvider()
    }
}
