package com.medical.app.ui.prescription

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
import com.medical.app.databinding.FragmentPrescriptionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PrescriptionsFragment : Fragment() {

    private var _binding: FragmentPrescriptionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrescriptionsViewModel by viewModels()
    private lateinit var adapter: PrescriptionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrescriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFab()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = PrescriptionsAdapter(
            onItemClick = { prescription ->
                // TODO: Navegar a detalle de prescripción
                showMessage("Ver prescripción: ${prescription.medicamento}")
            },
            onDeleteClick = { prescription ->
                viewModel.deletePrescription(prescription)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PrescriptionsFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.emptyLayout.isVisible = !state.isLoading && state.prescriptions.isEmpty()
                binding.recyclerView.isVisible = !state.isLoading && state.prescriptions.isNotEmpty()
                
                adapter.submitList(state.prescriptions)
                
                state.error?.let { error ->
                    showMessage(error)
                    viewModel.clearError()
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddPrescriptionDialog()
        }
    }
    
    private fun showAddPrescriptionDialog() {
        val dialog = AddPrescriptionDialog.newInstance()
        dialog.onPrescriptionCreated = { prescription ->
            viewModel.addPrescription(prescription)
            showMessage("Receta creada exitosamente")
        }
        dialog.show(childFragmentManager, AddPrescriptionDialog.TAG)
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
