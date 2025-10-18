package com.medical.app.ui.appointment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.data.model.Patient
import com.medical.app.databinding.FragmentNewAppointmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NewAppointmentFragment : Fragment() {

    private var _binding: FragmentNewAppointmentBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NewAppointmentViewModel by viewModels()
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    private lateinit var patientAdapter: ArrayAdapter<String>
    private var patientsList: List<Patient> = emptyList()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupAdapters()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Actualizar título según si es edición o creación
        val args = arguments?.let { NewAppointmentFragmentArgs.fromBundle(it) }
        if (args?.appointmentId != null && args.appointmentId != -1L) {
            binding.toolbar.title = "Editar Cita"
        }
    }
    
    private fun setupAdapters() {
        // Adapter para pacientes
        patientAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        binding.actvPatient.setAdapter(patientAdapter)
        
        // Adapter para tipo de consulta
        val typeAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        binding.actvType.setAdapter(typeAdapter)
        
        // Adapter para duración
        val durationAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        binding.actvDuration.setAdapter(durationAdapter)
    }
    
    private fun setupClickListeners() {
        // Selector de fecha
        binding.tilDate.setEndIconOnClickListener {
            showDatePicker()
        }
        
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
        
        // Selector de hora
        binding.tilTime.setEndIconOnClickListener {
            showTimePicker()
        }
        
        binding.etTime.setOnClickListener {
            showTimePicker()
        }
        
        // Botón guardar
        binding.btnSave.setOnClickListener {
            viewModel.onEvent(NewAppointmentEvent.SaveAppointment)
        }
        
        // Selector de paciente
        binding.actvPatient.setOnItemClickListener { _, _, position, _ ->
            val selectedPatient = patientsList.getOrNull(position)
            viewModel.onEvent(NewAppointmentEvent.PatientSelected(selectedPatient))
        }
        
        // Selector de tipo
        binding.actvType.setOnItemClickListener { _, _, position, _ ->
            val type = binding.actvType.adapter?.getItem(position) as? String ?: return@setOnItemClickListener
            viewModel.onEvent(NewAppointmentEvent.TypeSelected(type))
        }
        
        // Selector de duración
        binding.actvDuration.setOnItemClickListener { _, _, position, _ ->
            val duration = binding.actvDuration.adapter?.getItem(position) as? String ?: return@setOnItemClickListener
            viewModel.onEvent(NewAppointmentEvent.DurationSelected(duration))
        }
    }
    
    private fun setupTextWatchers() {
        binding.etTitle.addTextChangedListener { text ->
            viewModel.onEvent(NewAppointmentEvent.TitleChanged(text.toString()))
        }
        
        binding.etDescription.addTextChangedListener { text ->
            viewModel.onEvent(NewAppointmentEvent.DescriptionChanged(text.toString()))
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val state = viewModel.uiState.value
        
        if (state.date != null) {
            calendar.time = state.date
        }
        
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                viewModel.onEvent(NewAppointmentEvent.DateSelected(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Establecer fecha mínima como hoy
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                viewModel.onEvent(NewAppointmentEvent.TimeSelected(time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // Use 24-hour format
        )
        
        timePicker.show()
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar estado
                launch {
                    viewModel.uiState.collectLatest { state ->
                        updateUi(state)
                    }
                }
                
                // Observar pacientes
                launch {
                    viewModel.patients.collectLatest { patients ->
                        updatePatientsList(patients)
                    }
                }
                
                // Observar eventos
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is NewAppointmentViewModel.Event.ShowErrorMessage -> {
                                showError(event.message)
                            }
                            is NewAppointmentViewModel.Event.NavigateBackWithResult -> {
                                if (event.success) {
                                    showSuccess("Cita guardada exitosamente")
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun updateUi(state: NewAppointmentState) {
        binding.progressBar.isVisible = state.isLoading
        binding.content.isEnabled = !state.isLoading
        
        // Actualizar campos
        if (binding.etTitle.text.toString() != state.title) {
            binding.etTitle.setText(state.title)
        }
        
        state.date?.let { date ->
            val formattedDate = dateFormat.format(date)
            if (binding.etDate.text.toString() != formattedDate) {
                binding.etDate.setText(formattedDate)
            }
        }
        
        if (state.time.isNotEmpty() && binding.etTime.text.toString() != state.time) {
            binding.etTime.setText(state.time)
        }
        
        // Actualizar adaptadores de tipo y duración
        @Suppress("UNCHECKED_CAST")
        val typeAdapter = binding.actvType.adapter as? ArrayAdapter<String>
        typeAdapter?.clear()
        typeAdapter?.addAll(state.appointmentTypes)
        
        if (binding.actvType.text.toString() != state.appointmentType) {
            binding.actvType.setText(state.appointmentType, false)
        }
        
        @Suppress("UNCHECKED_CAST")
        val durationAdapter = binding.actvDuration.adapter as? ArrayAdapter<String>
        durationAdapter?.clear()
        durationAdapter?.addAll(state.durationOptions)
        
        if (binding.actvDuration.text.toString() != state.duration) {
            binding.actvDuration.setText(state.duration, false)
        }
        
        if (binding.etDescription.text.toString() != state.description) {
            binding.etDescription.setText(state.description)
        }
        
        // Actualizar paciente seleccionado
        state.selectedPatient?.let { patient ->
            val patientDisplay = "${patient.name} ${patient.lastName}"
            if (binding.actvPatient.text.toString() != patientDisplay) {
                binding.actvPatient.setText(patientDisplay, false)
            }
        }
    }
    
    private fun updatePatientsList(patients: List<Patient>) {
        patientsList = patients
        patientAdapter.clear()
        patientAdapter.addAll(patients.map { "${it.name} ${it.lastName}" })
        patientAdapter.notifyDataSetChanged()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
            .show()
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(requireContext().getColor(android.R.color.holo_green_dark))
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
