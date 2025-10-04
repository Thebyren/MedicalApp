package com.medical.app.util

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R

/**
 * Clase sellada que representa un mensaje de interfaz de usuario
 */
sealed class UiMessage {
    data class StringMessage(val message: String) : UiMessage()
    data class ResourceMessage(@StringRes val resId: Int) : UiMessage()
    data class FormattedMessage(@StringRes val resId: Int, val formatArgs: Array<out Any>) : UiMessage() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FormattedMessage

            if (resId != other.resId) return false
            if (!formatArgs.contentEquals(other.formatArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }
    }

    /**
     * Obtiene el mensaje como cadena
     */
    fun getMessage(context: Context): String {
        return when (this) {
            is StringMessage -> message
            is ResourceMessage -> context.getString(resId)
            is FormattedMessage -> context.getString(resId, *formatArgs)
        }
    }

    companion object {
        /**
         * Crea un mensaje a partir de una excepción
         */
        fun from(throwable: Throwable): UiMessage {
            return when (throwable) {
                is AppException -> StringMessage(throwable.message ?: "Error desconocido")
                else -> StringMessage(throwable.message ?: "Error desconocido")
            }
        }

        /**
         * Crea un mensaje a partir de un recurso de string
         */
        fun from(@StringRes resId: Int): UiMessage {
            return ResourceMessage(resId)
        }

        /**
         * Crea un mensaje formateado a partir de un recurso de string y argumentos
         */
        fun from(@StringRes resId: Int, vararg args: Any): UiMessage {
            return FormattedMessage(resId, args)
        }
    }
}

/**
 * Extensión para mostrar un mensaje en una vista usando Snackbar
 */
fun UiMessage.showAsSnackbar(
    view: android.view.View,
    duration: Int = Snackbar.LENGTH_LONG,
    action: Pair<String, (view: android.view.View) -> Unit>? = null
) {
    val context = view.context
    val message = getMessage(context)
    val snackbar = Snackbar.make(view, message, duration)
    
    action?.let { (actionText, actionListener) ->
        snackbar.setAction(actionText, actionListener)
    }
    
    snackbar.show()
}

/**
 * Extensión para mostrar un mensaje de error en una vista usando Snackbar
 */
fun UiMessage.showErrorAsSnackbar(
    view: android.view.View,
    duration: Int = Snackbar.LENGTH_LONG,
    action: Pair<String, (view: android.view.View) -> Unit>? = null
) {
    val context = view.context
    val message = getMessage(context)
    val snackbar = Snackbar.make(view, message, duration)
    
    // Personalizar el estilo para errores
    snackbar.setBackgroundTint(context.getColor(R.color.red_500))
    snackbar.setTextColor(context.getColor(android.R.color.white))
    
    action?.let { (actionText, actionListener) ->
        snackbar.setActionTextColor(context.getColor(android.R.color.white))
        snackbar.setAction(actionText, actionListener)
    }
    
    snackbar.show()
}

/**
 * Extensión para mostrar un mensaje de éxito en una vista usando Snackbar
 */
fun UiMessage.showSuccessAsSnackbar(
    view: android.view.View,
    duration: Int = Snackbar.LENGTH_SHORT,
    action: Pair<String, (view: android.view.View) -> Unit>? = null
) {
    val context = view.context
    val message = getMessage(context)
    val snackbar = Snackbar.make(view, message, duration)
    
    // Personalizar el estilo para éxito
    snackbar.setBackgroundTint(context.getColor(R.color.green_500))
    snackbar.setTextColor(context.getColor(android.R.color.white))
    
    action?.let { (actionText, actionListener) ->
        snackbar.setActionTextColor(context.getColor(android.R.color.white))
        snackbar.setAction(actionText, actionListener)
    }
    
    snackbar.show()
}

/**
 * Extensión para mostrar un diálogo de error
 */
fun UiMessage.showErrorDialog(
    context: Context,
    title: String? = null,
    positiveButtonText: String = context.getString(R.string.aceptar),
    onDismiss: () -> Unit = {}
) {
    val message = getMessage(context)
    
    // Usar un diálogo de Material Design
    com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
        .setTitle(title ?: context.getString(R.string.error))
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            dialog.dismiss()
            onDismiss()
        }
        .setOnDismissListener { onDismiss() }
        .show()
}

/**
 * Extensión para mostrar un diálogo de confirmación
 */
fun UiMessage.showConfirmationDialog(
    context: Context,
    title: String? = null,
    positiveButtonText: String = context.getString(R.string.aceptar),
    negativeButtonText: String = context.getString(R.string.cancelar),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    val message = getMessage(context)
    
    // Usar un diálogo de Material Design
    com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
        .setTitle(title ?: context.getString(R.string.confirmation))
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            dialog.dismiss()
            onConfirm()
        }
        .setNegativeButton(negativeButtonText) { dialog, _ ->
            dialog.dismiss()
            onDismiss()
        }
        .setOnDismissListener { onDismiss() }
        .show()
}
