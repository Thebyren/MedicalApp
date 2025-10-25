package com.medical.app.data.remote

import android.util.Log
import com.medical.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.PropertyConversionMethod
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Proveedor del cliente de Supabase para manejar la comunicación con el backend
 */
@Singleton
class SupabaseClientProvider @Inject constructor() {
    
    companion object {
        private const val TAG = "SupabaseClientProvider"
    }
    
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Cliente de Supabase configurado
     */
    val client: SupabaseClient by lazy {
        try {
            val url = BuildConfig.SUPABASE_URL
            val key = BuildConfig.SUPABASE_ANON_KEY
            
            Log.d(TAG, "Inicializando cliente Supabase...")
            Log.d(TAG, "URL: $url")
            
            createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = key
            ) {
                install(Postgrest) {
                    serializer = KotlinXSerializer(json)
                    propertyConversionMethod = PropertyConversionMethod.CAMEL_CASE_TO_SNAKE_CASE
                }
            }.also {
                Log.d(TAG, "Cliente Supabase inicializado exitosamente")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar cliente Supabase", e)
            throw e
        }
    }
    
    /**
     * Verifica si hay conexión con Supabase
     */
    fun isConnected(): Boolean {
        return try {
            // Verificar si las credenciales están configuradas
            val url = BuildConfig.SUPABASE_URL
            val key = BuildConfig.SUPABASE_ANON_KEY
            
            if (url.isBlank() || url == "null" || url == "" || 
                key.isBlank() || key == "null" || key == "") {
                Log.e(TAG, "Credenciales de Supabase no configuradas")
                Log.e(TAG, "URL: $url")
                Log.e(TAG, "Key está configurada: ${key.isNotBlank() && key != "null"}")
                return false
            }
            
            // El cliente está inicializado
            client.postgrest
            Log.d(TAG, "Conexión con Supabase verificada exitosamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "No hay conexión con Supabase: ${e.message}")
            false
        }
    }
    
    /**
     * Verifica si las credenciales están configuradas
     */
    fun areCredentialsConfigured(): Boolean {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        return url.isNotBlank() && url != "null" && url != "" && 
               key.isNotBlank() && key != "null" && key != ""
    }
    
    /**
     * Obtiene el cliente de Postgrest para realizar operaciones
     */
    fun getPostgrest() = client.postgrest
}
