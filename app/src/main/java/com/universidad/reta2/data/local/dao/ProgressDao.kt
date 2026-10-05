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

    // Obtener tdo el progreso del usuario
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
    suspend fun getLevelStats(username: String, levelId: Int): LevelStats?

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

    @Query("""
        SELECT DISTINCT question_id 
        FROM question_attempts 
        WHERE username = :username 
        AND is_correct = 1 
        AND level_id = :levelId
    """)
    suspend fun getCorrectlyAnsweredQuestionIds(
        username: String,
        levelId: Int
    ): List<Int>

    @Query("""
        SELECT COUNT(DISTINCT question_id) 
        FROM question_attempts 
        WHERE username = :username 
        AND is_correct = 1 
        AND level_id IN (:levelIds)
    """)
    suspend fun getUniqueCorrectQuestionCountForLevels(username: String, levelIds: List<Int>): Int

    /**
     * Aciertos que cuentan para el progreso, acotados al banco actual.
     *
     * La version de arriba cuenta cualquier intento acertado, exista aun la
     * pregunta o no. Cuando el banco paso a venir del panel los ids cambiaron
     * y los intentos viejos quedaron apuntando a preguntas que ya no estan:
     * el numerador seguia sumandolos y el porcentaje se iba por encima del
     * 100 % — un nivel de 8 preguntas llego a marcar 137 %.
     *
     * Pasando los ids del banco, numerador y denominador miden lo mismo.
     */
    @Query("""
        SELECT COUNT(DISTINCT question_id)
        FROM question_attempts
        WHERE username = :username
        AND is_correct = 1
        AND level_id IN (:levelIds)
        AND question_id IN (:questionIds)
    """)
    suspend fun contarAciertosDeEstasPreguntas(
        username: String,
        levelIds: List<Int>,
        questionIds: List<Int>
    ): Int
}
