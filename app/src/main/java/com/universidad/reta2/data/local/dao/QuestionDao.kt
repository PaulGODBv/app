package com.universidad.reta2.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.universidad.reta2.data.local.entities.QuestionEntity
import com.universidad.reta2.data.local.entities.QuestionOptionEntity

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE level_id = :levelId")
    suspend fun getQuestionsByLevel(levelId: Int): List<QuestionEntity>

    @Query("SELECT * FROM question_options WHERE question_id = :questionId ORDER BY original_order")
    suspend fun getOptionsForQuestion(questionId: Int): List<QuestionOptionEntity>

    @Query("SELECT COUNT(id) FROM questions WHERE level_id IN (:levelIds)")
    suspend fun getQuestionCountForLevels(levelIds: List<Int>): Int
}
