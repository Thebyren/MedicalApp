package com.medical.app.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.data.model.Patient
import com.medical.app.databinding.FragmentPatientDetailBinding
import com.medical.app.util.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PatientDetailFragment : Fragment() {

    private var _binding: FragmentPatientDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PatientDetailViewModel by viewModels()
    private val args: PatientDetailFragmentArgs by navArgs()
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupMenu()
        setupClickListeners()
        observeViewModel()
        
        // Load patient data
        viewModel.loadPatient(args.patientId)
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_patient_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        navigateToEditPatient()
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun setupClickListeners() {
        // Set up any click listeners for the detail view
        binding.btnNewAppointment.setOnClickListener {
            // Navigate to new appointment with this patient
            // findNavController().navigate(PatientDetailFragmentDirections.actionPatientDetailToNewAppointment(args.patientId))
        }
        
        binding.btnViewAppointments.setOnClickListener {
            // Navigate to appointments list filtered by this patient
            // findNavController().navigate(PatientDetailFragmentDirections.actionPatientDetailToAppointmentsList(args.patientId))
        }
        
        binding.btnViewPrescriptions.setOnClickListener {
            // Navigate to prescriptions list for this patient
            // findNavController().navigate(PatientDetailFragmentDirections.actionPatientDetailToPrescriptionsList(args.patientId))
        }
    }
    
    private fun observeViewModel() {
        viewModel.patient.observe(viewLifecycleOwner) { patient ->
            patient?.let { updateUi(it) }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.content.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
        
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PatientDetailViewModel.Event.ShowErrorMessage -> {
                    showError(event.message)
                }
                is PatientDetailViewModel.Event.NavigateBackWithResult -> {
                    if (event.deleted) {
                        // Show a message that the patient was deleted
                        Snackbar.make(
                            binding.root,
                            R.string.patient_deleted_successfully,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    findNavController().navigateUp()
                }
                null -> { /* No action needed */ }
            }
        }
    }
    
    private fun updateUi(patient: Patient) {
        binding.apply {
            // Personal Info
            tvPatientName.text = patient.fullName
            tvPatientInfo.text = getString(
                R.string.patient_info_format,
                getAge(patient.dateOfBirth),
                patient.gender
            )
            
            // Contact Info
            tvPhoneNumber.text = patient.phoneNumber
            tvEmail.text = patient.email ?: getString(R.string.not_specified)
            tvAddress.text = patient.address ?: getString(R.string.not_specified)
            
            // Medical Info
            tvBloodType.text = patient.bloodType ?: getString(R.string.not_specified)
            tvAllergies.text = patient.allergies ?: getString(R.string.none)
            tvNotes.text = patient.notes ?: getString(R.string.none)
            
            // Last Updated
            tvLastUpdated.text = getString(
                R.string.last_updated_format,
                dateFormat.format(patient.updatedAt)
            )
        }
    }
    
    private fun getAge(dateOfBirth: Date): String {
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance().apply { time = dateOfBirth }
        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        
        return "$age ${if (age == 1) "año" else "años"}"
    }
    
    private fun navigateToEditPatient() {
        findNavController().navigate(
            PatientDetailFragmentDirections.actionPatientDetailToAddEditPatient(args.patientId)
        )
    }
    
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_patient)
            .setMessage(R.string.delete_patient_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePatient()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
