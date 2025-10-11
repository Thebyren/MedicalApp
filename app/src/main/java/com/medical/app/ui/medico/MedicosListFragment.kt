package com.medical.app.ui.medico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.databinding.FragmentMedicosListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicosListFragment : Fragment() {

    private var _binding: FragmentMedicosListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MedicosListViewModel by viewModels()
    private lateinit var medicoAdapter: MedicoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicosListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupObservers()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        medicoAdapter = MedicoAdapter { medico ->
            // Navegar al detalle del médico o a la pantalla de agendar cita
            // findNavController().navigate(MedicosListFragmentDirections.actionToMedicoDetail(medico.id))
        }
        
        binding.rvMedicos.apply {
            adapter = medicoAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupSearch() {
        // SearchBar doesn't have editText property
        // Search functionality is handled through the SearchBar's hint and click listener
        // TODO: Implement proper search functionality with SearchView if needed
    }
    
    private fun setupFilters() {
        // TODO: Add IDs to chips in layout XML and implement filter functionality
        // For now, filters are displayed but not functional
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun updateUI(state: MedicosListUiState) {
        // Mostrar/ocultar loading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        
        // Mostrar lista o estado vacío
        if (state.filteredMedicos.isEmpty() && !state.isLoading) {
            binding.rvMedicos.visibility = View.GONE
            binding.layoutEmptyState.root.visibility = View.VISIBLE
        } else {
            binding.rvMedicos.visibility = View.VISIBLE
            binding.layoutEmptyState.root.visibility = View.GONE
            medicoAdapter.submitList(state.filteredMedicos)
        }
        
        // Mostrar error si existe
        state.error?.let { error ->
            showError(error)
            viewModel.clearError()
        }
    }
    
    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
