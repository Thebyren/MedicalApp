package com.medical.app.ui.consulta

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.databinding.FragmentRegistroConsultaBinding
import com.medical.app.ui.auth.AuthState
import com.medical.app.ui.consulta.RegistroConsultaEvent.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class RegistroConsultaFragment : Fragment() {

    private var _binding: FragmentRegistroConsultaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegistroConsultaViewModel by viewModels()
    private val args: RegistroConsultaFragmentArgs by navArgs()

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistroConsultaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupForm()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            if (isFormDirty()) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun setupForm() {
        // Configurar listeners de los campos
        binding.etMotivo.addTextChangedListener(createTextWatcher { viewModel.onEvent(MotivoChanged(it)) })
        binding.etSintomas.addTextChangedListener(createTextWatcher { viewModel.onEvent(SintomasChanged(it)) })
        binding.etDiagnostico.addTextChangedListener(createTextWatcher { viewModel.onEvent(DiagnosticoChanged(it)) })
        binding.etTratamiento.addTextChangedListener(createTextWatcher { viewModel.onEvent(TratamientoChanged(it)) })
        binding.etNotas.addTextChangedListener(createTextWatcher { viewModel.onEvent(NotasChanged(it)) })

        // Configurar selector de fecha para próxima cita
        binding.tilProximaCita.setEndIconOnClickListener {
            showDatePicker()
        }

        // Configurar botón de guardar
        binding.btnGuardar.setOnClickListener {
            viewModel.onEvent(Submit(args.patientId.toLong()))
        }
    }

    private fun createTextWatcher(onTextChanged: (String) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onTextChanged(s?.toString() ?: "")
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                viewModel.onEvent(ProximaCitaChanged(selectedDate.time))
                binding.etProximaCita.setText(dateFormatter.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Establecer fecha mínima como mañana
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.datePicker.minDate = calendar.timeInMillis

        datePicker.show()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RegistroConsultaState.Idle -> {
                            binding.progressBar.isVisible = false
                            binding.btnGuardar.isEnabled = false
                        }
                        is RegistroConsultaState.FormValidation -> {
                            binding.btnGuardar.isEnabled = state.isFormValid
                        }
                        is RegistroConsultaState.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.btnGuardar.isEnabled = false
                        }
                        is RegistroConsultaState.Success -> {
                            binding.progressBar.isVisible = false
                            showSuccessMessage()
                            findNavController().navigateUp()
                        }
                        is RegistroConsultaState.Error -> {
                            binding.progressBar.isVisible = false
                            // Re-enable button based on last known form validity
                            // Or just enable it to allow retry
                            binding.btnGuardar.isEnabled = true 
                            showError(state.message?: getString(R.string.error_generico))
                        }
                    }
                }
            }
        }
    }

    private fun isFormDirty(): Boolean {
        return binding.etMotivo.text?.isNotBlank() == true ||
               binding.etSintomas.text?.isNotBlank() == true ||
               binding.etDiagnostico.text?.isNotBlank() == true ||
               binding.etTratamiento.text?.isNotBlank() == true ||
               binding.etNotas.text?.isNotBlank() == true ||
               binding.etProximaCita.text?.isNotBlank() == true
    }

    private fun showDiscardChangesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.descartar_cambios)
            .setMessage(R.string.seguro_descartar_cambios)
            .setPositiveButton(R.string.descartar) { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancelar, null)
            .show()
    }

    private fun showSuccessMessage() {
        Snackbar.make(
            binding.root,
            R.string.consulta_guardada_correctamente,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showError(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
