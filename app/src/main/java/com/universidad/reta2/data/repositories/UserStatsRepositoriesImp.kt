package com.universidad.reta2.data.repositories

import com.universidad.reta2.data.local.EstadisticasCache
import com.universidad.reta2.data.local.dao.UserStatsDao
import com.universidad.reta2.data.local.entities.UserStatsEntity
import com.universidad.reta2.data.local.mappers.UserStatsMapper
import com.universidad.reta2.domain.models.DailyProgress
import com.universidad.reta2.domain.models.UserStats
import com.universidad.reta2.domain.repositories.UserStatsRepository
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.services.StatsInitializer
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UserStatsRepositoriesImp @Inject constructor(
    private val userStatsDao: UserStatsDao,
    private val statsInitializer: StatsInitializer,
    private val estadisticasCache: EstadisticasCache,
    @ApplicationContext private val context: Context
) : UserStatsRepository {

    override fun getUserStats(): Flow<UserStats> {
        val username = getCurrentUsername()
        return userStatsDao.getUserStats(username)
            .map { entity -> entity?.let { UserStatsMapper.toDomain(it) } ?: UserStats() }
            .map { alDia(it) }
            .onEach { estadisticasCache.guardar(username, it) }
            // Lo ultimo que se supo va por delante: sin esto la tarjeta de
            // Inicio nace en cero y salta a las cifras buenas unos cientos de
            // milisegundos despues. Room confirma o corrige acto seguido.
            // Se vuelve a pasar por `conTiempoDelDia` porque lo guardado pudo
            // quedar de ayer si la app lleva abierta desde antes de medianoche.
            .onStart { estadisticasCache.obtener(username)?.let { emit(alDia(it)) } }
    }

    /**
     * `dailyPracticeTime` es el tiempo de practica **de hoy**, pero en la base
     * solo se suma: nadie lo pone a cero al cambiar el dia, asi que el valor
     * guardado se queda con el del ultimo dia en que se practico. Aqui se
     * corrige al leer, comparando contra `lastPracticeDate`.
     *
     * Corregir al leer y no al escribir tiene dos ventajas: no hace falta nadie
     * que vigile el cambio de dia, y el valor se recalcula en cada emision, asi
     * que una app abierta cuando pasa la medianoche tambien acaba enterandose.
     * El valor guardado se pone al dia solo: la primera practica del dia nuevo
     * parte de este cero y escribe encima.
     */
    private fun conTiempoDelDia(stats: UserStats): UserStats =
        if (stats.lastPracticeDate == getCurrentDate()) stats
        else stats.copy(dailyPracticeTime = 0)

    /**
     * La racha tenia el mismo problema que el tiempo del dia, y peor: solo se
     * recalculaba al escribir. Quien practicaba tres dias seguidos y luego
     * dejaba de entrar seguia viendo «Racha 3 dias» indefinidamente, y el
     * numero caia a 1 sin explicacion en cuanto volvia a jugar.
     *
     * La regla es la misma que aplica [shouldIncrementStreak] al escribir, solo
     * que mirada desde la lectura:
     *
     * - practicó hoy      -> la racha vale lo guardado;
     * - practicó ayer     -> sigue viva: aun se puede mantener practicando hoy;
     * - antes, o nunca    -> se rompió, vale cero.
     *
     * Igual que con el tiempo, no se toca lo guardado: la siguiente practica lo
     * reescribe y mientras tanto la pantalla dice la verdad.
     */
    private fun conRachaDelDia(stats: UserStats): UserStats {
        if (stats.lastPracticeDate.isEmpty()) {
            return stats.copy(currentStreakDays = 0)
        }

        val hoy = LocalDate.parse(getCurrentDate())
        val ultima = try {
            LocalDate.parse(stats.lastPracticeDate)
        } catch (e: Exception) {
            // Fecha ilegible: se trata como si no hubiera racha en lugar de
            // tumbar la pantalla entera por un dato corrupto.
            return stats.copy(currentStreakDays = 0)
        }

        val sigueViva = ultima == hoy || ultima.plusDays(1) == hoy
        return if (sigueViva) stats else stats.copy(currentStreakDays = 0)
    }

    /**
     * Único punto de escritura de las estadísticas.
     *
     * Antes de guardar sube las marcas históricas. Se comparan contra lo que ya
     * hay en la base y no solo contra lo que trae quien llama: así, aunque
     * alguien pase un `UserStats()` recién construido —con las marcas en cero—,
     * no se pierde un logro ya conseguido. Las marcas solo suben.
     */
    private suspend fun guardar(entity: UserStatsEntity) {
        val guardadas: UserStatsEntity? =
            runCatching { userStatsDao.getUserStatsSync(entity.username) }.getOrNull()

        userStatsDao.updateUserStats(
            entity.copy(
                maxStreakDays = maxOf(
                    entity.maxStreakDays,
                    entity.currentStreakDays,
                    guardadas?.maxStreakDays ?: 0
                ),
                maxDailyPracticeTime = maxOf(
                    entity.maxDailyPracticeTime,
                    entity.dailyPracticeTime,
                    guardadas?.maxDailyPracticeTime ?: 0
                )
            )
        )
    }

    /** Las dos correcciones de fecha que se aplican a todo lo que se lee. */
    private fun alDia(stats: UserStats): UserStats = conRachaDelDia(conTiempoDelDia(stats))

    override suspend fun getUserStatsOnce(): UserStats {
        val username = getCurrentUsername()
        statsInitializer.initializeUserStats(username)
        return alDia(UserStatsMapper.toDomain(userStatsDao.getUserStatsSync(username)))
    }

    override suspend fun updateUserStats(stats: UserStats) {
        val username = getCurrentUsername()
        statsInitializer.initializeUserStats(username)

        val entity = UserStatsMapper.toEntity(stats, username)
        guardar(entity)
    }

    override suspend fun addQuestionsAnswered(count: Int) {
        val username = getCurrentUsername()
        try {
            statsInitializer.initializeUserStats(username)

            val currentStats = userStatsDao.getUserStatsSync(username)
            val updatedStats = currentStats.copy(
                totalQuestionsAnswered = currentStats.totalQuestionsAnswered + count
            )
            guardar(updatedStats)

        } catch (e: Exception) {
            println("❌ Error en addQuestionsAnswered: ${e.message}")
            statsInitializer.initializeUserStats(username)
        }
    }

    override suspend fun addPracticeTime(seconds: Int) {
        val username = getCurrentUsername()
        try {
            statsInitializer.initializeUserStats(username)

            val currentStats = userStatsDao.getUserStatsSync(username)
            // El acumulado del dia arranca de cero si lo guardado es de otro dia.
            val delDia = if (currentStats.lastPracticeDate == getCurrentDate()) currentStats.dailyPracticeTime else 0
            val updatedStats = currentStats.copy(
                totalPracticeTimeSeconds = currentStats.totalPracticeTimeSeconds + seconds,
                dailyPracticeTime = delDia + seconds
            )
            guardar(updatedStats)

        } catch (e: Exception) {
            println("❌ Error en addPracticeTime: ${e.message}")
            statsInitializer.initializeUserStats(username)
        }
    }

    override suspend fun registerActivityToday() {
        val username = getCurrentUsername()
        try {
            statsInitializer.initializeUserStats(username)

            val currentStats = userStatsDao.getUserStatsSync(username)
            val today = getCurrentDate()

            if (currentStats.lastPracticeDate == today) {
                println("ℹ️ Actividad de hoy ya registrada, racha sin cambios")
                return
            }

            val updatedStats = if (shouldIncrementStreak(currentStats.lastPracticeDate, today)) {
                currentStats.copy(
                    currentStreakDays = currentStats.currentStreakDays + 1,
                    lastPracticeDate = today
                )
            } else {
                currentStats.copy(
                    currentStreakDays = 1,
                    lastPracticeDate = today
                )
            }

            guardar(updatedStats)
            println("✅ Racha actualizada: ${updatedStats.currentStreakDays} días")

        } catch (e: Exception) {
            println("❌ Error en registerActivityToday: ${e.message}")
            statsInitializer.initializeUserStats(username)
        }
    }

    override suspend fun incrementStreak() {
        val username = getCurrentUsername()
        try {
            // 🔥 INICIALIZAR STATS SI NO EXISTEN
            statsInitializer.initializeUserStats(username)

            val currentStats = userStatsDao.getUserStatsSync(username)
            val today = getCurrentDate()

            val updatedStats = if (shouldIncrementStreak(currentStats.lastPracticeDate, today)) {
                currentStats.copy(
                    currentStreakDays = currentStats.currentStreakDays + 1,
                    lastPracticeDate = today
                )
            } else {
                currentStats.copy(lastPracticeDate = today)
            }

            guardar(updatedStats)

        } catch (e: Exception) {
            println("❌ Error en incrementStreak: ${e.message}")
            statsInitializer.initializeUserStats(username)
        }
    }

    override suspend fun resetStreak() {
        val username = getCurrentUsername()
        try {
            // 🔥 INICIALIZAR STATS SI NO EXISTEN
            statsInitializer.initializeUserStats(username)

            val currentStats = userStatsDao.getUserStatsSync(username)
            val updatedStats = currentStats.copy(
                currentStreakDays = 0,
                lastPracticeDate = getCurrentDate()
            )
            guardar(updatedStats)

        } catch (e: Exception) {
            println("❌ Error en resetStreak: ${e.message}")
            statsInitializer.initializeUserStats(username)
        }
    }


    override suspend fun updateLevelProgress(competenceId: Int, levelId: Int, progress: Float) {
        val username = getCurrentUsername()
        try {
            statsInitializer.initializeUserStats(username)

            val currentStats = userStatsDao.getUserStatsSync(username)
            val updatedStats = currentStats.copy(
                totalQuestionsAnswered = currentStats.totalQuestionsAnswered + 1,
                totalPracticeTimeSeconds = currentStats.totalPracticeTimeSeconds + 60,
                dailyPracticeTime = currentStats.dailyPracticeTime + 60
            )
            guardar(updatedStats)
            println("✅ Progreso actualizado: competencia $competenceId, nivel $levelId (${(progress * 100).toInt()}%)")

        } catch (e: Exception) {
            println("❌ Error en updateLevelProgress: ${e.message}")
            statsInitializer.initializeUserStats(username)
        }
    }

    override suspend fun updateLastPracticeDate(date: String) {
        val username = getCurrentUsername()
        val currentStats = userStatsDao.getUserStatsSync(username)
        val updatedStats = currentStats.copy(lastPracticeDate = date)
        guardar(updatedStats)
    }

    /**
     * Sin uso desde que el tiempo del dia se corrige al leer (ver
     * [conTiempoDelDia]). Se conserva por si hace falta limpiar el valor
     * guardado a proposito, pero el camino normal ya no lo necesita.
     */
    override suspend fun resetDailyStats() {
        val username = getCurrentUsername()
        val currentStats = userStatsDao.getUserStatsSync(username)
        val updatedStats = currentStats.copy(dailyPracticeTime = 0)
        guardar(updatedStats)
    }

    override suspend fun getWeeklyProgress(): List<DailyProgress> {
        val username = getCurrentUsername()
        return userStatsDao.getWeeklyProgress(username)
    }

    override suspend fun getAchievementsProgress(): Map<String, Float> {
        val username = getCurrentUsername()
        val stats = userStatsDao.getUserStatsSync(username)

        return mapOf(
            "questions_100" to (stats.totalQuestionsAnswered / 100f).coerceAtMost(1f),
            "practice_5_hours" to (stats.totalPracticeTimeSeconds / (5 * 3600f)).coerceAtMost(1f),
            // Contra las marcas históricas: la racha y el tiempo del día se
            // reinician por diseño, y un logro conseguido no se devuelve.
            "streak_7_days" to (stats.maxStreakDays / 7f).coerceAtMost(1f),
            "daily_30_min" to (stats.maxDailyPracticeTime / 1800f).coerceAtMost(1f)
        )
    }

    // Métodos auxiliares privados
    private fun getCurrentUsername(): String {
        return try {
            val username = SessionManager.getCurrentUsername(context)
            if (username.isNullOrEmpty()) {
                println("⚠️ No hay usuario logueado, usando usuario por defecto")
                "usuario_invitado"
            } else {
                println("✅ Usuario obtenido de SessionManager: $username")
                username
            }
        } catch (e: Exception) {
            println("❌ Error obteniendo usuario de SessionManager: ${e.message}")
            "usuario_invitado" // Fallback seguro
        }
    }

    private fun getCurrentDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }

    private fun shouldIncrementStreak(lastPracticeDate: String, currentDate: String): Boolean {
        return if (lastPracticeDate.isEmpty()) {
            true // Primera vez que practica
        } else {
            val lastDate = LocalDate.parse(lastPracticeDate)
            val current = LocalDate.parse(currentDate)
            lastDate.plusDays(1) == current // Solo incrementa si practica días consecutivos
        }
    }
}