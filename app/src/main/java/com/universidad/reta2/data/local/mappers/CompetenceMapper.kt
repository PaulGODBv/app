package com.universidad.reta2.data.local.mappers

import com.universidad.reta2.data.local.entities.CompetenceEntity
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level

object CompetenceMapper {

    fun toDomain(entity: CompetenceEntity, levels: List<Level> = emptyList()): Competence {
        return Competence(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            icon = entity.icon,
            levels = levels,
            totalProgress = entity.totalProgress
        )
    }

    fun toEntity(domain: Competence): CompetenceEntity {
        return CompetenceEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            icon = domain.icon,
            totalProgress = domain.totalProgress
        )
    }

    // Para crear entidades desde datos externos (API, JSON, etc.)
    fun fromNetworkData(
        id: String,
        name: String,
        description: String,
        icon: String,
        category: String = "",
        isFeatured: Boolean = false
    ): CompetenceEntity {
        return CompetenceEntity(
            id = id,
            name = name,
            description = description,
            icon = icon,
            totalProgress = 0f
        )
    }
}