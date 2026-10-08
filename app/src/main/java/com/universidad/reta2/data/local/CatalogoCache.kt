package com.universidad.reta2.data.local

import com.universidad.reta2.domain.models.Competence
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Catálogo de competencias con su progreso, guardado en memoria.
 *
 * Componerlo cuesta unos cientos de milisegundos: hay que contar las preguntas
 * de cada nivel y cruzarlas con los intentos acertados. Los ViewModel de las
 * pestañas se recrean al volver a ellas, así que sin esta caché el cálculo se
 * repetía en cada cambio de pestaña y obligaba a enseñar el esqueleto de carga
 * una y otra vez.
 *
 * Vive mientras vive el proceso. La capa de progreso llama a [invalidar] en
 * cuanto escribe algo, de modo que nunca se sirve un porcentaje viejo.
 */
@Singleton
class CatalogoCache @Inject constructor() {

    private data class Entrada(val usuario: String, val catalogo: List<Competence>)

    /** Una sola entrada: en la app hay una sesión abierta a la vez. */
    private val entrada = AtomicReference<Entrada?>(null)

    /** Devuelve el catálogo de [usuario], o `null` si hay que recalcularlo. */
    fun obtener(usuario: String): List<Competence>? =
        entrada.get()?.takeIf { it.usuario == usuario }?.catalogo

    fun guardar(usuario: String, catalogo: List<Competence>) {
        entrada.set(Entrada(usuario, catalogo))
    }

    fun invalidar() {
        entrada.set(null)
    }
}
