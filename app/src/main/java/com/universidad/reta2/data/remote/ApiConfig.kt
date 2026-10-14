package com.universidad.reta2.data.remote

import com.universidad.reta2.BuildConfig

object ApiConfig {
    /**
     * El panel al que habla la app.
     *
     * Hasta el 14/10/2026 esto era una constante con la IP de la LAN y, debajo,
     * cinco IPs mas comentadas —casa, datos, la sala de informatica, dos wifis
     * del campus—. Cambiar de red significaba editar codigo versionado, asi que
     * las IPs acabaron en el repositorio y la de turno viajaba en cada commit.
     *
     * Ahora depende de la compilacion y entra por `buildConfigField` (ver
     * `app/build.gradle.kts`):
     *
     * - `release` habla con el dominio de produccion, **por HTTPS**.
     * - `debug` habla con lo que diga `RETA2_BASE_URL` en `local.properties`,
     *   que no va a git, y con produccion si no se pone nada.
     *
     * Y esto es lo que de verdad cierra el agujero: mientras se hablaba en
     * claro, la cabecera `X-API-Key` viajaba legible por la red local y se
     * podia capturar con un analizador sin descompilar nada. Rotar la clave
     * nunca lo arreglo, porque el problema no era la clave sino el transporte.
     */
    val BASE_URL: String = BuildConfig.RETA2_BASE_URL

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
