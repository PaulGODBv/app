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
    println("🔍 safeNavigate - Current: $current, Target: $route")

    if (current == route) {
        println("⚠️ safeNavigate - Already on target route, skipping")
        return false
    }

    return runCatching {
        if (builder == null) {
            this.navigate(route) {
                // 🔥 AGREGAR OPCIONES DE NAVEGACIÓN BÁSICAS
                launchSingleTop = true
                restoreState = true
            }
        } else {
            this.navigate(route, builder)
        }
        println("✅ safeNavigate - Navigation successful to: $route")
        true
    }.onFailure { e ->
        println("❌ safeNavigate - Navigation failed: ${e.message}")
        e.printStackTrace() // 🔥 MOSTRAR STACK TRACE COMPLETO
    }.getOrElse { false }
}