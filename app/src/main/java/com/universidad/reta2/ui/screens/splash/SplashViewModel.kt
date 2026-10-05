package com.universidad.reta2.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.remote.NetworkChecker
import com.universidad.reta2.data.repositories.ContentSyncRepository
import com.universidad.reta2.data.repositories.SyncRepository
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
    private val userRepository: UserRepository,
    private val syncRepository: SyncRepository,
    private val contentSyncRepository: ContentSyncRepository,
    private val networkChecker: NetworkChecker
) : ViewModel() {

    private companion object {
        /**
         * Los niveles del panel que hay que bajarse, por su id de Django.
         *
         * Se derivan del mapa de traduccion de `ContentSyncRepository` en vez
         * de escribirse aparte. Antes esto era un `(1..13)` fijo, y al dar de
         * alta Comunicacion Escrita —nivel 18— la sincronizacion siguio
         * pidiendo del 1 al 13 y la competencia nueva llego vacia. Con una
         * sola lista, anadir un nivel es tocar un sitio y no dos.
         *
         * Sigue siendo una lista local: cuando el catalogo se descargue del
         * panel (TODO-1, paso 4), esto sale de ahi.
         */
        val ID_DE_NIVELES = ContentSyncRepository.NIVEL_DJANGO_A_APP.keys.sorted()
    }

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    // NUEVO: estado de sincronización para mostrar feedback
    private val _syncResult = MutableStateFlow<Result<String>?>(null)
    val syncResult = _syncResult.asStateFlow()

    // NUEVO: estado de conectividad
    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Checking)
    val networkState = _networkState.asStateFlow()

    sealed class NetworkState {
        object Checking : NetworkState()
        object Connected : NetworkState()
        object Disconnected : NetworkState()
    }

    suspend fun initializeAppData() {
        try {
            val competences = competenceRepository.getAllCompetences()
            println("🎉 Datos inicializados: ${competences.size} competencias")
        } catch (e: Exception) {
            println("❌ Error inicializando datos: ${e.message}")
        }
    }

    init {
        viewModelScope.launch {
            initializeAppData()
            checkConnectivityAndSession()
        }
    }

    private fun checkConnectivityAndSession() {
        viewModelScope.launch {
            delay(2000)

            // Verificar conectividad
            if (networkChecker.isConnected()) {
                _networkState.value = NetworkState.Connected
            } else {
                _networkState.value = NetworkState.Disconnected
                // No navegamos aún, esperamos decisión del usuario
                return@launch
            }

            // Si hay conexión, verificar sesión y sincronizar
            proceedWithSession()
        }
    }

    fun proceedOffline() {
        // Usuario eligió continuar sin conexión
        viewModelScope.launch {
            proceedWithSession()
        }
    }

    private suspend fun proceedWithSession() {
        val sessionUser = sessionRepository.getCurrentUser()

        if (sessionUser != null) {
            val dbUser = userRepository.getUserByUsername(sessionUser.username)

            if (dbUser != null) {
                // Sync en background solo si hay conexión
                if (networkChecker.isConnected()) {
                    viewModelScope.launch {
                        // Subir el progreso y bajar el contenido son dos cosas
                        // distintas: lo primero manda lo que hizo el
                        // estudiante, lo segundo trae lo que publico el panel.
                        val result = syncRepository.syncToServer()
                        _syncResult.value = result

                        // El contenido se baja aparte y con la vida del
                        // proceso, no la de esta pantalla: el splash dura dos
                        // segundos y cancelaba la descarga a mitad.
                        contentSyncRepository.sincronizarEnSegundoPlano(ID_DE_NIVELES)
                    }
                }
                _isUserLoggedIn.value = true
            } else {
                sessionRepository.clearSession()
                _isUserLoggedIn.value = false
            }
        } else {
            _isUserLoggedIn.value = false
        }
    }
}