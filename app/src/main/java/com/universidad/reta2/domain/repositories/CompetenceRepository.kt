package com.universidad.reta2.domain.repositories

import com.universidad.reta2.domain.models.Competence

interface CompetenceRepository {

    suspend fun getAllCompetences(): List<Competence>
    suspend fun getCompetenceById(id: Int): Competence?
    suspend fun getCompetencesByCategory(category: String): List<Competence>
    suspend fun getFeaturedCompetences(): List<Competence>
    suspend fun searchCompetences(query: String): List<Competence>
    suspend fun getOverallProgress(): Float
    suspend fun updateCompetence(competence: Competence): Boolean

    /**
     * Anota que se acaba de abrir una sesion en este nivel, en cualquier modo.
     *
     * Vive en el repositorio y no se llama al DAO desde el ViewModel porque
     * ademas de escribir hay que **invalidar la cache del catalogo**: si no,
     * Inicio sigue sirviendo la lista anterior y la actividad no aparece
     * aunque este guardada.
     */
    suspend fun marcarNivelPracticado(levelId: Int)
}
