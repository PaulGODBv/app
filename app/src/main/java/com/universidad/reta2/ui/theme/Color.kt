package com.universidad.reta2.ui.theme
import androidx.compose.ui.graphics.Color

val Primary100 = Color(0xFF005B99)  // Azul oscuro principal
val Primary200 = Color(0xFF4E88CA)  // Azul medio
val Primary300 = Color(0xFFB7E9FF)  // Azul claro

val Accent100 = Color(0xFFFFD700)   // Dorado brillante
val Accent200 = Color(0xFFE9AA2B)  // Dorado oscuro

val Text100 = Color(0xFF333333)    // Texto principal oscuro
val Text200 = Color(0xFF5C5C5C)    // Texto secundario

val Bg100 = Color(0xFFFFFFFF)      // Fondo blanco puro
val Bg200 = Color(0xFFF8F9FA)      // Fondo gris muy claro para tarjetas
val Bg300 = Color(0xFFE9ECEF)      // Fondo gris medio para elementos secundarios

// Colores adicionales para el estilo de la imagen
val CardBackground = Color(0xFFF8F9FA)     // Fondo de tarjetas secundarias
val StatsCardBackground = Color(0xFF005B99) // Fondo de tarjeta de estadísticas principal
val ProgressBackground = Color(0xFFE9ECEF)  // Fondo de círculos de progreso
val BorderColor = Color(0xFF005B99)         // Color de bordes y separadores

// Colores para feedback de respuestas
val Success100 = Color(0xFF2E7D32)   // Verde para respuestas correctas
val Success200 = Color(0xFF1B5E20)   // Verde oscuro para fondos
val Error100 = Color(0xFFD32F2F)     // Rojo para respuestas incorrectas
val Error200 = Color(0xFFB71C1C)     // Rojo oscuro para fondos de error

// Feedback Oscuro (Más pasteles para legibilidad)
val DarkSuccess100 = Color(0xFF81C784) // Verde pastel
val DarkSuccess200 = Color(0xFF2E7D32) // Verde oscuro original usado como fondo de feedback
val DarkError100 = Color(0xFFE57373)   // Rojo pastel
val DarkError200 = Color(0xFFC62828)   // Rojo oscuro usado como fondo de error

// --- MODO OSCURO — paleta «complementario real» ---
//
// El azul (212°) y el ámbar (38°) son opuestos en el círculo cromático y se
// igualaron en luminancia, de modo que ninguno de los dos aplasta al otro. El
// reparto de trabajo es lo que evita que compitan:
//
//   azul  → acciones y estado: botones, progreso, selección, enlaces
//   ámbar → logro: rachas, insignias, desbloqueos, tramos intermedios
//
// La paleta anterior fallaba justo ahí: el dorado #B9A839 daba 7,78:1 contra el
// fondo y el azul #437ECC solo 4,55:1, así que el acento pesaba más que el
// color principal y la jerarquía se leía al revés.

val DarkPrimary100 = Color(0xFF83B6E8) // Azul claro — acciones (8,16:1 sobre surface)
val DarkPrimary200 = Color(0xFF2E5D91) // Azul medio — inversePrimary
val DarkPrimary300 = Color(0xFF1E4468) // Azul profundo — contenedores

val DarkAccent100 = Color(0xFFD2954B)  // Ámbar cálido — logro (6,76:1 sobre surface)
val DarkAccent200 = Color(0xFF5A3C14)  // Ámbar profundo — contenedores

val DarkTertiary100 = Color(0xFF8FA3B8) // Azul pizarra — tramo intermedio de resultados
val DarkTertiary200 = Color(0xFF2A3541) // Su contenedor

val DarkText100 = Color(0xFFE8ECF1)    // Texto principal
val DarkText200 = Color(0xFFA3ADB8)    // Texto secundario

val DarkBg100 = Color(0xFF0E1116)      // Fondo base
val DarkBg200 = Color(0xFF161A21)      // Superficie (tarjetas)
val DarkBg300 = Color(0xFF252B34)      // Superficie variante

// Antes `outline` y `surfaceVariant` eran el mismo #2C2C2C, y `outlineVariant`
// el mismo #1E1E1E que `surface`: los bordes y los divisores tenían una razón
// de contraste de 1,00 y sencillamente no se veían.
val DarkOutline = Color(0xFF3B444F)        // Bordes visibles
val DarkOutlineVariant = Color(0xFF2A313A) // Divisores discretos

// Esqueleto de carga. Estaba fijo en #E0E0E0/#F5F5F5 dentro del componente, lo
// que daba 12,63:1 sobre una tarjeta oscura: un bloque casi blanco.
val SkeletonBaseLight = Color(0xFFE4E6E9)
val SkeletonGlowLight = Color(0xFFF2F3F5)
val SkeletonBaseDark = Color(0xFF242B34)
val SkeletonGlowDark = Color(0xFF2E3742)

// Colores legacy para compatibilidad
val AzulPrincipal = Primary100
val AzulClaro = Primary200
val FondoClaro = Bg100
val FondoOscuro = DarkBg100