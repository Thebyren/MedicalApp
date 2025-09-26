package com.medical.app.util

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult()
    
    val isValid: Boolean
        get() = this is Valid
        
    val error: String?
        get() = (this as? Invalid)?.errorMessage
}

fun ValidationResult.onError(block: (String) -> Unit): ValidationResult {
    if (this is ValidationResult.Invalid) {
        block(errorMessage)
    }
    return this
}

fun ValidationResult.onValid(block: () -> Unit): ValidationResult {
    if (this is ValidationResult.Valid) {
        block()
    }
    return this
}
