package com.universidad.reta2.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.repositories.SessionRepository
import com.universidad.reta2.domain.repositories.CompetenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val competenceRepository: CompetenceRepository
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
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            delay(2000) // Simular carga
            val user = sessionRepository.getCurrentUser()
            _isUserLoggedIn.value = user != null
        }
    }
}
