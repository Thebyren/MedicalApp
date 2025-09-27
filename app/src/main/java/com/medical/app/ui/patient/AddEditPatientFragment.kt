package com.medical.app.ui.patient

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.databinding.FragmentAddEditPatientBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddEditPatientFragment : Fragment() {

    private var _binding: FragmentAddEditPatientBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddEditPatientViewModel by viewModels()
    private val args: AddEditPatientFragmentArgs by navArgs()
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditPatientBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupForm()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.title = if (args.patientId.isNullOrEmpty()) {
            getString(R.string.add_patient)
        } else {
            getString(R.string.edit_patient)
        }
        
        binding.toolbar.inflateMenu(R.menu.menu_save)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save -> {
                    viewModel.onEvent(AddEditPatientEvent.SavePatient)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupForm() {
        // Setup gender dropdown
        val genders = listOf(
            getString(R.string.male),
            getString(R.string.female),
            getString(R.string.other)
        )
        
        val genderAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            genders
        )
        
        binding.actvGender.setAdapter(genderAdapter)
        
        // Setup blood type dropdown
        val bloodTypes = listOf(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        )
        
        val bloodTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            bloodTypes
        )
        
        binding.actvBloodType.setAdapter(bloodTypeAdapter)
    }
    
    private fun setupClickListeners() {
        // Date of birth picker
        binding.tilDob.setEndIconOnClickListener {
            showDatePicker()
        }
        
        binding.etDob.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                viewModel.onEvent(AddEditPatientEvent.DateOfBirthChanged(selectedDate.time))
            },
            year, month, day
        )
        
        // Set max date to today
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI state
                launch {
                    viewModel.uiState.collect { state ->
                        updateUi(state)
                    }
                }
                
                // Collect events
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AddEditPatientViewModel.Event.ShowErrorMessage -> {
                                showError(event.message)
                            }
                            is AddEditPatientViewModel.Event.NavigateBackWithResult -> {
                                if (event.success) {
                                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                                        PATIENT_SAVED_RESULT,
                                        true
                                    )
                                    findNavController().navigateUp()
                                }
                            }
                            null -> { /* No action needed */ }
                        }
                    }
                }
            }
        }
    }
    
    private fun updateUi(state: AddEditPatientState) {
        // Show/hide loading
        binding.progressBar.isVisible = state.isLoading
        binding.content.isEnabled = !state.isLoading
        
        // Update form fields
        binding.etFirstName.setText(state.firstName)
        binding.etLastName.setText(state.lastName)
        
        state.dateOfBirth?.let { date ->
            binding.etDob.setText(dateFormat.format(date))
        } ?: run {
            binding.etDob.text?.clear()
        }
        
        binding.actvGender.setText(state.gender, false)
        binding.etPhoneNumber.setText(state.phoneNumber)
        binding.etEmail.setText(state.email)
        binding.etAddress.setText(state.address)
        binding.actvBloodType.setText(state.bloodType, false)
        binding.etAllergies.setText(state.allergies)
        binding.etNotes.setText(state.notes)
        
        // Setup text change listeners
        binding.etFirstName.addTextChangedListener { text ->
            viewModel.onEvent(AddEditPatientEvent.FirstNameChanged(text.toString()))
        }
        
        binding.etLastName.addTextChangedListener { text ->
            viewModel.onEvent(AddEditPatientEvent.LastNameChanged(text.toString()))
        }
        
        binding.actvGender.setOnItemClickListener { _, _, position, _ ->
            val gender = binding.actvGender.adapter?.getItem(position) as? String ?: return@setOnItemClickListener
            viewModel.onEvent(AddEditPatientEvent.GenderSelected(gender))
        }
        
        binding.etPhoneNumber.addTextChangedListener { text ->
            viewModel.onEvent(AddEditPatientEvent.PhoneNumberChanged(text.toString()))
        }
        
        binding.etEmail.addTextChangedListener { text ->
            viewModel.onEvent(AddEditPatientEvent.EmailChanged(text.toString()))
        }
        
        binding.etAddress.addTextChangedListener { text ->
            viewModel.onEvent(AddEditPatientEvent.AddressChanged(text.toString()))
        }
        
        binding.actvBloodType.setOnItemClickListener { _, _, position, _ ->
            val bloodType = binding.actvBloodType.adapter?.getItem(position) as? String ?: return@setOnItemClickListener
            viewModel.onEvent(AddEditPatientEvent.BloodTypeSelected(bloodType))
        }
        
        binding.etAllergies.addTextChangedListener { text ->
            viewModel.onEvent(AddEditPatientEvent.AllergiesChanged(text.toString()))
        }
        
        binding.etNotes.addTextChangedListener { text ->
            viewModel.onEvent(AddEditPatientEvent.NotesChanged(text.toString()))
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        const val PATIENT_SAVED_RESULT = "patient_saved_result"
    }
}
