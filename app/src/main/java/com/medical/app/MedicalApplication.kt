package com.medical.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase de aplicación personalizada para inicializar componentes globales.
 * Hilt generará código para esta clase anotada con @HiltAndroidApp.
 */
@HiltAndroidApp
class MedicalApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // La base de datos se inicializará bajo demanda a través de Hilt
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // La base de datos se cerrará automáticamente cuando la aplicación se cierre
    }
}
