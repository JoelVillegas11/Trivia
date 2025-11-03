plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.project6electiva.trivia"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.project6electiva.trivia"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            postprocessing {
                // Habilitar la reducción de recursos (shrinkResources)
                isRemoveUnusedResources = true

                // Otras opciones de ProGuard si las necesitas
                // isOptimizeCode = true
                // isCompressAssets = true
            }
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
}

dependencies {

    // 1. DEPENDENCIAS DE ANDROID (con libs, asumimos que usas Version Catalogs)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 2. ELIMINAR ESTAS LÍNEAS DUPLICADAS/FIJAS si ya usas 'libs' arriba:
    // implementation("androidx.appcompat:appcompat:1.6.1")
    // implementation("com.google.android.material:material:1.10.0")
    // implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // -----------------------------------------------------------------
    // 3. FIREBASE: Usar el BoM UNA SOLA VEZ (con la versión más reciente)
    implementation(platform("com.google.firebase:firebase-bom:34.4.0")) // <-- ÚNICA DECLARACIÓN BOM
    // -----------------------------------------------------------------

    // 4. Dependencias de Firebase SIN ESPECIFICAR VERSIÓN (el BoM lo maneja)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")       // <-- Sin versión
    implementation("com.google.firebase:firebase-firestore")  // <-- Sin versión

    //Google
    implementation("androidx.credentials:credentials:1.6.0-beta03")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0-beta03")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    // Opcional: para imágenes si usas Storage
    // implementation("com.google.firebase:firebase-storage")
}
