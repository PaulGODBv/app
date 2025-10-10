package com.universidad.reta2.data.local.dao

import androidx.room.*
import com.universidad.reta2.data.local.entities.LevelProgressEntity
import com.universidad.reta2.data.local.entities.QuestionAttemptEntity
import com.universidad.reta2.domain.models.LevelStats
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    // Registrar intento de pregunta individual
    @Insert
    suspend fun insertQuestionAttempt(attempt: QuestionAttemptEntity)

    // Obtener progreso de un nivel específico
    @Query("SELECT * FROM level_progress WHERE username = :username AND competence_id = :competenceId AND level_id = :levelId")
    suspend fun getLevelProgress(username: String, competenceId: Int, levelId: Int): LevelProgressEntity?

    // Guardar o actualizar progreso de nivel
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelProgress(progress: LevelProgressEntity)

    // Obtener todo el progreso del usuario
    @Query("SELECT * FROM level_progress WHERE username = :username ORDER BY last_updated DESC")
    fun getUserProgress(username: String): Flow<List<LevelProgressEntity>>

    // Obtener estadísticas de un nivel
    @Query("""
    SELECT 
        level_id as levelId,
        COUNT(*) as totalAttempts,
        SUM(CASE WHEN is_correct THEN 1 ELSE 0 END) as correctAttempts,
        AVG(time_spent_seconds) as averageTime
    FROM question_attempts 
    WHERE username = :username AND level_id = :levelId
    GROUP BY level_id
""")
    suspend fun getLevelStats(username: String, levelId: Int): LevelStats

    // Eliminar progreso de un nivel
    @Query("DELETE FROM level_progress WHERE username = :username AND competence_id = :competenceId AND level_id = :levelId")
    suspend fun deleteLevelProgress(username: String, competenceId: Int, levelId: Int)

    // Obtener preguntas incorrectas de un nivel para repaso
    @Query("""
        SELECT question_id 
        FROM question_attempts 
        WHERE username = :username AND level_id = :levelId AND is_correct = 0
        GROUP BY question_id
    """)
    suspend fun getIncorrectQuestions(username: String, levelId: Int): List<Int>
}