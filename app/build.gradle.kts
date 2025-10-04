plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.hilt.android)
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.medical.app"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.medical.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Habilitar el uso de Java 8 features
        vectorDrawables.useSupportLibrary = true
        
        // Leer API key de Gemini desde local.properties
        val properties = org.jetbrains.kotlin.konan.properties.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }
        val geminiApiKey = properties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    // Habilitar el uso de ViewBinding
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation("com.google.android.material:material:1.13.0")
    // Dependencias principales de Android
    implementation(libs.hilt.android)

    implementation(libs.androidx.navigation.ui.ktx)
    ksp(libs.hilt.compiler)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Glide for image loading
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    
    // Room components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation("androidx.paging:paging-runtime-ktx:3.3.6")
    ksp(libs.androidx.room.compiler)
    
    // Lifecycle components (ViewModel & LiveData)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    
    // Navigation Component
    // Navigation Component - Replace with catalog aliases 
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.security.crypto)
    implementation(libs.security.crypto.ktx) 
    
    // ... 
    
    // Navigation testing - Replace with catalog alias 
    androidTestImplementation(libs.navigation.testing)
    
    // Espresso para pruebas de UI
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.intents)
    
    // Reglas y runners para pruebas instrumentadas
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.runner)
    
    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    
    // MockWebServer para pruebas de red
    testImplementation(libs.mockwebserver)
    
    // Turbine para probar Flows
    testImplementation(libs.turbine)
    
    // Robolectric para pruebas unitarias de Android
    testImplementation(libs.robolectric)
    
    // Mockito para pruebas unitarias
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    
    // Truth para aserciones más legibles
    testImplementation(libs.google.truth)
    androidTestImplementation(libs.google.truth)

    // SwipeRefreshLayout
    implementation(libs.swiperefreshlayout)

    // web services
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    
    // Gemini AI
    implementation(libs.generativeai)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}