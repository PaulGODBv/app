package com.universidad.reta2.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.universidad.reta2.data.local.entities.QuestionEntity
import com.universidad.reta2.data.local.entities.QuestionOptionEntity

@Dao
interface QuestionDao {

    // ---- Lectura -----------------------------------------------------------

    /** Solo las activas: el panel retira preguntas desactivandolas. */
    @Query("SELECT * FROM questions WHERE level_id = :levelId AND is_active = 1")
    suspend fun getQuestionsByLevel(levelId: Int): List<QuestionEntity>

    @Query("SELECT * FROM question_options WHERE question_id = :questionId ORDER BY original_order")
    suspend fun getOptionsForQuestion(questionId: Int): List<QuestionOptionEntity>

    @Query("SELECT * FROM question_options WHERE question_id IN (:questionIds) ORDER BY original_order")
    suspend fun getOptionsForQuestions(questionIds: List<Int>): List<QuestionOptionEntity>

    @Query("SELECT COUNT(id) FROM questions WHERE level_id IN (:levelIds) AND is_active = 1")
    suspend fun getQuestionCountForLevels(levelIds: List<Int>): Int

    @Query("SELECT COUNT(id) FROM questions")
    suspend fun contarTodas(): Int

    /** Las URLs de imagen de un nivel, para precargarlas con Coil. */
    @Query("""
        SELECT context_image_url FROM questions
        WHERE level_id = :levelId AND is_active = 1 AND context_image_url IS NOT NULL
    """)
    suspend fun urlsDeImagen(levelId: Int): List<String>

    // ---- Escritura ---------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPreguntas(preguntas: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOpciones(opciones: List<QuestionOptionEntity>)

    @Query("DELETE FROM question_options WHERE question_id IN (:questionIds)")
    suspend fun borrarOpcionesDe(questionIds: List<Int>)

    @Query("UPDATE questions SET is_active = 0 WHERE id IN (:questionIds)")
    suspend fun desactivar(questionIds: List<Int>)

    /**
     * Deja el nivel igual a lo que envio el panel, en una sola transaccion.
     *
     * Las opciones se borran y se reescriben porque el panel no manda ids
     * estables para ellas; las preguntas si conservan su id, que es lo que
     * enlaza con los intentos ya registrados del estudiante.
     */
    @Transaction
    suspend fun reemplazarNivel(
        preguntas: List<QuestionEntity>,
        opciones: List<QuestionOptionEntity>,
        retiradas: List<Int>
    ) {
        if (preguntas.isNotEmpty()) {
            borrarOpcionesDe(preguntas.map { it.id })
            insertarPreguntas(preguntas)
            insertarOpciones(opciones)
        }
        if (retiradas.isNotEmpty()) {
            desactivar(retiradas)
        }
    }
}
