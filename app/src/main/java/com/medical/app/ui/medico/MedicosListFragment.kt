package com.medical.app.ui.medico

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.data.model.Medico
import com.medical.app.databinding.FragmentMedicosListBinding
import com.medical.app.ui.medico.MedicosListViewModel.MedicosListUiState
import dagger.hilt.android.AndroidEntryPoint

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
        binding.searchBar.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s?.toString() ?: "")
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun setupFilters() {
        // Configurar los chips de filtro
        val chips = listOf(
            binding.root.findViewById<Chip>(R.id.chip_todos),
            binding.root.findViewById<Chip>(R.id.chip_cardiologos),
            binding.root.findViewById<Chip>(R.id.chip_dermatologos),
            binding.root.findViewById<Chip>(R.id.chip_pediatras)
        )
        
        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Actualizar filtro cuando se selecciona un chip
                    val especialidad = when (chip.id) {
                        R.id.chip_cardiologos -> "Cardiología"
                        R.id.chip_dermatologos -> "Dermatología"
                        R.id.chip_pediatras -> "Pediatría"
                        else -> null
                    }
                    // viewModel.filterBySpecialty(especialidad)
                }
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            updateUI(state)
        }
        
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            // Actualizar UI de búsqueda si es necesario
        }
    }
    
    private fun updateUI(state: MedicosListUiState) {
        // Mostrar/ocultar loading
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        
        // Mostrar lista o estado vacío
        if (state.filteredMedicos.isEmpty() && !state.isLoading) {
            binding.rvMedicos.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvMedicos.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
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
