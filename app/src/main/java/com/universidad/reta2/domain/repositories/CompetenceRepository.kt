package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.Competence

interface CompetenceRepository {

    suspend fun getAllCompetences(): List<Competence>
    suspend fun getCompetenceById(id: String): Competence?
    suspend fun getCompetencesByCategory(category: String): List<Competence>
    suspend fun getFeaturedCompetences(): List<Competence>
    suspend fun searchCompetences(query: String): List<Competence>
    suspend fun getOverallProgress(): Float
    suspend fun updateCompetence(competence: Competence): Boolean
}