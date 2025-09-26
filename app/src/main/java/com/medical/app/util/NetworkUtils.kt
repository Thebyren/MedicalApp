package com.medical.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para verificar el estado de la conexión de red.
 */
@Singleton
class NetworkUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Verifica si el dispositivo tiene conexión a Internet activa.
     * @return `true` si hay conexión a Internet, `false` en caso contrario
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }
}

/**
 * Clase sellada que representa el estado de la red.
 */
sealed class NetworkState {
    /**
     * El dispositivo está conectado a Internet.
     */
    object Connected : NetworkState()

    /**
     * El dispositivo no tiene conexión a Internet.
     */
    object NotConnected : NetworkState()

    /**
     * No se pudo determinar el estado de la red.
     */
    object Unknown : NetworkState()
}
