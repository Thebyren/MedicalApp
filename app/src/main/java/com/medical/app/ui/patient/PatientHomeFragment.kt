package com.medical.app.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.medical.app.data.entities.Appointment
import com.medical.app.data.local.SessionManager
import com.medical.app.databinding.FragmentPatientHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PatientHomeFragment : Fragment() {

    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PatientHomeViewModel by viewModels()
    private val args: PatientHomeFragmentArgs by navArgs()
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    private lateinit var appointmentAdapter: AppointmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        
        // Cargar datos del paciente
        viewModel.loadPatientData(args.patientId)
    }
    
    private fun setupRecyclerView() {
        appointmentAdapter = AppointmentAdapter { appointment ->
            // Navegar al detalle de la cita
            // Como el fragmento no existe, esta navegación se queda comentada.
            // findNavController().navigate(PatientHomeFragmentDirections.actionToAppointmentDetail(appointment.id))
        }
        
        binding.rvAppointments.apply {
            adapter = appointmentAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupClickListeners() {
        val action = PatientHomeFragmentDirections.actionPatientHomeToRegistroConsultaFragment(args.patientId.toString())

        // Configurar menú del toolbar
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_notifications -> {
                    showError("Notificaciones próximamente")
                    true
                }
                R.id.action_settings -> {
                    // Navegar al perfil del paciente (que incluye logout)
                    findNavController().navigate(
                        PatientHomeFragmentDirections.actionPatientHomeToPatientProfile(args.patientId)
                    )
                    true
                }
                R.id.action_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }

        // Card de Recetas
        binding.cardPrescriptions.setOnClickListener {
            findNavController().navigate(
                PatientHomeFragmentDirections.actionPatientHomeToPatientPrescriptions(args.patientId)
            )
        }
        
        // Card de Perfil
        binding.cardProfile.setOnClickListener {
            findNavController().navigate(
                PatientHomeFragmentDirections.actionPatientHomeToPatientProfile(args.patientId)
            )
        }

        // Botón de nueva cita (FAB)
        binding.fabNewAppointment.setOnClickListener {
            findNavController().navigate(action)
        }
        
        // Botón de nueva cita (cuando no hay citas)
        binding.cardNoAppointments.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnNewAppointment)
            .setOnClickListener {
                findNavController().navigate(action)
            }
            
        // Botón de ver detalles de la próxima cita
        binding.cardNextAppointment.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewAppointment)
            .setOnClickListener {
                viewModel.uiState.value.nextAppointment?.let { appointment ->
                    showError("Detalles de cita próximamente")
                }
            }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        updateUI(state)
                    }
                }
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            showError(it)
                            viewModel.clearError()
                        }
                    }
                }
                launch {
                    viewModel.upcomingAppointments.collect { appointments ->
                        appointmentAdapter.submitList(appointments)
                        updateAppointmentsUI(appointments)
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: PatientHomeUiState) {
        // Actualizar nombre del paciente
        binding.tvPatientName.text = state.patientName
        
        // Actualizar próxima cita
        state.nextAppointment?.let { appointment ->
            showNextAppointment(appointment)
        } ?: hideNextAppointment()
    }
    
    private fun showNextAppointment(appointment: Appointment) {
        binding.cardNextAppointment.visibility = View.VISIBLE
        binding.cardNoAppointments.visibility = View.GONE
        
        val date = Date(appointment.dateTime.time)
        
        binding.cardNextAppointment.apply {
            findViewById<android.widget.TextView>(R.id.tvAppointmentTitle).text = appointment.title
            findViewById<android.widget.TextView>(R.id.tvAppointmentDate).text = 
                "${dateFormat.format(date)} - ${timeFormat.format(date)}"
            // TODO: Cargar nombre del doctor
            findViewById<android.widget.TextView>(R.id.tvAppointmentDoctor).text = 
                "Dr. " + (appointment.doctorId?.toString() ?: "Sin asignar")
        }
    }
    
    private fun hideNextAppointment() {
        binding.cardNextAppointment.visibility = View.GONE
        binding.cardNoAppointments.visibility = View.VISIBLE
    }
    
    private fun updateAppointmentsUI(appointments: List<Appointment>) {
        // Mostrar u ocultar el mensaje de "No hay citas"
        if (appointments.isEmpty()) {
            // Ya está manejado por el estado de nextAppointment
        } else {
            // El adaptador ya se actualiza automáticamente
        }
    }
    
    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
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
            R.id.action_global_to_loginFragment
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
