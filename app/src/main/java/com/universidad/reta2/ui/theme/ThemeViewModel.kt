package com.universidad.reta2.ui.theme

import androidx.lifecycle.ViewModel
import com.universidad.reta2.data.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor() : ViewModel() {
    val themeMode = SessionManager.themeModeFlow
}
