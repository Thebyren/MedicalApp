// MedicoHomeFragment.kt
package com.medical.app.ui.medico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.data.model.DoctorStats
import com.medical.app.databinding.FragmentMedicoHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MedicoHomeFragment : Fragment() {

    private var _binding: FragmentMedicoHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MedicoHomeViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicoHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupBottomNavigation()
        setupQuickActions()
        setupObservers()
        updateWelcomeMessage()
    }


    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // Abrir el menú lateral (Navigation Drawer)
            // (activity as? AppCompatActivity)?.findViewById<DrawerLayout>(R.id.drawer_layout)?.openDrawer(GravityCompat.START)
        }
    }

    private fun setupBottomNavigation() {
        val navController = findNavController()
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    private fun setupQuickActions() {
        // Agregar Paciente
        binding.cardAddPatient.setOnClickListener {
            findNavController().navigate(R.id.action_medicoHomeFragment_to_patientListFragment)
        }

        // Nueva Cita
        binding.cardNewAppointment.setOnClickListener {
            findNavController().navigate(R.id.action_medicoHomeFragment_to_newAppointmentFragment)
        }

        // Ver Agenda
        binding.cardViewSchedule.setOnClickListener {
            findNavController().navigate(R.id.action_medicoHomeFragment_to_scheduleFragment)
        }

        // Recetas
        binding.cardPrescriptions.setOnClickListener {
            findNavController().navigate(R.id.action_medicoHomeFragment_to_prescriptionsFragment)
        }

        // Reportes
        binding.cardReports.setOnClickListener {
            findNavController().navigate(R.id.action_medicoHomeFragment_to_reportsFragment)
        }

        // Más Opciones
        binding.cardMoreOptions.setOnClickListener {
            showMoreOptions()
        }
    }

    private fun updateWelcomeMessage() {
        // Obtener el usuario actual desde SessionManager
        val currentUser = viewModel.getCurrentUser()
        
        // Actualizar el mensaje de bienvenida con el nombre del médico
        val welcomeMessage = if (currentUser != null) {
            "¡Bienvenido, Dr. ${currentUser.nombreCompleto}!"
        } else {
            "¡Bienvenido, Doctor!"
        }
        binding.tvWelcome.text = welcomeMessage
        
        // Actualizar la fecha actual
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val currentDate = dateFormat.format(Date())
        binding.tvDate.text = "Hoy es $currentDate"
    }

    private fun showMoreOptions() {
        // Mostrar un menú con más opciones
        val options = arrayOf(
            "Configuración",
            "Cerrar Sesión"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Opciones")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> findNavController().navigate(R.id.action_medicoHomeFragment_to_settingsFragment)
                    1 -> {
                        // Cerrar sesión
                        // viewModel.logout()
                        findNavController().navigate(R.id.action_medicoHomeFragment_to_loginFragment)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        // Cargar estadísticas del médico actual
        val currentUser = viewModel.getCurrentUser()
        currentUser?.let {
            viewModel.loadDoctorStats(it.id.toString())
        }
        
        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            updateStatsUI(stats)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateStatsUI(stats: DoctorStats) {
        binding.tvTotalPatients.text = stats.totalPatients.toString()
        binding.tvAppointmentsToday.text = stats.appointmentsToday.toString()
        binding.tvMonthlyEarnings.text = formatCurrency(stats.monthlyEarnings)
    }
    
    private fun formatCurrency(amount: Double): String {
        return if (amount == 0.0) {
            "$0"
        } else {
            "$${String.format("%,.0f", amount)}"
        }
    }
}