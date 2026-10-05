package com.universidad.reta2.domain.models

data class Question(
    val id: Int,
    val text: String,
    val options: List<QuestionOption>,
    val correctOptionId: Int,
    val explanation: String = "",
    val readingText: String = "",

    /**
     * Nombre de un drawable del APK. Es como funcionaba el banco hardcodeado y
     * se conserva para el contenido de arranque; el que viene del panel usa
     * [contextImageUrl].
     */
    val contextImage: String? = null,

    /** URL absoluta de la imagen de contexto servida por el panel. */
    val contextImageUrl: String? = null,

    /** Que se ve en la imagen. Lo necesita el lector de pantalla. */
    val contextImageAlt: String? = null,

    /**
     * Competencia y nivel a los que pertenece.
     *
     * Vienen en la propia pregunta para que nadie tenga que deducirlos
     * consultando el catalogo: `TimedModeViewModel` lo hacia contra
     * `CompetencyData`, y esa era la ultima atadura al banco hardcodeado.
     */
    val competenceId: Int = 0,
    val levelId: Int = 0
)

data class QuestionOption(
    val id: Int,
    val text: String,
    val originalOrder: Int
)
