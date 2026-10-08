package com.universidad.reta2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Lo que devuelve `GET /api/questions/<level_id>/`.
 *
 * Es el contenido tal y como lo publica el panel. La app no juega contra esto:
 * lo vuelca en Room y juega siempre contra Room, que es lo que mantiene la
 * practica disponible sin conexion.
 */
data class LevelContentResponse(
    @SerializedName("level_id") val levelId: Int,
    @SerializedName("level_name") val levelName: String = "",
    /**
     * Como se juega el nivel en modo practica: "opcion", "unir" o "arrastrar".
     *
     * Lo decide el panel por nivel. Con valor por defecto para que una version
     * vieja del panel —que no manda el campo— siga funcionando: sin el, todo
     * se juega eligiendo una opcion, que es lo que habia antes.
     */
    @SerializedName("formato_practica") val formatoPractica: String = "opcion",
    @SerializedName("question_count") val questionCount: Int = 0,
    @SerializedName("questions") val questions: List<RemoteQuestionDto> = emptyList()
)

data class RemoteQuestionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("level_id") val levelId: Int = 0,
    @SerializedName("competence_id") val competenceId: Int = 0,
    @SerializedName("text") val text: String,
    @SerializedName("options") val options: List<RemoteOptionDto> = emptyList(),
    // Posicion (1..4) de la opcion correcta, no su id: el panel no garantiza
    // ids estables para las opciones, solo para las preguntas.
    @SerializedName("correct_option_order") val correctOptionOrder: Int = 1,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("reading_text") val readingText: String? = null,
    @SerializedName("context_image_url") val contextImageUrl: String? = null,
    @SerializedName("context_image_alt") val contextImageAlt: String? = null,
    @SerializedName("context_image") val contextImage: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class RemoteOptionDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("text") val text: String,
    @SerializedName("order") val order: Int = 0
)
