package com.universidad.reta2.domain.models

data class  UserStats(
    val totalQuestionsAnswered: Int = 0,
    val totalPracticeTimeSeconds: Int = 0, // Tiempo total en segundos
    val currentStreakDays: Int = 0,
    val lastPracticeDate: String = "", // Fecha del último día de práctica
    val dailyPracticeTime: Int = 0, // Tiempo de práctica del día actual en segundos

    /** Mejor racha alcanzada. Solo sube; es contra esta que se miden los logros. */
    val maxStreakDays: Int = 0,

    /** Mejor tiempo de práctica en un solo día, en segundos. Solo sube. */
    val maxDailyPracticeTime: Int = 0
) {

    /**
     * Cierto cuando hay una racha viva que se pierde si hoy no se practica.
     *
     * No es lo mismo que `currentStreakDays == 0`, que era la condición que
     * usaba el aviso de Inicio: cero significa que **ya** no hay racha —o que
     * nunca la hubo—, así que avisar entonces de que «está en riesgo» llega
     * tarde y, en un usuario recién registrado, es sencillamente falso.
     *
     * [hoy] se recibe en formato ISO para no meter el reloj dentro del modelo.
     */
    fun rachaEnRiesgo(hoy: String): Boolean =
        currentStreakDays > 0 && lastPracticeDate != hoy
}