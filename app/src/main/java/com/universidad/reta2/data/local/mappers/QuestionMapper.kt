package com.universidad.reta2.data.local.mappers

import com.universidad.reta2.data.local.entities.QuestionEntity
import com.universidad.reta2.data.local.entities.QuestionOptionEntity
import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.models.QuestionOption
import kotlin.math.exp

object QuestionMapper {
    fun toDomain(
        entity: QuestionEntity,
        options: List<QuestionOptionEntity>
    ): Question{
        return Question(
            id = entity.id,
            text = entity.text,
            options=options.map{optionEntity ->
                QuestionOption(
                    id=optionEntity.id,
                    text=optionEntity.optionText,
                    originalOrder=optionEntity.originalOrder
                )
            },
            correctOptionId = entity.correctOptionId,
            explanation = entity.explanation
        )
    }

    fun toEntity(domain: Question, levelId: Int): Pair<QuestionEntity, List<QuestionOptionEntity>>{
        val questionEntity=QuestionEntity(
            id=domain.id,
            levelId=levelId,
            text=domain.text,
            correctOptionId = domain.correctOptionId,
            explanation = domain.explanation
        )

        val optionEntities=domain.options.map{
            option ->
            QuestionOptionEntity(
                id=option.id,
                questionId = domain.id,
                optionText = option.text,
                originalOrder=option.originalOrder
            )
        }

        return Pair(questionEntity,optionEntities)
    }
}