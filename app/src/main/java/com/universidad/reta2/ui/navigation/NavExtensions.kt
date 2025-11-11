package com.universidad.reta2.ui.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import kotlin.runCatching

/**
 * Navega solo si la ruta objetivo no es la misma que la actual.
 * Devuelve true si se lanzó la navegación.
 */
fun NavHostController.safeNavigate(route: String, builder: (NavOptionsBuilder.() -> Unit)? = null): Boolean {
    val current = this.currentBackStackEntry?.destination?.route
    if (current == route) return false
    return runCatching {
        if (builder == null) this.navigate(route) else this.navigate(route, builder)
    }.isSuccess
}