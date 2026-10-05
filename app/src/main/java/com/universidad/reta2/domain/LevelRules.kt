package com.universidad.reta2.domain

/**
 * Reglas de aprobación de un nivel.
 *
 * El umbral vivía duplicado: `PASSING_PERCENTAGE` en la pantalla de resultados
 * y `0.8f` dentro de `ProgressRepositoriesImp`. Cuando los dos valores se
 * separaron, la pantalla ofrecía avanzar a un nivel que el repositorio dejaba
 * bloqueado. Aquí hay un único valor para las dos capas.
 *
 * El 70 % es el que fija el anteproyecto: «desbloqueo de contenidos
 * condicionado al rendimiento mínimo del 70 % de aciertos por nivel».
 */
object LevelRules {

    /** Porcentaje mínimo de aciertos para dar un nivel por superado. */
    const val PASSING_PERCENTAGE = 70

    /** El mismo umbral como fracción, para comparar contra un progreso 0f..1f. */
    const val PASSING_RATIO = PASSING_PERCENTAGE / 100f

    /**
     * Si un nivel es de práctica (calentamiento) y no de evaluación.
     *
     * Los ids los genera `competencia * 100 + numero`, así que el nivel de
     * práctica de cada competencia —el que va delante del básico, con número
     * cero— **termina en 00**: 100, 200, 300 y 400. Los de evaluación van del
     * 01 en adelante.
     *
     * La regla vive aquí y no repartida por la app porque de ella cuelgan tres
     * cosas que tienen que decir lo mismo: que la práctica no desbloquea nada,
     * que no entra en el porcentaje de la competencia, y que su
     * retroalimentación es inmediata en vez de al final.
     */
    fun esDePractica(levelId: Int): Boolean = levelId % 100 == 0
}
