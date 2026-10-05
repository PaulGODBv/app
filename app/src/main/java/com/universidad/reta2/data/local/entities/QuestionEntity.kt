package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = LevelEntity::class,
            parentColumns = ["id"],
            childColumns = ["level_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuestionEntity(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "level_id")
    val levelId: Int,

    @ColumnInfo(name = "text")
    val text: String,

    @ColumnInfo(name = "correct_option_id")
    val correctOptionId: Int, // ID de la opción correcta en question_options

    @ColumnInfo(name = "explanation")
    val explanation: String = "",

    @ColumnInfo(name = "competence_id")
    val competenceId: Int = 0,

    @ColumnInfo(name = "reading_text")
    val readingText: String = "",

    // Se guarda la URL, no la imagen: el binario lo cachea Coil en disco. Meter
    // los bytes en Room hincharia la base y duplicaria un trabajo que la
    // libreria ya hace mejor.
    @ColumnInfo(name = "context_image_url")
    val contextImageUrl: String? = null,

    @ColumnInfo(name = "context_image_alt")
    val contextImageAlt: String? = null,

    // Nombre de drawable del contenido de arranque, cuando la pregunta no
    // viene del panel.
    @ColumnInfo(name = "context_image")
    val contextImage: String? = null,

    // Marca de cambio que envia el panel. Es el cursor de la sincronizacion.
    @ColumnInfo(name = "remote_updated_at")
    val remoteUpdatedAt: String? = null,

    // El panel retira preguntas desactivandolas. Se guardan igual para no
    // romper los intentos ya registrados, pero no se juegan.
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
