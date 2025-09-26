package com.medical.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.medical.app.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clase de utilidad para cargar y almacenar en caché imágenes usando Glide.
 * Proporciona métodos para cargar imágenes desde diferentes fuentes y aplicar transformaciones.
 */
@Singleton
class ImageLoader @Inject constructor(
    private val context: Context
) {
    
    /**
     * Carga una imagen desde una URL y la muestra en un ImageView.
     * 
     * @param url URL de la imagen
     * @param imageView ImageView donde se mostrará la imagen
     * @param placeholderId ID del recurso a mostrar mientras se carga la imagen
     * @param errorId ID del recurso a mostrar si hay un error al cargar la imagen
     * @param centerCrop Si es true, la imagen se ajustará al tamaño del ImageView recortándola
     * @param circleCrop Si es true, la imagen se recortará en forma de círculo
     * @param onSuccess Callback que se llama cuando la imagen se carga exitosamente
     * @param onError Callback que se llama cuando hay un error al cargar la imagen
     */
    fun loadImage(
        url: String?,
        imageView: ImageView,
        @DrawableRes placeholderId: Int = R.drawable.ic_placeholder,
        @DrawableRes errorId: Int = R.drawable.ic_error,
        centerCrop: Boolean = true,
        circleCrop: Boolean = false,
        onSuccess: (() -> Unit)? = null,
        onError: ((String?) -> Unit)? = null
    ) {
        if (url.isNullOrEmpty()) {
            imageView.setImageResource(errorId)
            onError?.invoke("URL de imagen vacía o nula")
            return
        }

        val requestOptions = RequestOptions()
            .placeholder(placeholderId)
            .error(errorId)
            .diskCacheStrategy(DiskCacheStrategy.ALL)

        if (centerCrop) {
            requestOptions.centerCrop()
        }
        
        if (circleCrop) {
            requestOptions.circleCrop()
        }

        Glide.with(context)
            .load(url)
            .apply(requestOptions)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onError?.invoke(e?.message ?: "Error desconocido al cargar la imagen")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    onSuccess?.invoke()
                    return false
                }
            })
            .into(imageView)
    }

    /**
     * Carga una imagen desde un recurso y la muestra en un ImageView.
     * 
     * @param resourceId ID del recurso de la imagen
     * @param imageView ImageView donde se mostrará la imagen
     * @param centerCrop Si es true, la imagen se ajustará al tamaño del ImageView recortándola
     * @param circleCrop Si es true, la imagen se recortará en forma de círculo
     */
    fun loadImageFromResources(
        @DrawableRes resourceId: Int,
        imageView: ImageView,
        centerCrop: Boolean = true,
        circleCrop: Boolean = false
    ) {
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)

        if (centerCrop) {
            requestOptions.centerCrop()
        }
        
        if (circleCrop) {
            requestOptions.circleCrop()
        }

        Glide.with(context)
            .load(resourceId)
            .apply(requestOptions)
            .into(imageView)
    }

    /**
     * Carga una imagen desde un Bitmap y la muestra en un ImageView.
     * 
     * @param bitmap Bitmap a mostrar
     * @param imageView ImageView donde se mostrará la imagen
     * @param centerCrop Si es true, la imagen se ajustará al tamaño del ImageView recortándola
     * @param circleCrop Si es true, la imagen se recortará en forma de círculo
     */
    fun loadImageFromBitmap(
        bitmap: Bitmap?,
        imageView: ImageView,
        centerCrop: Boolean = true,
        circleCrop: Boolean = false
    ) {
        if (bitmap == null) {
            imageView.setImageResource(R.drawable.ic_error)
            return
        }

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)

        if (centerCrop) {
            requestOptions.centerCrop()
        }
        
        if (circleCrop) {
            requestOptions.circleCrop()
        }

        Glide.with(context)
            .load(bitmap)
            .apply(requestOptions)
            .into(imageView)
    }

    /**
     * Limpia la memoria caché de Glide.
     * Útil para liberar memoria cuando la aplicación está en segundo plano.
     */
    fun clearMemoryCache() {
        Glide.get(context).clearMemory()
    }

    /**
     * Limpa la caché en disco de Glide en un hilo en segundo plano.
     */
    fun clearDiskCache() {
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }
    
    /**
     * Cancela todas las solicitudes de carga de imágenes para un ImageView específico.
     * Útil para evitar memory leaks en RecyclerViews o ViewPagers.
     * 
     * @param imageView ImageView para el que se cancelarán las solicitudes
     */
    fun clear(imageView: ImageView) {
        Glide.with(context).clear(imageView)
    }
}
