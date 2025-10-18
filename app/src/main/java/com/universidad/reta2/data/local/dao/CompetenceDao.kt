package com.universidad.reta2.data.local.dao
import androidx.room.Dao
import androidx.room.Query
import com.universidad.reta2.data.local.entities.CompetenceEntity


@Dao
interface CompetenceDao {
    @Query("SELECT * FROM competences")
    suspend fun getAllCompetences(): List<CompetenceEntity>

    @Query("SELECT * FROM competences WHERE id = :id")
    suspend fun getCompetence(id: String): CompetenceEntity?
}