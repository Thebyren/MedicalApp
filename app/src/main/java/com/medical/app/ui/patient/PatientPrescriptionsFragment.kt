package com.medical.app.ui.patient

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
import com.medical.app.databinding.FragmentPatientPrescriptionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PatientPrescriptionsFragment : Fragment() {

    private var _binding: FragmentPatientPrescriptionsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PatientPrescriptionsViewModel by viewModels()
    private lateinit var prescriptionsAdapter: PatientPrescriptionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientPrescriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        
        // Cargar recetas del paciente
        viewModel.loadPrescriptions()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        prescriptionsAdapter = PatientPrescriptionsAdapter { prescription ->
            // Mostrar detalles de la receta
            showPrescriptionDetails(prescription)
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = prescriptionsAdapter
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.emptyLayout.isVisible = !state.isLoading && state.prescriptions.isEmpty()
                binding.recyclerView.isVisible = !state.isLoading && state.prescriptions.isNotEmpty()
                
                prescriptionsAdapter.submitList(state.prescriptions)
                
                // Actualizar estadísticas
                binding.tvActivePrescriptions.text = "${state.activePrescriptions}"
                binding.tvTotalPrescriptions.text = "${state.totalPrescriptions}"
                
                state.error?.let { error ->
                    showMessage(error)
                    viewModel.clearError()
                }
            }
        }
    }
    
    private fun showPrescriptionDetails(prescription: PatientPrescription) {
        // TODO: Implementar diálogo o navegación a detalle de receta
        showMessage("Receta: ${prescription.medicationName}")
    }
    
    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
