package com.universidad.reta2.domain.models

data class Level(
    val id: Int,
    val name: String,
    val description: String,
    val questions: List<Question>,
    val isLocked: Boolean=false,
    val isCompleted: Boolean=false,
    val progress: Float=0f,

    /**
     * Como se juega el nivel en modo practica.
     *
     * Lo decide el panel. "opcion" es elegir una respuesta, como en evaluacion;
     * "unir" es emparejar enunciados con respuestas; "arrastrar" es llevar la
     * palabra a su hueco. En evaluacion se ignora: ahi siempre se elige.
     */
    val formatoPractica: String = FORMATO_OPCION,

    /** Cuando se practico por ultima vez, en milisegundos. 0 = nunca. */
    val ultimaPractica: Long = 0L
) {
    companion object {
        const val FORMATO_OPCION = "opcion"
        const val FORMATO_UNIR = "unir"
        const val FORMATO_ARRASTRAR = "arrastrar"
    }
}