package com.universidad.reta2.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.DailyProgress
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.usecases.GetCompetencesUseCase
import com.universidad.reta2.domain.repositories.UserStatsRepository
import com.universidad.reta2.data.remote.NetworkChecker
import com.universidad.reta2.data.repositories.RankingRepository
import com.universidad.reta2.data.repositories.SyncRepository
import com.universidad.reta2.data.remote.dto.RankingResponse
import com.universidad.reta2.data.preferences.SessionManager
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class ProgressState(
    val isLoading: Boolean = true,
    val userStats: UserStats? = null,
    val competences: List<Competence> = emptyList(),
    val weeklyProgress: List<DailyProgress> = emptyList(),
    val ranking: RankingResponse? = null,
    // El ranking es lo unico de esta pantalla que sale del panel, asi que
    // lleva su propio estado: el resto se pinta de inmediato desde Room.
    val rankingCargando: Boolean = true,
    val rankingFallo: Boolean = false,
    val error: String? = null,
    // Sincronización manual: la automática vive en el splash.
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val syncFailed: Boolean = false
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getUserStatsUseCase: GetUserStatsUseCase,
    private val getCompetencesUseCase: GetCompetencesUseCase,
    private val userStatsRepository: UserStatsRepository,
    private val rankingRepository: RankingRepository,
    private val syncRepository: SyncRepository,
    private val networkChecker: NetworkChecker,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ESTADO SEGURO CON PROTECCIONES
    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    private var isViewModelActive = true

    private companion object {
        /** Lo que se espera al panel por el ranking antes de rendirse. */
        const val ESPERA_RANKING_MS = 8_000L
    }

    // La pantalla muestra el estado vacío cuando no hay competencias. Si se quita
    // el "cargando" al recibir solo las estadísticas, durante unos milisegundos se
    // ve ese estado vacío antes de que lleguen las competencias. Por eso se espera
    // a que ambas cargas terminen.
    private var statsLoaded = false
    private var localDataLoaded = false

    init {
        println(" ProgressViewModel INIT")
        // Reset explícito del estado al iniciar
        _state.value = ProgressState(isLoading = true)
        loadProgressData()
    }

    // ACTIVACIÓN SEGURA
    fun activate() {
        println(" Activando ProgressViewModel")
        isViewModelActive = true
    }

    // CARGA DE DATOS CON PROTECCIÓN
    fun loadProgressData() {
        if (!isViewModelActive) {
            println(" ViewModel no activo, ignorando carga")
            return
        }

        viewModelScope.launch {
            try {
                println(" ProgressViewModel: Iniciando carga de datos...")

                statsLoaded = false
                localDataLoaded = false
                _state.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        rankingCargando = true,
                        rankingFallo = false
                    )
                }

                val username = SessionManager.getCurrentUsername(context) ?: ""

                // 1. Cargar estadísticas de forma reactiva (Inicia de inmediato)
                val statsJob = launch {
                    getUserStatsUseCase().collect { userStats ->
                        if (isViewModelActive) {
                            println(" ProgressViewModel: Stats actualizados recibidos")
                            _state.update {
                                it.copy(
                                    userStats = userStats,
                                    error = null
                                )
                            }
                            statsLoaded = true
                            finishLoadingIfReady()
                        }
                    }
                }

                // 2. Cargar datos locales pesados en segundo plano
                launch {
                    try {
                        val competences = getCompetencesUseCase()
                        val weeklyProgress = userStatsRepository.getWeeklyProgress()
                        if (isViewModelActive) {
                            _state.update {
                                it.copy(
                                    competences = competences,
                                    weeklyProgress = weeklyProgress
                                )
                            }
                        }
                    } catch (e: Exception) {
                        println("Error cargando datos locales: ${e.message}")
                    } finally {
                        // También en caso de error: si no se marca, la pantalla
                        // se quedaría cargando para siempre.
                        localDataLoaded = true
                        if (isViewModelActive) finishLoadingIfReady()
                    }
                }

                // 3. Cargar Ranking en segundo plano (No bloquea el resto)
                if (username.isNotEmpty()) {
                    launch {
                        try {
                            // El ranking es decoracion: si el panel no contesta
                            // pronto, se abandona. El timeout de red son 30 s y
                            // dejar el hueco girando medio minuto es peor que
                            // decir que no se pudo.
                            val rankingResult = withTimeoutOrNull(ESPERA_RANKING_MS) {
                                rankingRepository.getGlobalRanking(username).getOrNull()
                            }
                            if (isViewModelActive) {
                                if (rankingResult != null) {
                                    println(" ProgressViewModel: Ranking cargado exitosamente")
                                    _state.update {
                                        it.copy(
                                            ranking = rankingResult,
                                            rankingCargando = false,
                                            rankingFallo = false
                                        )
                                    }
                                } else {
                                    // Se reserva el sitio y se explica: la tarjeta ya
                                    // ocupa espacio, desaparecer seria peor que avisar.
                                    _state.update { it.copy(rankingCargando = false, rankingFallo = true) }
                                }
                            }
                        } catch (e: Exception) {
                            println("⚠️ Error cargando ranking: ${e.message}")
                            if (isViewModelActive) {
                                _state.update { it.copy(rankingCargando = false, rankingFallo = true) }
                            }
                        }
                    }
                } else {
                    // Sin usuario no hay ranking que pedir: no se deja el hueco cargando.
                    _state.update { it.copy(rankingCargando = false) }
                }

            } catch (e: Exception) {
                if (isViewModelActive) {
                    println(" ❌ Error crítico en ProgressViewModel: ${e.message}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Error al cargar datos"
                        )
                    }
                }
            }
        }
    }

    /** Quita el indicador de carga solo cuando ambas fuentes locales respondieron. */
    private fun finishLoadingIfReady() {
        if (statsLoaded && localDataLoaded) {
            _state.update { it.copy(isLoading = false) }
        }
    }

    // FORMATO SEGURO DE TIEMPO
    /**
     * Sincronización manual con el panel.
     *
     * Usa el mismo [SyncRepository.syncToServer] que el splash ejecuta de forma
     * automática al abrir la app. Comprobar la conexión en cada pantalla no
     * compensa en una app de este tamaño, así que aquí la comprobación solo
     * ocurre cuando el usuario pulsa el botón.
     */
    fun syncNow() {
        if (_state.value.isSyncing) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSyncing = true,
                syncMessage = null,
                syncFailed = false
            )

            if (!networkChecker.isConnected()) {
                _state.value = _state.value.copy(
                    isSyncing = false,
                    syncMessage = "Sin conexión. Tu progreso sigue guardado en el teléfono.",
                    syncFailed = true
                )
                return@launch
            }

            val resultado = syncRepository.syncToServer()

            _state.value = _state.value.copy(
                isSyncing = false,
                syncMessage = resultado.fold(
                    onSuccess = { "Progreso enviado al panel." },
                    onFailure = { "No se pudo sincronizar: ${it.message ?: "error desconocido"}" }
                ),
                syncFailed = resultado.isFailure
            )
        }
    }

    fun getFormattedPracticeTime(): String {
        val totalSeconds = state.value.userStats?.dailyPracticeTime ?: 0
        return when {
            totalSeconds >= 3600 -> {
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                "$hours hr $minutes min $seconds seg"
            }
            totalSeconds >= 60 -> {
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                "$minutes min $seconds seg"
            }
            else -> "$totalSeconds seg"
        }
    }

    // LIMPIEZA SEGURA
    fun cleanup() {
        println(" Limpiando ProgressViewModel")
        isViewModelActive = false
    }

    override fun onCleared() {
        println(" ProgressViewModel siendo destruido")
        super.onCleared()
        isViewModelActive = false
    }
}