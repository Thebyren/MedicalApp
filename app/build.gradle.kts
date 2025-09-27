plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.medical.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.medical.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Habilitar el uso de Java 8 features
        vectorDrawables.useSupportLibrary = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    // Habilitar el uso de ViewBinding
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    
    // Configuración de Kotlin
    kotlinOptions {
        jvmTarget = "11"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Dependencias principales de Android
    implementation(libs.hilt.android)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.paging.runtime.ktx)
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
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation("com.google.code.gson:gson:2.10.1")
}