package com.medical.app.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragmento base que proporciona funcionalidad común para todos los fragmentos
 */
abstract class BaseFragment<VB : ViewBinding, VM : BaseViewModel> : Fragment() {

    protected abstract val viewModel: VM

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    @get:LayoutRes
    protected abstract val layoutResId: Int

    /**
     * Método para inflar el binding del fragmento
     */
    protected abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    /**
     * Configura la interfaz de usuario
     */
    protected open fun setupUI() {
        // Implementar en las clases hijas si es necesario
    }

    /**
     * Configura los observadores del ViewModel
     */
    protected open fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar cambios en el estado de carga
                viewModel.isLoading.collect { isLoading ->
                    showLoading(isLoading)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar mensajes de error
                viewModel.errorMessage.collect { message ->
                    message?.let { showError(it) }
                }
            }
        }
    }

    /**
     * Muestra u oculta el indicador de carga
     */
    protected open fun showLoading(isLoading: Boolean) {
        // Implementar en las clases hijas según sea necesario
    }

    /**
     * Muestra un mensaje de error
     */
    protected open fun showError(message: String) {
        view?.let { view ->
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.aceptar) { /* Acción opcional */ }
                .show()
        }
    }

    /**
     * Muestra un mensaje de éxito
     */
    protected fun showSuccess(message: String) {
        view?.let { view ->
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(R.color.green_500))
                .show()
        }
    }

    /**
     * Método de conveniencia para recolectar flujos de manera segura
     */
    protected fun <T> Flow<T>.collectWithLifecycle(
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
                collect(action)
            }
        }
    }

    /**
     * Método de conveniencia para recolectar el último valor de un flujo de manera segura
     */
    protected fun <T> Flow<T>.collectLatestWithLifecycle(
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(minActiveState) {
                collectLatest(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Muestra un diálogo de error
     */
    protected fun showErrorDialog(
        title: String = getString(R.string.error),
        message: String,
        positiveButtonText: String = getString(R.string.aceptar),
        onPositiveClick: (() -> Unit)? = null,
        negativeButtonText: String? = null,
        onNegativeClick: (() -> Unit)? = null,
        cancelable: Boolean = true
    ) {
        // Implementar diálogo de error personalizado si es necesario
        // Por ahora usamos un Snackbar como ejemplo
        showError(message)
    }

    /**
     * Muestra un diálogo de confirmación
     */
    protected fun showConfirmationDialog(
        title: String,
        message: String,
        positiveButtonText: String = getString(R.string.aceptar),
        negativeButtonText: String = getString(R.string.cancelar),
        onConfirmed: () -> Unit,
        onDismissed: (() -> Unit)? = null
    ) {
        // Implementar diálogo de confirmación personalizado si es necesario
        // Por ahora usamos un Snackbar como ejemplo
        showError("Diálogo de confirmación: $message")
    }
}
