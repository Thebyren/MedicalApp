package com.medical.app.util

import android.text.TextUtils
import java.util.regex.Pattern

object Validators {
    
    // Validación de campos requeridos
    fun requiredField(value: String?, fieldName: String): ValidationResult {
        return if (value.isNullOrBlank()) {
            ValidationResult.Invalid("El campo $fieldName es requerido")
        } else {
            ValidationResult.Valid
        }
    }
    
    // Validación de longitud mínima
    fun minLength(value: String, minLength: Int, fieldName: String): ValidationResult {
        return if (value.length < minLength) {
            ValidationResult.Invalid("$fieldName debe tener al menos $minLength caracteres")
        } else {
            ValidationResult.Valid
        }
    }
    
    // Validación de longitud máxima
    fun maxLength(value: String, maxLength: Int, fieldName: String): ValidationResult {
        return if (value.length > maxLength) {
            ValidationResult.Invalid("$fieldName no debe exceder los $maxLength caracteres")
        } else {
            ValidationResult.Valid
        }
    }
    
    // Validación de formato de email
    fun email(value: String?): ValidationResult {
        if (value.isNullOrBlank()) return ValidationResult.Valid
        
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+"
        return if (value.matches(Regex(emailPattern))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("El formato del correo electrónico no es válido")
        }
    }
    
    // Validación de formato de teléfono
    fun phoneNumber(value: String?): ValidationResult {
        if (value.isNullOrBlank()) return ValidationResult.Valid
        
        val phonePattern = "^[+]?[0-9]{10,13}$"
        return if (value.matches(Regex(phonePattern))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("El formato del teléfono no es válido")
        }
    }
    
    // Validación de fechas (fecha no futura)
    fun notFutureDate(date: java.util.Date?, fieldName: String): ValidationResult {
        if (date == null) return ValidationResult.Valid
        
        val today = java.util.Date()
        return if (date.after(today)) {
            ValidationResult.Invalid("La fecha de $fieldName no puede ser futura")
        } else {
            ValidationResult.Valid
        }
    }
    
    // Validación de rango de fechas (fecha de inicio antes que fecha de fin)
    fun validDateRange(startDate: java.util.Date?, endDate: java.util.Date?): ValidationResult {
        if (startDate == null || endDate == null) return ValidationResult.Valid
        
        return if (startDate.after(endDate)) {
            ValidationResult.Invalid("La fecha de inicio debe ser anterior a la fecha de fin")
        } else {
            ValidationResult.Valid
        }
    }
    
    // Validación de número dentro de rango
    fun inRange(
        value: Number, 
        min: Number, 
        max: Number, 
        fieldName: String
    ): ValidationResult {
        return if (value.toDouble() < min.toDouble() || value.toDouble() > max.toDouble()) {
            ValidationResult.Invalid("$fieldName debe estar entre $min y $max")
        } else {
            ValidationResult.Valid
        }
    }
    
    // Validación de campos que deben ser iguales (ej: contraseñas)
    fun fieldsMatch(
        value1: String, 
        value2: String, 
        fieldName1: String, 
        fieldName2: String
    ): ValidationResult {
        return if (value1 != value2) {
            ValidationResult.Invalid("Los campos $fieldName1 y $fieldName2 no coinciden")
        } else {
            ValidationResult.Valid
        }
    }
    
    // Validación de formato de CURP
    fun curp(value: String?): ValidationResult {
        if (value.isNullOrBlank()) return ValidationResult.Valid
        
        val curpPattern = "^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[0-9A-Z]{2}$"
        return if (value.matches(Regex(curpPattern, RegexOption.IGNORE_CASE))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("El formato del CURP no es válido")
        }
    }
    
    // Validación de RFC
    fun rfc(value: String?): ValidationResult {
        if (value.isNullOrBlank()) return ValidationResult.Valid
        
        val rfcPattern = "^[A-Z&Ñ]{3,4}[0-9]{6}[A-Z0-9]{3}$"
        return if (value.matches(Regex(rfcPattern, RegexOption.IGNORE_CASE))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("El formato del RFC no es válido")
        }
    }
    
    // Validación de código postal mexicano
    fun zipCode(value: String?): ValidationResult {
        if (value.isNullOrBlank()) return ValidationResult.Valid
        
        val zipPattern = "^[0-9]{5}$"
        return if (value.matches(Regex(zipPattern))) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("El código postal debe tener 5 dígitos")
        }
    }
}
