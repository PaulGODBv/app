import java.io.File
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
fun propiedadLocal(nombre: String): String {
    val fichero = rootProject.file("local.properties")
    if (!fichero.exists()) return ""
    return Properties().apply { fichero.inputStream().use { load(it) } }
        .getProperty(nombre)
        .orEmpty()
        .trim()
}

fun claveDeLaApi(): String {
    val clave = propiedadLocal("RETA2_API_KEY")
    require(clave.isNotBlank()) {
        "Falta RETA2_API_KEY en local.properties. Sin ella la app no puede " +
            "sincronizar con el panel. Pon el mismo valor que el .env del panel."
    }
    return clave
}

/**
 * El panel al que habla la app, que ya no es el mismo en las dos compilaciones.
 *
 * En `release` es siempre el dominio de produccion, por HTTPS. No sale de
 * `local.properties` a proposito: un APK que se reparte no puede depender de un
 * fichero que vive en una sola maquina, y el dominio no es ningun secreto.
 *
 * En `debug` se puede apuntar al panel de la maquina de desarrollo poniendo
 * `RETA2_BASE_URL` en `local.properties`. Eso sustituye a la lista de IPs
 * comentadas que habia en `ApiConfig.kt`: cada red daba una IP distinta, asi
 * que cambiarla significaba editar codigo versionado y acabar commiteando la
 * IP de casa. Si no se pone nada, el `debug` habla con produccion.
 */
val urlDeProduccion =
    "https://reta2panel.graymeadow-2656f405.mexicocentral.azurecontainerapps.io/api/"

fun urlDeDesarrollo(): String {
    val url = propiedadLocal("RETA2_BASE_URL")
    require(url.isBlank() || url.endsWith("/")) {
        "RETA2_BASE_URL tiene que terminar en barra: Retrofit lo exige en " +
            "baseUrl y, si falta, revienta al construir el cliente. " +
            "Ejemplo: http://192.168.1.10:8000/api/"
    }
    return url.ifBlank { urlDeProduccion }
}

android {
    namespace = "com.universidad.reta2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.universidad.reta2"
        minSdk = 24
        targetSdk = 34
        // Android se niega a instalar encima una version con codigo igual o
        // menor, y lo hace sin un mensaje que apunte a la causa: el estudiante
        // ve «no se pudo instalar» y nada mas. Sube SIEMPRE antes de repartir
        // un APK nuevo, aunque el cambio sea de una linea.
        //
        // 2 = primera version que se reparte de verdad: habla por HTTPS con el
        // panel desplegado, ya no escribe la clave ni las respuestas en el log,
        // la racha se deriva de los intentos (Room v20) y cada nivel tiene su
        // icono. La 1 solo existio en los telefonos de desarrollo.
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "RETA2_API_KEY", "\"${claveDeLaApi()}\"")
    }

    /**
     * La firma de release, leida de `local.properties`, que no va a git.
     *
     * Es **opcional a proposito**. Quien clone el repositorio sin el keystore
     * tiene que poder compilar: `assembleDebug` le funciona y `assembleRelease`
     * le saca un APK sin firmar, con un aviso. Exigirlo convertiria el keystore
     * del autor en un requisito para construir, que es justo lo contrario de lo
     * que debe ser un secreto.
     *
     *     RETA2_KEYSTORE=C:/ruta/a/reta2-release.jks
     *     RETA2_KEYSTORE_PASSWORD=...
     *     RETA2_KEY_ALIAS=reta2
     *     RETA2_KEY_PASSWORD=...
     *
     * **El fichero .jks se guarda fuera del repositorio y con copia aparte.**
     * Android identifica una app por su firma: si se pierde esa clave, la
     * siguiente version es, para el sistema, una aplicacion distinta, y no hay
     * forma de actualizar la que ya tengan instalada los estudiantes. No es un
     * secreto que se pueda rotar como la clave de la API.
     */
    signingConfigs {
        create("release") {
            val ruta = propiedadLocal("RETA2_KEYSTORE")
            if (ruta.isNotBlank()) {
                // Un .properties de Java trata la barra invertida como
                // ESCAPE. Android Studio escribe la ruta con barras
                // simples y al leerla Java se las come: lo que llega es
                // "C:UsersPaul..." y no existe. Se avisa aqui porque el
                // error de Gradle sin esto habla de un fichero que no se
                // parece a lo escrito y no apunta a la causa.
                require(File(ruta).isFile) {
                    "No existe el keystore en: $ruta. Si en local.properties pusiste barras invertidas, cambialas por barras normales: un .properties de Java las trata como escape y se las come."
                }
                storeFile = file(ruta)
                storePassword = propiedadLocal("RETA2_KEYSTORE_PASSWORD")
                keyAlias = propiedadLocal("RETA2_KEY_ALIAS").ifBlank { "reta2" }
                keyPassword = propiedadLocal("RETA2_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // Si no hay keystore configurado se deja sin firmar, pero se dice:
            // un APK sin firmar no se instala, y descubrirlo al ir a repartirlo
            // cuesta mas que leer una linea aqui.
            signingConfig = if (propiedadLocal("RETA2_KEYSTORE").isNotBlank()) {
                signingConfigs.getByName("release")
            } else {
                logger.warn(
                    "AVISO: sin RETA2_KEYSTORE en local.properties, el APK de " +
                        "release sale SIN FIRMAR y no se puede instalar."
                )
                null
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String", "RETA2_BASE_URL", "\"$urlDeProduccion\"")
        }

        debug {
            buildConfigField("String", "RETA2_BASE_URL", "\"${urlDeDesarrollo()}\"")
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

/**
 * Room escribe el esquema de cada version en `app/schemas/`, y esos ficheros
 * van a git.
 *
 * Sin esto no hay forma de saber que cambio entre dos versiones de la base
 * salvo leyendo las entidades a mano, que es justo como se cuela una migracion
 * olvidada. Con el esquema versionado, el cambio aparece en el diff de la
 * revision igual que cualquier otro codigo, y ademas queda lo que hace falta
 * para escribir pruebas de migracion el dia que se quieran.
 */
kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
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