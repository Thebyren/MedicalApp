package com.medical.app.util

import android.util.Patterns

/**
 * Utilidades para validación de campos de formulario.
 */
object ValidationUtils {
    
    /**
     * Valida una dirección de correo electrónico.
     * @param email Correo electrónico a validar
     * @return true si el correo es válido, false en caso contrario
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Valida una contraseña.
     * @param password Contraseña a validar
     * @return Un objeto [PasswordValidationResult] que indica si la contraseña es válida y un mensaje de error si no lo es
     */
    fun validatePassword(password: String): PasswordValidationResult {
        if (password.length < 8) {
            return PasswordValidationResult(
                isValid = false,
                errorMessage = "La contraseña debe tener al menos 8 caracteres"
            )
        }
        
        if (!password.any { it.isDigit() }) {
            return PasswordValidationResult(
                isValid = false,
                errorMessage = "La contraseña debe contener al menos un número"
            )
        }
        
        if (!password.any { it.isLetter() }) {
            return PasswordValidationResult(
                isValid = false,
                errorMessage = "La contraseña debe contener al menos una letra"
            )
        }
        
        return PasswordValidationResult(isValid = true)
    }
    
    /**
     * Valida un nombre completo.
     * @param fullName Nombre completo a validar
     * @return Un objeto [ValidationResult] que indica si el nombre es válido y un mensaje de error si no lo es
     */
    fun validateFullName(fullName: String): ValidationResult {
        if (fullName.trim().length < 3) {
            return ValidationResult(
                isValid = false,
                errorMessage = "El nombre debe tener al menos 3 caracteres"
            )
        }
        
        if (fullName.trim().split(" ").size < 2) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Por favor ingrese su nombre completo"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Resultado de la validación de contraseña.
     * @property isValid Indica si la contraseña es válida
     * @property errorMessage Mensaje de error si la contraseña no es válida
     */
    data class PasswordValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    /**
     * Resultado genérico de validación.
     * @property isValid Indica si la validación fue exitosa
     * @property errorMessage Mensaje de error si la validación falla
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
}
