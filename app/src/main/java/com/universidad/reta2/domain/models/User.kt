package com.universidad.reta2.domain.models

/**
 * Usuario de la aplicación.
 *
 * [passwordHash] guarda siempre el resultado de `PasswordHasher.hashPassword`,
 * nunca la contraseña escrita por la persona.
 */
data class User(
    val username: String,
    val email: String,
    val passwordHash: String,
    val studentCode: String = "",
    val studentProgram: String = ""
)
