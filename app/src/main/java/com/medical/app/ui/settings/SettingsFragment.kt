package com.medical.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.medical.app.R
import com.medical.app.data.local.SessionManager
import com.medical.app.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Modo oscuro (por implementar)
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Implementar cambio de tema
        }
        
        // Notificaciones (por implementar)
        binding.btnNotifications.setOnClickListener {
            // TODO: Navegar a configuración de notificaciones
        }
        
        // Cerrar sesión
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    
    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun performLogout() {
        // Cerrar sesión usando SessionManager
        sessionManager.logoutUser()
        
        // Navegar a la pantalla de login y limpiar el back stack
        findNavController().navigate(
            R.id.action_settingsFragment_to_loginFragment
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
