package com.medical.app.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.medical.app.R
import com.medical.app.util.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para proporcionar dependencias relacionadas con la carga de imágenes.
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    /**
     * Proporciona una instancia de [RequestManager] de Glide.
     * @param context Contexto de la aplicación
     * @return Instancia de [RequestManager]
     */
    @Provides
    @Singleton
    fun provideGlideRequestManager(@ApplicationContext context: Context): RequestManager {
        return Glide.with(context)
    }

    /**
     * Proporciona las opciones de solicitud predeterminadas para Glide.
     * @return [RequestOptions] configurado con valores predeterminados
     */
    @Provides
    @Singleton
    fun provideGlideDefaultRequestOptions(): RequestOptions {
        return RequestOptions()
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_error)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
    }

    /**
     * Proporciona una instancia de [ImageLoader].
     * @param context Contexto de la aplicación
     * @return Instancia de [ImageLoader]
     */
    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader(context)
    }
}
