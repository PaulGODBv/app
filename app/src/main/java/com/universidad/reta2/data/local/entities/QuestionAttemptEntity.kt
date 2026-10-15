package com.universidad.reta2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo


@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "question_id")
    val questionId: Int,

    @ColumnInfo(name = "level_id")
    val levelId: Int,

    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean,

    @ColumnInfo(name = "time_spent_seconds")
    val timeSpentSeconds: Int,

    @ColumnInfo(name = "attempted_at")
    val attemptedAt: Long = System.currentTimeMillis(),

    /**
     * Si el intento fue practicando en vez de evaluandose.
     *
     * Hasta la v20 la practica no dejaba ninguna fila aqui, y era a proposito:
     * sus aciertos entrarian en el porcentaje del nivel y en el desbloqueo, y
     * el estudiante veria subir su progreso sin haberse evaluado nunca.
     *
     * El efecto secundario era que la practica no dejaba **ningun rastro
     * fechado en ninguna parte**: solo movia contadores en `user_stats`. La
     * racha no se podia recalcular ni auditar, el grafico semanal no veia los
     * dias de practica, y terminar un nivel vacio regalaba un dia de racha sin
     * responder nada —medido en un telefono el 14/10/2026: trece segundos en un
     * Calentamiento sin preguntas subieron la racha de 2 a 3—.
     *
     * Con la columna, la practica escribe su fila y lo que calcula progreso
     * filtra por `es_practica = 0`. Lo que se gana no es solo el dato: un dia
     * sin responder nada no produce ninguna fila, asi que la racha fantasma
     * deja de ser representable en vez de depender de un `if` que alguien puede
     * quitar.
     */
    @ColumnInfo(name = "es_practica")
    val esPractica: Boolean = false
)

