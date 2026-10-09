package com.universidad.reta2

import android.app.Application
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.utils.Sonidos
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Reta2App : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        // Los efectos se descomprimen aqui y no en el primer uso: `load()`
        // es asincrono, y pedir que suene antes de que termine no falla,
        // simplemente no se oye.
        Sonidos.init(this)
    }
}
