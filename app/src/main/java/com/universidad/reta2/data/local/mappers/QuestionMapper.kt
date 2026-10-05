package com.universidad.reta2.data.local.mappers

import com.universidad.reta2.data.local.entities.QuestionEntity
import com.universidad.reta2.data.local.entities.QuestionOptionEntity
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.models.QuestionOption

object QuestionMapper {

    fun toDomain(
        entity: QuestionEntity,
        options: List<QuestionOptionEntity>
    ): Question = Question(
        id = entity.id,
        text = entity.text,
        // El id de la opcion es su POSICION (1..4), no la clave de la fila.
        // Toda la app comprueba el acierto con `option.id == correctOptionId`,
        // y el panel manda la correcta como posicion. Devolver aqui el id
        // autogenerado de Room haria que ninguna respuesta se diera por buena.
        // El banco local ya sigue esta convencion: en las 94 preguntas de
        // CompetencyData se cumple `id == originalOrder + 1`.
        options = options.sortedBy { it.originalOrder }.map { opcion ->
            QuestionOption(
                id = opcion.originalOrder + 1,
                text = opcion.optionText,
                originalOrder = opcion.originalOrder
            )
        },
        correctOptionId = entity.correctOptionId,
        explanation = entity.explanation,
        readingText = entity.readingText,
        contextImage = entity.contextImage,
        contextImageUrl = entity.contextImageUrl,
        contextImageAlt = entity.contextImageAlt,
        competenceId = entity.competenceId,
        levelId = entity.levelId
    )

    fun toEntity(
        domain: Question,
        levelId: Int,
        remoteUpdatedAt: String? = null,
        isActive: Boolean = true
    ): Pair<QuestionEntity, List<QuestionOptionEntity>> {
        val pregunta = QuestionEntity(
            id = domain.id,
            levelId = levelId,
            competenceId = domain.competenceId,
            text = domain.text,
            correctOptionId = domain.correctOptionId,
            explanation = domain.explanation,
            readingText = domain.readingText,
            contextImageUrl = domain.contextImageUrl,
            contextImageAlt = domain.contextImageAlt,
            contextImage = domain.contextImage,
            remoteUpdatedAt = remoteUpdatedAt,
            isActive = isActive
        )

        // Las opciones se reescriben enteras en cada sincronizacion, asi que no
        // se conserva su id local: se deja que Room lo genere.
        val opciones = domain.options.map { opcion ->
            QuestionOptionEntity(
                questionId = domain.id,
                optionText = opcion.text,
                originalOrder = opcion.originalOrder
            )
        }

        return pregunta to opciones
    }
}
