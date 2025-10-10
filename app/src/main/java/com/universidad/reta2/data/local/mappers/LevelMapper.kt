package com.universidad.reta2.data.local.mappers

import com.universidad.reta2.data.local.entities.LevelEntity
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.models.Question

object LevelMapper{
    fun toDomain(entity: LevelEntity, questions: List<Question> = emptyList()): Level {
        return Level(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            questions = questions,
            isLocked = entity.isLocked,
            isCompleted = entity.isCompleted,
            progress = entity.progress
        )
    }

    fun toEntity(domain: Level): LevelEntity{
        return LevelEntity(
            id=domain.id,
            competenceId="",
            name=domain.name,
            description=domain.description,
            isLocked= domain.isLocked,
            isCompleted=domain.isCompleted,
            progress=domain.progress
        )
    }
}