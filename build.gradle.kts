// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.12.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.57.2")
        classpath("com.google.firebase:firebase-appdistribution-gradle:5.1.1")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.5")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.20-2.0.3")
    }
}

plugins {
    id("com.android.application") version "8.12.3" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    id("com.google.firebase.appdistribution") version "5.1.1" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.5" apply false
}
