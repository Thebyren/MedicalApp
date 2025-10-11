package com.medical.app.util.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin

/**
 * Oculta el teclado virtual.
 */
fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

/**
 * Oculta el teclado virtual.
 */
fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

/**
 * Oculta el teclado virtual para la vista dada.
 */
fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * Renderiza texto Markdown en un TextView.
 * Soporta: negrita, cursiva, listas, tablas, tareas, tachado, etc.
 */
fun TextView.setMarkdown(markdown: String) {
    val markwon = Markwon.builder(context)
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TablePlugin.create(context))
        .usePlugin(TaskListPlugin.create(context))
        .build()
    
    markwon.setMarkdown(this, markdown)
}

/**
 * Crea una instancia de Markwon configurada para usar en múltiples TextViews.
 * Útil cuando necesitas renderizar markdown en varios lugares.
 */
fun Context.createMarkwon(): Markwon {
    return Markwon.builder(this)
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TablePlugin.create(this))
        .usePlugin(TaskListPlugin.create(this))
        .build()
}
