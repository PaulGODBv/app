package com.universidad.reta2.data.local

import com.universidad.reta2.domain.models.UserStats
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Últimas estadísticas conocidas del usuario, guardadas en memoria.
 *
 * Hermana de [CatalogoCache] y por el mismo motivo. `UserStats()` vale cero en
 * todos sus campos, así que mientras Room contesta la primera vez la tarjeta de
 * Inicio enseñaba «0 preguntas, 0 de racha» y unos cientos de milisegundos
 * después saltaba a los valores buenos. Sirviendo antes lo último que se supo,
 * la tarjeta nace con las cifras correctas y Room solo las confirma.
 *
 * Vive mientras vive el proceso, así que en el primer arranque no hay nada que
 * servir y se espera a Room como siempre.
 */
@Singleton
class EstadisticasCache @Inject constructor() {

    private data class Entrada(val usuario: String, val stats: UserStats)

    private val entrada = AtomicReference<Entrada?>(null)

    fun obtener(usuario: String): UserStats? =
        entrada.get()?.takeIf { it.usuario == usuario }?.stats

    fun guardar(usuario: String, stats: UserStats) {
        entrada.set(Entrada(usuario, stats))
    }
}
