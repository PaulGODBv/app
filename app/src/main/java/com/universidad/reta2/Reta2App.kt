package com.universidad.reta2

import android.app.Application
import com.universidad.reta2.data.preferences.SessionManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Reta2App : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
    }
}
