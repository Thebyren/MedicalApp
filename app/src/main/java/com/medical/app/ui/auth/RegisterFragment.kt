package com.medical.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.data.entities.enums.TipoUsuario
import com.medical.app.utils.Result
import com.medical.app.databinding.FragmentRegisterBinding
import com.medical.app.ui.auth.viewmodel.RegisterViewModel
import com.medical.app.util.extensions.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    private val userTypes = listOf(
        TipoUsuario.MEDICO to "Médico",
        TipoUsuario.PACIENTE to "Paciente"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Configurar el spinner de tipos de usuario
        val userTypeStrings = userTypes.map { it.second }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            userTypeStrings
        )

        binding.spinnerUserType.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            hideKeyboard()

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val fullName = binding.etFullName.text.toString().trim()
            val selectedUserTypeText = binding.spinnerUserType.text.toString().trim()

            // Validar que se haya seleccionado un tipo de usuario
            if (selectedUserTypeText.isEmpty()) {
                showMessage("Por favor selecciona un tipo de usuario")
                return@setOnClickListener
            }

            // Buscar el tipo de usuario de forma segura
            val selectedUserTypePair = userTypes.firstOrNull { it.second == selectedUserTypeText }

            if (selectedUserTypePair == null) {
                showMessage("Tipo de usuario no válido")
                return@setOnClickListener
            }

            val selectedUserType = selectedUserTypePair.first

            if (validateInputs(email, password, confirmPassword, fullName)) {
                viewModel.register(email, password, fullName, selectedUserType)
            }
        }

        binding.tvLoginHere.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registrationState.collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Success -> {
                        showLoading(false)
                        showMessage("Registro exitoso. Por favor inicia sesión.")
                        navigateToLogin()
                    }
                    is Result.Error -> {
                        showLoading(false)
                        showMessage(result.exception.message ?: "Error en el registro")
                    }
                    else -> { /* No action needed */ }
                }
            }
        }
    }

    private fun validateInputs(
        email: String,
        password: String,
        confirmPassword: String,
        fullName: String
    ): Boolean {
        var isValid = true

        // Validar nombre completo
        if (fullName.isBlank()) {
            binding.tilFullName.error = "Por favor ingrese su nombre completo"
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        // Validar email
        if (email.isBlank()) {
            binding.tilEmail.error = "Por favor ingrese su correo electrónico"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Por favor ingrese un correo electrónico válido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Validar contraseña
        if (password.isBlank()) {
            binding.tilPassword.error = "Por favor ingrese una contraseña"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        // Validar confirmación de contraseña
        if (confirmPassword.isBlank()) {
            binding.tilConfirmPassword.error = "Por favor confirme su contraseña"
            isValid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnRegister.isEnabled = !isLoading
            etEmail.isEnabled = !isLoading
            etPassword.isEnabled = !isLoading
            etConfirmPassword.isEnabled = !isLoading
            etFullName.isEnabled = !isLoading
            spinnerUserType.isEnabled = !isLoading
        }
    }

    private fun showMessage(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun navigateToLogin() {
        val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}