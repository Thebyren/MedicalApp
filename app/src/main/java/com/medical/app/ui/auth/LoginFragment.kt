package com.medical.app.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.utils.Result
import com.medical.app.data.local.AuthState
import com.medical.app.data.local.SessionManager
import com.medical.app.data.entities.enums.TipoUsuario
import com.medical.app.databinding.FragmentLoginBinding
import com.medical.app.ui.auth.viewmodel.LoginViewModel
import com.medical.app.util.extensions.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    private var hasNavigated = false // Flag para evitar múltiples navegaciones

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar si ya hay una sesión activa
        checkCurrentSession()
        setupClickListeners()
        observeViewModel()
    }

    private fun checkCurrentSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Usar collect en lugar de collectLatest para no cancelar en cada emisión
            sessionManager.authState.collect { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        Log.d(TAG, "Usuario ya autenticado detectado")
                        // Si ya está autenticado, navegar a la pantalla principal
                        if (!hasNavigated) {
                            navigateToMain()
                        }
                    }
                    is AuthState.Error -> {
                        showMessage(authState.errorMessage)
                    }
                    else -> {
                        Log.d(TAG, "AuthState: $authState")
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnLogin.setOnClickListener {
                // Ocultar el teclado al hacer clic en el botón
                hideKeyboard()

                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()

                if (validateInputs(email, password)) {
                    viewModel.login(email, password)
                }
            }

            tvRegisterHere.setOnClickListener {
                navigateToRegister()
            }

            tvForgotPassword.setOnClickListener {
                // TODO: Implementar flujo de recuperación de contraseña
                showMessage("Función de recuperación de contraseña no implementada")
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Success -> {
                        showLoading(false)
                        showMessage("Inicio de sesión exitoso")

                        // NO llamar a navigateToMain() aquí
                        // El checkCurrentSession() se encargará de la navegación
                        // cuando detecte que el authState cambió a Authenticated
                        Log.d(TAG, "Login exitoso, esperando cambio en authState")
                    }
                    is Result.Error -> {
                        showLoading(false)
                        val errorMessage = result.exception.message ?: "Error desconocido al iniciar sesión"
                        showMessage(errorMessage)
                    }
                    else -> { /* No action needed for other states */ }
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            binding.tilEmail.error = "Por favor ingrese su correo electrónico"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Por favor ingrese un correo electrónico válido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isBlank()) {
            binding.tilPassword.error = "Por favor ingrese su contraseña"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
            etEmail.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
        }
    }

    private fun showMessage(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun navigateToMain() {
        // Evitar navegaciones múltiples
        if (hasNavigated) {
            Log.d(TAG, "Ya se navegó, ignorando llamada adicional")
            return
        }

        try {
            // Obtener el usuario actual y su tipo
            val currentUser = sessionManager.getCurrentUser()
            if (currentUser == null) {
                Log.e(TAG, "No hay usuario en sesión")
                showMessage("Error: No se pudo obtener la información del usuario")
                return
            }

            Log.d(TAG, "Navegando según tipo de usuario: ${currentUser.tipoUsuario}")
            hasNavigated = true

            when (currentUser.tipoUsuario) {
                TipoUsuario.MEDICO -> {
                    // Verificar si ya estamos en el destino
                    val currentDestination = findNavController().currentDestination?.id
                    if (currentDestination == R.id.medicoHomeFragment) {
                        Log.d(TAG, "Ya estamos en medicoHomeFragment")
                        return
                    }
                    
                    val action = LoginFragmentDirections.actionLoginFragmentToMedicoHomeFragment()
                    findNavController().navigate(
                        action,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.loginFragment, true)
                            .setLaunchSingleTop(true)
                            .build()
                    )
                }
                TipoUsuario.PACIENTE -> {
                    // Navegar a la vista del paciente
                    // Primero necesitamos obtener el ID del paciente desde la tabla de pacientes
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            val patientId = currentUser.id.toLong() // Por ahora usar el ID del usuario
                            val action = LoginFragmentDirections.actionLoginFragmentToPatientHomeFragment(patientId)
                            findNavController().navigate(
                                action,
                                NavOptions.Builder()
                                    .setPopUpTo(R.id.loginFragment, true)
                                    .setLaunchSingleTop(true)
                                    .build()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error navegando a PatientHome", e)
                            showMessage("Error al cargar la vista del paciente")
                            hasNavigated = false
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en navegación", e)
            hasNavigated = false // Resetear el flag si falla
            showMessage("Error en la navegación")
        }
    }

    private fun navigateToRegister() {
        try {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e(TAG, "Error navegando a registro", e)
            showMessage("Error en la navegación")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}