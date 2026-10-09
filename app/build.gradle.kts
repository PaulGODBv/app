import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

/**
 * Lee la clave de la API de `local.properties`, que no va a git.
 *
 * Antes era una constante en `ApiConfig.kt`, asi que viajaba en cada commit de
 * un repositorio publico. Sacarla del codigo no la esconde del APK —sigue
 * dentro, y quien descompile la encuentra— pero evita que se filtre otra vez
 * por el historial, que es por donde se filtro.
 */
fun claveDeLaApi(): String {
    val fichero = rootProject.file("local.properties")
    val clave = if (fichero.exists()) {
        Properties().apply { fichero.inputStream().use { load(it) } }
            .getProperty("RETA2_API_KEY")
            .orEmpty()
    } else {
        ""
    }
    require(clave.isNotBlank()) {
        "Falta RETA2_API_KEY en local.properties. Sin ella la app no puede " +
            "sincronizar con el panel. Pon el mismo valor que el .env del panel."
    }
    return clave
}

android {
    namespace = "com.universidad.reta2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.universidad.reta2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "RETA2_API_KEY", "\"${claveDeLaApi()}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        // Hace falta para BUILD_CONFIG_FIELD de la clave de la API.
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    // UN SOLO BOM para todo Compose, y de aqui depende que la aplicacion no
    // se caiga. Antes convivian tres: este bloque fijaba el 2024.02.00,
    // libs.versions.toml declaraba el 2024.09.00 y las pruebas el 2023.10.01.
    // Ademas `foundation` iba clavado al 1.9.3 por fuera del BOM, asi que
    // arrastraba ui, runtime y animation al 1.9.3 mientras material3 se
    // quedaba en el 1.2.0 del BOM viejo: ano y medio de diferencia entre
    // librerias que comparten la contabilidad interna de los nodos.
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("com.google.dagger:hilt-android:2.48.1")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation(libs.play.services.dtdi)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.compose.foundation:foundation")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    //Hilt
    implementation ("com.google.dagger:hilt-android:2.48")
    kapt ("com.google.dagger:hilt-compiler:2.48")
    implementation ("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")
// Para ViewModel con Hilt
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
// Para coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil
    implementation(libs.coil.compose)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}