package com.universidad.reta2.data.local.dao

import androidx.room.*
import com.universidad.reta2.data.local.entities.CompetenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetenceDao {

    @Query("SELECT * FROM competences ORDER BY name ASC")
    suspend fun getAllCompetences(): List<CompetenceEntity>

    @Query("SELECT * FROM competences WHERE id = :id")
    suspend fun getCompetenceById(id: String): CompetenceEntity?

    @Query("SELECT * FROM competences WHERE name LIKE :query OR description LIKE :query ORDER BY name ASC")
    suspend fun searchCompetences(query: String): List<CompetenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetence(competence: CompetenceEntity)

    @Update
    suspend fun updateCompetence(competence: CompetenceEntity): Int

    @Query("DELETE FROM competences WHERE id = :id")
    suspend fun deleteCompetence(id: String)

    // Para observación reactiva
    @Query("SELECT * FROM competences ORDER BY name ASC")
    fun getAllCompetencesFlow(): Flow<List<CompetenceEntity>>

    @Query("SELECT * FROM competences WHERE id = :id")
    fun getCompetenceByIdFlow(id: String): Flow<CompetenceEntity?>
}