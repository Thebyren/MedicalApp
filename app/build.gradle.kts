plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs")
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
        viewBinding = true
    }
    
    // Configuración de Kotlin
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Dependencias principales de Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Room components
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion") // Para integración con Paging 3
    kapt("androidx.room:room-compiler:$roomVersion")
    
    // Lifecycle components (ViewModel & LiveData)
    val lifecycleVersion = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    
    // Navigation Component
    val navVersion = "2.7.5"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    
    // Paging 3
    val pagingVersion = "3.2.1"
    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // Material Design Components
    implementation("com.google.android.material:material:1.10.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Dependencias para autenticación
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // Para encriptación segura
    implementation("com.google.code.gson:gson:2.10.1") // Para serialización/deserialización
    implementation("androidx.datastore:datastore-preferences:1.0.0") // Para almacenamiento seguro de preferencias
    
    // ===== Dependencias para pruebas =====
    
    // Dependencias básicas de JUnit
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    
    // Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") {
        // Solución para conflictos con coroutines-core
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-debug")
    }
    
    // MockK para pruebas unitarias
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-android:1.13.8")
    
    // MockK para pruebas instrumentadas
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    
    // Arquitectura Components - Testing
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // LiveData testing
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // Room testing
    testImplementation("androidx.room:room-testing:$roomVersion")
    
    // Fragment testing
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
    
    // Navigation testing
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")
    
    // Espresso para pruebas de UI
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    
    // Reglas y runners para pruebas instrumentadas
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    
    // Hilt testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.48")
    
    // MockWebServer para pruebas de red
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    
    // Turbine para probar Flows
    testImplementation("app.cash.turbine:turbine:1.0.0")
    
    // Robolectric para pruebas unitarias de Android
    testImplementation("org.robolectric:robolectric:4.10.3")
    
    // Mockito para pruebas unitarias
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    
    // Truth para aserciones más legibles
    testImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("com.google.truth:truth:1.1.5")
}