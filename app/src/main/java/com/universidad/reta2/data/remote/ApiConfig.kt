package com.universidad.reta2.data.remote

import com.universidad.reta2.BuildConfig

object ApiConfig {
    // IP Casa
    const val BASE_URL = "http://192.168.20.72:8000/api/"


    //Datos
    //const val BASE_URL = "http://10.81.235.97:8000/api/"

    //Acceso Movil PC sala de informatica
    //const val BASE_URL = "http://192.168.137.24:8000/api/"

    //Udes Campus Data wifi
    //const val BASE_URL = "http://10.10.16.97:8000/api/"


    //const val BASE_URL = "http://10.20.9.41:8000/api/"



    const val TIMEOUT_SECONDS = 30L

    /**
     * Clave compartida con el panel (cabecera X-API-Key).
     *
     * Debe coincidir con RETA2_API_KEY del `.env` del panel. Cierra el endpoint
     * a quien no tenga la app, pero no identifica al estudiante: va dentro del
     * APK y puede extraerse descompilandolo.
     *
     * **No se escribe aqui.** Viene de `local.properties` por `buildConfigField`
     * (ver `app/build.gradle.kts`). Estuvo escrita en esta linea hasta el
     * 05/10/2026, y por eso acabo en el historial publico del repositorio.
     */
    val API_KEY: String = BuildConfig.RETA2_API_KEY

    const val API_KEY_HEADER = "X-API-Key"
}