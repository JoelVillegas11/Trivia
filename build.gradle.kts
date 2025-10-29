// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}

// build.gradle.kts (project level)
buildscript {
    // ... otros repositorios
    repositories {
        google() // Asegúrate de tener el repositorio de Google
        mavenCentral()
    }
    dependencies {
        // Debes tener esta línea con la versión actual del plugin
        classpath("com.google.gms:google-services:4.4.4") // O la versión más reciente
        // ... otras dependencias de classpath
    }
}
//...