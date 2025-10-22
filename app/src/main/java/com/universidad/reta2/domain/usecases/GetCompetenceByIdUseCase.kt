package com.universidad.reta2.domain.usecases

import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.repositories.CompetenceRepository
import javax.inject.Inject

class GetCompetenceByIdUseCase @Inject constructor(
    private val competenceRepository: CompetenceRepository
) {
    suspend operator fun invoke(id: String): Competence? {
        return competenceRepository.getCompetenceById(id)
    }
}