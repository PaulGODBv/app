package com.universidad.reta2.data.local.mappers

import com.universidad.reta2.data.local.entities.LevelEntity
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.domain.models.Question

object LevelMapper {

    /** Convierte una entidad de la base de datos en un modelo de dominio **/
    fun toDomain(
        entity: LevelEntity,
        questions: List<Question> = emptyList()
    ): Level {
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

    /** Convierte un modelo de dominio a una entidad Room, vinculando correctamente la competencia **/
    fun toEntity(
        domain: Level,
        competenceId: Int // 🔗 ID de la competencia relacionada
    ): LevelEntity {
        return LevelEntity(
            id = domain.id,
            competenceId = competenceId, // 🔗 FK bien asignada
            name = domain.name,
            description = domain.description,
            isLocked = domain.isLocked,
            isCompleted = domain.isCompleted,
            progress = domain.progress
        )
    }

    /** Convierte una lista de niveles de dominio en entidades asociadas a una competencia **/
    fun toEntityList(
        levels: List<Level>,
        competenceId: Int
    ): List<LevelEntity> {
        return levels.map { toEntity(it, competenceId) }
    }
}
