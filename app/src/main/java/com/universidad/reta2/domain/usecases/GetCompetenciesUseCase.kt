package com.universidad.reta2.domain.usecases
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import javax.inject.Inject

class GetCompetencesUseCase @Inject constructor(
    private val competenceRepository: CompetenceRepository
) {
    suspend operator fun invoke(): List<Competence> {
        return competenceRepository.getAllCompetences()
    }
}