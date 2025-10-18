package com.medical.app.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.medical.app.databinding.FragmentScheduleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScheduleViewModel by viewModels()
    private lateinit var adapter: AppointmentsAdapter
    private val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupCalendar()
        setupRecyclerView()
        setupObservers()
        setupFab()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCalendar() {
        // Configurar el calendario para seleccionar fecha
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            viewModel.selectDate(calendar.time)
        }
        
        // Botones de navegación de fecha
        binding.btnPreviousDay.setOnClickListener {
            viewModel.previousDay()
        }
        
        binding.btnNextDay.setOnClickListener {
            viewModel.nextDay()
        }
        
        binding.btnToday.setOnClickListener {
            viewModel.selectToday()
        }
    }

    private fun setupRecyclerView() {
        adapter = AppointmentsAdapter(
            onItemClick = { appointmentWithPatient ->
                // Navegar a editar cita
                val action = ScheduleFragmentDirections
                    .actionScheduleFragmentToNewAppointmentFragment(appointmentWithPatient.appointment.id)
                findNavController().navigate(action)
            },
            onStatusChange = { appointment, newStatus ->
                viewModel.updateAppointmentStatus(appointment, newStatus)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ScheduleFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.emptyLayout.isVisible = !state.isLoading && state.appointmentsWithPatient.isEmpty()
                binding.recyclerView.isVisible = !state.isLoading && state.appointmentsWithPatient.isNotEmpty()
                
                // Actualizar fecha seleccionada
                state.selectedDate?.let { date ->
                    binding.tvSelectedDate.text = dateFormat.format(date)
                    updateCalendarDate(date)
                }
                
                // Actualizar contador de citas
                binding.tvAppointmentCount.text = "${state.appointmentsWithPatient.size} citas"
                
                adapter.submitList(state.appointmentsWithPatient)
                
                state.error?.let { error ->
                    showMessage(error)
                    viewModel.clearError()
                }
            }
        }
    }

    private fun updateCalendarDate(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        binding.calendarView.date = calendar.timeInMillis
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                ScheduleFragmentDirections.actionScheduleFragmentToNewAppointmentFragment()
            )
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
