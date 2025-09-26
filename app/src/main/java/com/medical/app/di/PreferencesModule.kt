package com.medical.app.di

import android.content.Context
import com.medical.app.util.preferences.AppPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para proporcionar dependencias relacionadas con las preferencias.
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    /**
     * Proporciona una instancia de [AppPreferences].
     * @param context Contexto de la aplicación
     * @return Instancia de [AppPreferences]
     */
    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }
}
