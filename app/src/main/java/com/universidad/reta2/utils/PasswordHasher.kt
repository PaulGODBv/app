package com.universidad.reta2.utils

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Hashing de contraseñas con SHA-256 y salt aleatorio por usuario.
 *
 * La contraseña **nunca** se guarda en texto plano: lo que se persiste en Room es
 * el resultado de [hashPassword], con el formato
 *
 * ```
 * sha256$<salt en hexadecimal>$<hash en hexadecimal>
 * ```
 *
 * El salt es un valor aleatorio de 16 bytes distinto para cada contraseña. Sirve para
 * que dos usuarios con la misma clave produzcan hashes distintos y para que no se puedan
 * usar tablas precalculadas (rainbow tables) contra la base de datos.
 *
 * Se codifica en hexadecimal a propósito (y no con `android.util.Base64`) para que la
 * clase no dependa del framework de Android y pueda probarse con tests unitarios normales.
 *
 * Nota de seguridad: SHA-256 es una función rápida, pensada para integridad y no para
 * contraseñas. El salt elimina las rainbow tables, pero no encarece un ataque de fuerza
 * bruta contra una contraseña concreta. Si en algún momento se quiere endurecer, basta con
 * reemplazar el cuerpo de [computeHash] por PBKDF2 (`PBKDF2WithHmacSHA256`) e incrementar
 * [ALGORITHM_TAG]; [verifyPassword] seguirá funcionando para los hashes nuevos.
 */
object PasswordHasher {

    private const val ALGORITHM_TAG = "sha256"
    private const val SEPARATOR = "$"
    private const val SALT_LENGTH_BYTES = 16

    private val secureRandom = SecureRandom()

    /**
     * Genera el hash de [password] con un salt nuevo.
     *
     * @return cadena con formato `sha256$salt$hash`, lista para guardarse en la base de datos.
     */
    fun hashPassword(password: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { secureRandom.nextBytes(it) }
        val hash = computeHash(password, salt)
        return listOf(ALGORITHM_TAG, salt.toHex(), hash.toHex()).joinToString(SEPARATOR)
    }

    /**
     * Comprueba si [password] corresponde al [storedHash] guardado.
     *
     * Devuelve `false` —sin lanzar excepciones— si el valor almacenado está vacío o no
     * tiene el formato esperado, de modo que un registro corrupto nunca deje pasar un login.
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        if (!isHashed(storedHash)) return false

        val parts = storedHash.split(SEPARATOR)
        val salt = parts[1].hexToBytesOrNull() ?: return false
        val expectedHash = parts[2].hexToBytesOrNull() ?: return false

        // MessageDigest.isEqual compara en tiempo constante: no revela por cuántos
        // bytes acertó un atacante que mida el tiempo de respuesta.
        return MessageDigest.isEqual(computeHash(password, salt), expectedHash)
    }

    /**
     * Indica si [value] ya tiene el formato de hash de esta clase.
     *
     * Se usa como red de seguridad en la capa de datos para no escribir nunca texto plano.
     */
    fun isHashed(value: String): Boolean {
        val parts = value.split(SEPARATOR)
        return parts.size == 3 &&
                parts[0] == ALGORITHM_TAG &&
                parts[1].isNotEmpty() &&
                parts[2].isNotEmpty()
    }

    private fun computeHash(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { byte -> "%02x".format(byte) }

    private fun String.hexToBytesOrNull(): ByteArray? {
        if (length % 2 != 0) return null
        return try {
            ByteArray(length / 2) { index ->
                substring(index * 2, index * 2 + 2).toInt(16).toByte()
            }
        } catch (e: NumberFormatException) {
            null
        }
    }
}
