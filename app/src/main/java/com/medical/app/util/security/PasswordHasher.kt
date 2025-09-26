package com.medical.app.util.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad para el hashing seguro de contraseñas.
 * Utiliza SHA-256 con sal para mayor seguridad.
 */
@Singleton
class PasswordHasher @Inject constructor() {

    private val secureRandom = SecureRandom()
    private val algorithm = "SHA-256"
    private val saltLength = 32 // Longitud de la sal en bytes

    /**
     * Genera un hash seguro de la contraseña proporcionada.
     * @param password Contraseña en texto plano
     * @return Par (salt, hash) donde salt es la sal aleatoria y hash es el hash de la contraseña
     */
    fun hashPassword(password: String): Pair<String, String> {
        val salt = generateSalt()
        val hash = hashWithSalt(password, salt)
        return salt to hash
    }

    /**
     * Verifica si una contraseña coincide con un hash almacenado.
     * @param password Contraseña a verificar (en texto plano)
     * @param salt Sal utilizada para el hash original
     * @param expectedHash Hash esperado de la contraseña
     * @return true si la contraseña es válida, false en caso contrario
     */
    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val actualHash = hashWithSalt(password, salt)
        return MessageDigest.isEqual(actualHash.toByteArray(), expectedHash.toByteArray())
    }

    /**
     * Genera una sal aleatoria segura.
     * @return Sal codificada en Base64
     */
    private fun generateSalt(): String {
        val salt = ByteArray(saltLength)
        secureRandom.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    /**
     * Genera un hash de la contraseña utilizando la sal proporcionada.
     * @param password Contraseña en texto plano
     * @param salt Sal codificada en Base64
     * @return Hash de la contraseña codificado en Base64
     */
    private fun hashWithSalt(password: String, salt: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val saltBytes = Base64.getDecoder().decode(salt)
        
        // Combina la contraseña con la sal
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val combined = ByteArray(saltBytes.size + passwordBytes.size)
        
        System.arraycopy(saltBytes, 0, combined, 0, saltBytes.size)
        System.arraycopy(passwordBytes, 0, combined, saltBytes.size, passwordBytes.size)
        
        // Aplica múltiples iteraciones para mayor seguridad
        var hash = digest.digest(combined)
        repeat(1000) { // Aplicar múltiples iteraciones
            digest.reset()
            hash = digest.digest(hash)
        }
        
        return Base64.getEncoder().encodeToString(hash)
    }
}
