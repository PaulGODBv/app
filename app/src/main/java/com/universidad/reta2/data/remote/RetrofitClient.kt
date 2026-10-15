package com.universidad.reta2.data.remote

import com.universidad.reta2.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * El registro de red, y solo en desarrollo.
     *
     * Estuvo en `BODY` para todas las compilaciones hasta el 14/10/2026, y en
     * el APK de release eso escribia en el log del telefono dos cosas que no
     * pueden estar ahi:
     *
     * - **La cabecera `X-API-Key`**, en claro. Se roto esa clave y se saco del
     *   historial de git precisamente para protegerla; dejarla en el log la
     *   devuelve a ser legible sin descompilar nada.
     * - **El cuerpo de las respuestas**, que incluye `correct_option_order`.
     *   Es decir, la respuesta correcta de cada pregunta del banco.
     *
     * Se comprobo leyendolo con `adb logcat` desde el APK de release, **sin
     * root**: a un estudiante le basta activar la depuracion USB. Para una
     * aplicacion que mide aprendizaje en un piloto eso no es una fuga mas, es
     * que la medicion deja de significar algo.
     *
     * `BuildConfig.DEBUG` es false en release y true en debug, asi que en
     * desarrollo se sigue viendo todo, que es donde hace falta.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // Anade la clave de API a toda peticion: el panel rechaza con 403 las que
    // llegan sin ella.
    private val apiKeyInterceptor = Interceptor { chain ->
        val peticion = chain.request()
            .newBuilder()
            .addHeader(ApiConfig.API_KEY_HEADER, ApiConfig.API_KEY)
            .build()
        chain.proceed(peticion)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    val instance: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}