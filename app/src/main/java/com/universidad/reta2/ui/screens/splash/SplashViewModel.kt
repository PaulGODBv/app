package com.universidad.reta2.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.repositories.SessionRepository
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val competenceRepository: CompetenceRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    suspend fun initializeAppData() {
        println("🔧 Inicializando datos de la app...")
        try {
            val competences = competenceRepository.getAllCompetences()
            println("🎉 Datos inicializados exitosamente: ${competences.size} competencias")

            // Diagnóstico
            competences.forEach { competence ->
                println("   📊 Competencia ${competence.id}: ${competence.levels.size} niveles")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando datos: ${e.message}")
        }
    }
    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    init {
        // Inicializar datos y verificar sesión en paralelo
        viewModelScope.launch {
            initializeAppData() // Aseguramos que los datos base existan
            checkUserSession()
        }
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            delay(2000) // Simular carga (branding)

            // 1. Preguntar a la sesión (SharedPreferences)
            val sessionUser = sessionRepository.getCurrentUser()

            if (sessionUser != null) {

                val dbUser = userRepository.getUserByUsername(sessionUser.username)

                if (dbUser != null) {

                    _isUserLoggedIn.value = true
                } else {

                    sessionRepository.clearSession()
                    _isUserLoggedIn.value = false
                }
            } else {
                // No hay sesión
                _isUserLoggedIn.value = false
            }
        }
    }
}
