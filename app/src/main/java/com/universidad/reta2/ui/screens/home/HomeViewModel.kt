package com.universidad.reta2.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import com.universidad.reta2.domain.usecases.GetUserStatsUseCase
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.data.preferences.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val competenceRepository: CompetenceRepository,
    private val getUserStatsUseCase: GetUserStatsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _competences = MutableStateFlow<List<Competence>>(emptyList())
    val competences: StateFlow<List<Competence>> = _competences.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // `UserStats()` vale cero en todos sus campos, y una racha de cero días es
    // justo lo que dispara el aviso de racha en peligro. Sin esta bandera el
    // aviso se asomaba un instante en cada entrada, antes de que llegara el
    // primer valor real de Room.
    private val _statsCargadas = MutableStateFlow(false)
    val statsCargadas: StateFlow<Boolean> = _statsCargadas.asStateFlow()

    init {
        resetAndLoad()
    }

    private fun resetAndLoad() {
        _userName.value = ""
        _userStats.value = UserStats()
        _statsCargadas.value = false
        _competences.value = emptyList()
        _isLoading.value = true

        loadUserData()
        // La carga arranca aqui, no solo en el ON_RESUME de la pantalla. Dentro
        // de un NavHost el ciclo de vida de la entrada no llega a RESUMED hasta
        // que termina la transicion entre destinos —unos 400 ms—, asi que pedir
        // los datos alli dejaba a Inicio en blanco justo el tiempo suficiente
        // para que venciera el umbral del esqueleto. Con el catalogo en cache
        // esto se resuelve en milisegundos y el esqueleto ya no llega a salir.
        loadCompetences()
        loadUserStats()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _userName.value = SessionManager.getCurrentUsername(context) ?: "Usuario"
        }
    }

    fun loadCompetences() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val loadedCompetences = competenceRepository.getAllCompetences()


                _competences.value = loadedCompetences
            } catch (e: Exception) {
                _competences.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private var trabajoStats: Job? = null

    fun loadUserStats() {
        // Cada entrada a Inicio llamaba a este metodo y dejaba vivo el colector
        // anterior: tras unas cuantas idas y venidas habia media docena
        // escuchando la misma tabla de Room.
        trabajoStats?.cancel()
        trabajoStats = viewModelScope.launch {
            try {
                // Usamos collectLatest para asegurar que solo procesamos el último valor
                getUserStatsUseCase().collectLatest { stats ->
                    _userStats.value = stats
                    _statsCargadas.value = true
                }
            } catch (e: CancellationException) {
                // Cancelar es lo normal aqui: lo hace la propia recarga. Si se
                // colara en el catch de abajo, las estadisticas volverian a
                // cero y con ellas el aviso de racha en peligro.
                throw e
            } catch (e: Exception) {
                _userStats.value = UserStats()
                _statsCargadas.value = true
            }
        }
    }

    /**
     * Las competencias que enseña «Continuar practicando», de más reciente a
     * más antigua.
     *
     * Antes filtraba solo por `totalProgress > 0 && < 1`, y ese progreso sale
     * de los intentos acertados. Como la práctica **no registra intentos** —a
     * propósito, para no mover el porcentaje ni el desbloqueo—, nada de lo
     * practicado aparecía aquí: la sección enseñaba una competencia vieja de
     * evaluación e ignoraba todo lo recién hecho.
     *
     * Ahora entra lo que se ha tocado hace poco **o** lo que está a medias, y
     * manda la actividad para ordenar. Una competencia ya completada que se
     * acaba de repasar también sale: la sección habla de lo que estás
     * haciendo, no de lo que te falta.
     */
    fun getCompetencesWithProgress(): List<Competence> {
        fun actividad(c: Competence) = c.levels.maxOfOrNull { it.ultimaPractica } ?: 0L

        return _competences.value
            .filter { actividad(it) > 0L || (it.totalProgress > 0f && it.totalProgress < 1f) }
            .sortedByDescending { actividad(it) }
    }

    fun getCompletedCompetencesCount(): Int {
        // Esta lógica ya usa 'totalProgress', así que está bien.
        return _competences.value.count { it.totalProgress >= 1f }
    }
}