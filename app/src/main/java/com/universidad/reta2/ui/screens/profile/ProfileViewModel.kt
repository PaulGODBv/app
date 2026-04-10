package com.universidad.reta2.ui.screens.profile

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.universidad.reta2.data.preferences.SessionManager
import com.universidad.reta2.domain.repositories.UserRepository
import com.universidad.reta2.domain.repositories.UserStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val userStatsRepository: UserStatsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventChannel= MutableSharedFlow<ProfileEvent>()
    val eventChannel = _eventChannel.asSharedFlow()

    sealed class ProfileEvent{
        data class LaunchIntent(val intent: Intent): ProfileEvent()
    }


    init {
        val username = sessionManager.getCurrentUsername(context) ?: ""
        val email = sessionManager.getCurrentEmail(context) ?: ""
        _uiState.value = _uiState.value.copy(username = username, email = email)
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onCurrentPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(currentPassword = value)
    }

    fun onNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value)
    }

    fun updateProfile() {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.username.isEmpty()) {
                showError("El nombre de usuario no puede estar vacío")
                return@launch
            }
            if (state.email.isEmpty()) {
                showError("El correo electrónico no puede estar vacío")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, errorMessage = "", successMessage = "")

            val currentUsername = sessionManager.getCurrentUsername(context) ?: ""
            val currentEmail = sessionManager.getCurrentEmail(context) ?: ""

            val success = userRepository.updateUser(
                currentUsername,
                currentEmail,
                state.username,
                state.email,
                if (state.newPassword.isNotEmpty()) state.newPassword else null
            )

            if (success) {
                sessionManager.updateUserData(context, state.username, state.email)
                showSuccess("Perfil actualizado exitosamente")
                _uiState.value = _uiState.value.copy(
                    currentPassword = "",
                    newPassword = "",
                    confirmPassword = ""
                )
            } else {
                showError("Error al actualizar perfil. Verifique que el usuario/email no esté en uso")
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun logout() {
        sessionManager.logout(context)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = "", successMessage = "")
    }

    private companion object {
        const val ADMIN_EMAIL = "appreta2@gmail.com"
    }

    fun exportStatisticsToAdmin() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "", successMessage = "")

            try {
                // 1. Generar CSV con estadísticas
                val csvContent = generateStatisticsCSV()

                // 2. Enviar por correo
                sendEmailWithCSV(csvContent)

                showSuccess("Estadísticas enviadas a administración exitosamente")

            } catch (e: Exception) {
                showError("Error al exportar estadísticas: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }


    private suspend fun generateStatisticsCSV(): String {
        val username = sessionManager.getCurrentUsername(context) ?: "Usuario"
        val userEmail = sessionManager.getCurrentEmail(context) ?: "No especificado"

        // Collect stats using Flow
        val stats = userStatsRepository.getUserStats().first()
        val weeklyProgress = userStatsRepository.getWeeklyProgress()
        val achievements = userStatsRepository.getAchievementsProgress()

        val csvBuilder = StringBuilder()

        // Headers
        csvBuilder.append("Usuario,Email,Métricas,Valor,Fecha Reporte\n")

        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        // General stats
        csvBuilder.append("$username,$userEmail,Preguntas Respondidas,${stats.totalQuestionsAnswered},$currentDate\n")
        csvBuilder.append("$username,$userEmail,Tiempo Total (min),${stats.totalPracticeTimeSeconds / 60},$currentDate\n")
        csvBuilder.append("$username,$userEmail,Racha Actual,${stats.currentStreakDays},$currentDate\n")
        csvBuilder.append("$username,$userEmail,Tiempo Hoy (min),${stats.dailyPracticeTime / 60},$currentDate\n")

        // Weekly progress
        weeklyProgress.forEach { daily ->
            csvBuilder.append("$username,$userEmail,Preguntas ${daily.date},${daily.questionsAnswered},$currentDate\n")
            csvBuilder.append("$username,$userEmail,Tiempo ${daily.date} (min),${daily.practiceTime / 60},$currentDate\n")
        }

        // Achievements progress
        achievements.forEach { (achievement, progress) ->
            csvBuilder.append("$username,$userEmail,Logro $achievement,${(progress * 100).toInt()}%,$currentDate\n")
        }

        return csvBuilder.toString()
    }

    private fun sendEmailWithCSV(csvContent: String) {
        val username = sessionManager.getCurrentUsername(context) ?: "Usuario"
        val userEmail = sessionManager.getCurrentEmail(context) ?: "No especificado"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ADMIN_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Progreso - $username")
            putExtra(Intent.EXTRA_TEXT,
                """
                Reporte de progreso generado automáticamente desde la app Reta2.

                Usuario: $username
                Email: $userEmail
                Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}

                El archivo CSV adjunto contiene las estadísticas detalladas del usuario.

                ¡Saludos!
                """.trimIndent()
            )

            // Crear archivo temporal CSV
            val tempFile = createTempCSVFile(csvContent, username)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser=Intent.createChooser(intent, "Enviar reporte a administración")
        viewModelScope.launch {
            _eventChannel.emit(ProfileEvent.LaunchIntent(chooser))
        }

    }

    private fun createTempCSVFile(csvContent: String, username: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val safeUsername = username.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val fileName = "reporte_${safeUsername}_$timeStamp.csv"

        val file = File(context.getExternalFilesDir(null), fileName)
        file.writeText(csvContent, Charsets.UTF_8)

        return file
    }

    private fun showError(msg: String) {
        _uiState.value = _uiState.value.copy(errorMessage = msg)
    }

    private fun showSuccess(msg: String) {
        _uiState.value = _uiState.value.copy(successMessage = msg)
    }
}

data class ProfileUiState(
    val username: String = "",
    val email: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

