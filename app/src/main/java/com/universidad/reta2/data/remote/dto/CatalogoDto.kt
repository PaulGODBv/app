package com.universidad.reta2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * El catalogo de competencias y niveles tal y como lo publica el panel.
 *
 * Es lo que devuelve `GET /api/competences/`, y existe para que **dar de alta
 * un nivel deje de exigir publicar una version de la app**. Hasta ahora el
 * catalogo vivia escrito en `CompetencyData` y la lista de niveles a
 * sincronizar era una constante: un nivel nuevo en el panel no llegaba a
 * ningun telefono hasta la siguiente publicacion, con su migracion de Room.
 *
 * La app sigue jugando contra Room, no contra esto. El catalogo solo sirve
 * para **descubrir** que niveles hay y crear en Room los que falten.
 */
data class CompetenceDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("order") val order: Int = 0,
    @SerializedName("levels") val levels: List<LevelSummaryDto> = emptyList()
)

data class LevelSummaryDto(
    /** El id del panel. Es la identidad estable del nivel. */
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String = "",
    /**
     * Posicion dentro de su competencia. **Cero significa calentamiento.**
     *
     * No es la identidad del nivel: reordenar en el panel cambia esto y no
     * debe cambiar a que nivel apuntan los intentos ya registrados. Solo se
     * usa para proponer un id local cuando el nivel es nuevo.
     */
    @SerializedName("order") val order: Int = 0,
    @SerializedName("is_Locked_by_default") val bloqueadoPorDefecto: Boolean = false,
    @SerializedName("formato_practica") val formatoPractica: String = "opcion",
    @SerializedName("question_count") val questionCount: Int = 0
)
