package com.universidad.reta2.data.local.dao

import com.universidad.reta2.data.local.entities.LevelEntity
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface LevelDao {

    @Query("SELECT * FROM levels WHERE competence_id = :competenceId ORDER BY id")
    suspend fun getLevelsByCompetence(competenceId: Int): List<LevelEntity>

    @Query("SELECT * FROM levels WHERE id = :levelId AND competence_id = :competenceId")
    suspend fun getLevel(competenceId: Int, levelId: Int): LevelEntity?

    @Update
    suspend fun updateLevel(level: LevelEntity)

    @Query("UPDATE levels SET is_locked = :isLocked WHERE id = :levelId AND competence_id = :competenceId")
    suspend fun updateLevelLockStatus(competenceId: Int, levelId: Int, isLocked: Boolean)

    @Query("UPDATE levels SET is_completed = :isCompleted, progress = :progress WHERE id = :levelId AND competence_id = :competenceId")
    suspend fun updateLevelProgress(competenceId: Int, levelId: Int, isCompleted: Boolean, progress: Float)
}