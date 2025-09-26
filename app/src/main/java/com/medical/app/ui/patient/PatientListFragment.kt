package com.medical.app.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.databinding.FragmentPatientListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.paging.LoadState

@AndroidEntryPoint
class PatientListFragment : Fragment() {
    private var _binding: FragmentPatientListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientListViewModel by viewModels()
    private lateinit var adapter: PatientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadPatients()
    }

    private fun setupRecyclerView() {
        adapter = PatientAdapter { patient ->
            // Navegar al detalle del paciente
            findNavController().navigate(
                PatientListFragmentDirections.actionPatientListToPatientDetail(patient.id)
            )
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            this.adapter = this@PatientListFragment.adapter
            
            // Agregar divisor entre elementos
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                )
            )
        }

    private fun setupClickListeners() {
        binding.fabAddPatient.setOnClickListener {
            findNavController().navigate(
                PatientListFragmentDirections.actionPatientListToAddPatient()
            )
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            // Recargar los datos desde el principio
            adapter.refresh()
        }
    }

    private fun observeViewModel() {
        // Observar el estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { message ->
                showError(message)
                // Limpiar el error después de mostrarlo
                viewModel.onErrorShown()
            }
        }

        // Observar los datos paginados
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.patients.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        // Observar cuando no hay datos
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                val isEmpty = loadState.append.endOfPaginationReached && 
                    (adapter.itemCount == 0 || loadState.append is LoadState.Error)
                
                binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
                
                // Mostrar error si hay un problema al cargar más datos
                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                
                errorState?.let {
                    showError(it.error.localizedMessage ?: getString(R.string.error_loading_patients))
                }
            }
        }
    }

    private fun loadPatients() {
        // La carga inicial se maneja automáticamente por el PagingDataAdapter
        // Este método se mantiene por compatibilidad pero ya no es necesario
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
