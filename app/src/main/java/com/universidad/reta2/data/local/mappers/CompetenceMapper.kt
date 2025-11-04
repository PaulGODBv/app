package com.universidad.reta2.data.local.mappers

import com.universidad.reta2.data.local.entities.CompetenceEntity
import com.universidad.reta2.data.local.entities.LevelEntity
import com.universidad.reta2.domain.models.Competence
import com.universidad.reta2.domain.models.Level
import com.universidad.reta2.R

object CompetenceMapper {

    /** Convierte una entidad a un modelo de dominio **/
    fun toDomain(entity: CompetenceEntity, levels: List<Level> = emptyList()): Competence {
        val iconRes = when (entity.name.lowercase()) {
            "lectura crítica" -> R.drawable.ic_lectura_critica
            "razonamiento cuantitativo" -> R.drawable.ic_razonamiento_critico
            "inglés" -> R.drawable.ic_ingles
            "competencias ciudadanas" -> R.drawable.ic_competencia_ciudadana
            else -> R.drawable.ic_launcher_foreground // ícono genérico
        }

        return Competence(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            iconResId = iconRes,
            levels = levels,
            totalProgress = entity.totalProgress
        )
    }


    /** Convierte un modelo de dominio a una entidad Room **/
    fun toEntity(domain: Competence): CompetenceEntity {
        return CompetenceEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            iconResId = domain.iconResId,
            totalProgress = domain.totalProgress
        )
    }

    /** Convierte los niveles asociados a una competencia en entidades con su foreign key correcta **/
    fun toLevelEntities(domain: Competence): List<LevelEntity> {
        return domain.levels.map { level ->
            LevelEntity(
                id = level.id,
                competenceId = domain.id,
                name = level.name,
                description = level.description,
                isLocked = level.isLocked,
                isCompleted = level.isCompleted,
                progress = level.progress
            )
        }
    }

    /** Permite crear una competencia desde datos externos **/
    fun fromNetworkData(
        id: Int,
        name: String,
        description: String,
        icon: Int
    ): CompetenceEntity {
        return CompetenceEntity(
            id = id,
            name = name,
            description = description,
            iconResId = icon,
            totalProgress = 0f
        )
    }
}
