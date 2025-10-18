package com.medical.app.ui.patient

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.data.entities.enums.Genero
import com.medical.app.data.local.SessionManager
import com.medical.app.databinding.FragmentPatientProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PatientProfileFragment : Fragment() {

    private var _binding: FragmentPatientProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PatientProfileViewModel by viewModels()
    private val args: PatientProfileFragmentArgs by navArgs()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupClickListeners()
        setupObservers()
        
        // Cargar datos del paciente
        viewModel.loadPatientData(args.patientId)
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupClickListeners() {
        // Fecha de nacimiento
        binding.etBirthDate.setOnClickListener {
            showDatePicker()
        }
        
        // Género
        binding.etGender.setOnClickListener {
            showGenderPicker()
        }
        
        // Botón guardar
        binding.btnSave.setOnClickListener {
            saveProfile()
        }
        
        // Cambiar contraseña
        binding.btnChangePassword.setOnClickListener {
            showMessage("Cambiar contraseña próximamente")
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.scrollView.isVisible = !state.isLoading
                
                if (!state.isLoading) {
                    populateFields(state)
                }
                
                state.error?.let { error ->
                    showMessage(error)
                    viewModel.clearError()
                }
                
                if (state.profileUpdated) {
                    showMessage("Perfil actualizado correctamente")
                    viewModel.clearUpdateFlag()
                }
            }
        }
    }
    
    private fun populateFields(state: PatientProfileUiState) {
        state.patient?.let { patient ->
            binding.apply {
                etName.setText(patient.nombre)
                etLastName.setText(patient.apellidos)
                etEmail.setText(patient.email)
                etPhone.setText(patient.telefono ?: "")
                etAddress.setText(patient.direccion ?: "")
                etSSN.setText(patient.numeroSeguridadSocial ?: "")
                etEmergencyContact.setText(patient.contactoEmergencia ?: "")
                etEmergencyPhone.setText(patient.telefonoEmergencia ?: "")
                etBloodType.setText(patient.bloodType)
                etAllergies.setText(patient.allergies)
                etNotes.setText(patient.notes)
                
                patient.fechaNacimiento?.let {
                    etBirthDate.setText(dateFormat.format(it))
                }
                
                patient.genero?.let {
                    etGender.setText(getGenderText(it))
                }
            }
        }
    }
    
    private fun getGenderText(genero: Genero): String {
        return when (genero) {
            Genero.MASCULINO -> "Masculino"
            Genero.FEMENINO -> "Femenino"
            Genero.OTRO -> "Otro"
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        // Si ya hay una fecha, usarla como inicial
        binding.etBirthDate.text?.toString()?.let { dateText ->
            try {
                dateFormat.parse(dateText)?.let { date ->
                    calendar.time = date
                }
            } catch (e: Exception) {
                // Ignorar error de parseo
            }
        }
        
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            binding.etBirthDate.setText(dateFormat.format(calendar.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            .show()
    }
    
    private fun showGenderPicker() {
        val genders = arrayOf("Masculino", "Femenino", "Otro")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccionar género")
            .setItems(genders) { _, which ->
                binding.etGender.setText(genders[which])
            }
            .show()
    }
    
    private fun saveProfile() {
        val updatedPatient = viewModel.uiState.value.patient?.copy(
            nombre = binding.etName.text.toString(),
            apellidos = binding.etLastName.text.toString(),
            email = binding.etEmail.text.toString(),
            telefono = binding.etPhone.text.toString().ifEmpty { null },
            direccion = binding.etAddress.text.toString().ifEmpty { null },
            numeroSeguridadSocial = binding.etSSN.text.toString().ifEmpty { null },
            contactoEmergencia = binding.etEmergencyContact.text.toString().ifEmpty { null },
            telefonoEmergencia = binding.etEmergencyPhone.text.toString().ifEmpty { null },
            bloodType = binding.etBloodType.text.toString(),
            allergies = binding.etAllergies.text.toString(),
            notes = binding.etNotes.text.toString(),
            fechaNacimiento = try {
                dateFormat.parse(binding.etBirthDate.text.toString())
            } catch (e: Exception) {
                viewModel.uiState.value.patient.fechaNacimiento
            },
            genero = when (binding.etGender.text.toString()) {
                "Masculino" -> Genero.MASCULINO
                "Femenino" -> Genero.FEMENINO
                "Otro" -> Genero.OTRO
                else -> viewModel.uiState.value.patient.genero
            }
        )
        
        updatedPatient?.let {
            viewModel.updateProfile(it)
        }
    }
    
    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun logout() {
        // Cerrar sesión
        sessionManager.logoutUser()
        
        // Navegar al login y limpiar el back stack
        findNavController().navigate(
            PatientProfileFragmentDirections.actionGlobalToLoginFragment()
        )
    }
    
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
