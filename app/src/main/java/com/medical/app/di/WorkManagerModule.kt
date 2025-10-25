package com.medical.app.di

import android.content.Context
import androidx.work.WorkManager
import com.medical.app.data.repository.SyncRepository
import com.medical.app.sync.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para proporcionar dependencias relacionadas con WorkManager
 */
@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
    
    /**
     * Proporciona una instancia de WorkManager
     */
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    /**
     * Proporciona una instancia de SyncManager
     */
    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        syncRepository: SyncRepository
    ): SyncManager {
        return SyncManager(context, syncRepository)
    }
}
