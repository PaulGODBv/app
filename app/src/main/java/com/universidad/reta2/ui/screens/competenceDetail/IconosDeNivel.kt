package com.universidad.reta2.ui.screens.competenceDetail

import com.universidad.reta2.R

/**
 * El icono propio de cada nivel, o `null` si todavia no tiene.
 *
 * **Se mapea por el id local del nivel, no por su posicion.** La tentacion es
 * contar: «el tercer nivel de esta competencia lleva el tercer icono». Pero
 * entonces, el dia que el panel publique un nivel nuevo en medio, todos los
 * siguientes enseñarian el dibujo del vecino y nadie se enteraria: el fallo
 * seria silencioso y bonito. Mapeando por id, un nivel que no este aqui se
 * queda con la estrella de siempre — que se ve raro, se nota, y se arregla.
 *
 * Ojo: estos ids son los **locales de Room**, no los del panel. La app numera
 * por competencia (1xx, 2xx, 3xx…) y guarda aparte el `django_id`. Lectura
 * Critica es 101-103 aqui y 1-3 alli.
 *
 * Faltan a proposito:
 *
 * - **Los cuatro Calentamiento** (100, 200, 300, 400). Llevan el rayo, que
 *   distingue la practica de la evaluacion mejor que un dibujo.
 * - **Comunicacion Escrita** (501), que todavia no tiene arte. Se queda con la
 *   estrella hasta que lo tenga; el dia que exista, entra añadiendo una linea.
 */
fun iconoDeNivel(levelId: Int): Int? = when (levelId) {
    // Lectura Crítica
    101 -> R.mipmap.ic_lectura_nivel1
    102 -> R.mipmap.ic_lectura_nivel2
    103 -> R.mipmap.ic_lectura_nivel3

    // Razonamiento Cuantitativo
    201 -> R.mipmap.ic_razonamiento_nivel1
    202 -> R.mipmap.ic_razonamiento_nivel2
    203 -> R.mipmap.ic_razonamiento_nivel3

    // Inglés
    301 -> R.mipmap.ic_ingles_nivel1
    302 -> R.mipmap.ic_ingles_nivel2
    303 -> R.mipmap.ic_ingles_nivel3
    304 -> R.mipmap.ic_ingles_nivel4

    // Competencias Ciudadanas
    401 -> R.mipmap.ic_ciudadana_nivel1
    402 -> R.mipmap.ic_ciudadana_nivel2
    403 -> R.mipmap.ic_ciudadana_nivel3

    else -> null
}
