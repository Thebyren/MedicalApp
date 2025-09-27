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
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.databinding.FragmentPatientListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    }

    private fun setupRecyclerView() {
        adapter = PatientAdapter { patient ->
            findNavController().navigate(
                PatientListFragmentDirections.actionPatientListToPatientDetailFragment(patient.id.toString())
            )
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            this.adapter = this@PatientListFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddPatient.setOnClickListener {
            findNavController().navigate(
                PatientListFragmentDirections.actionPatientListToAddPatientFragment()
            )
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.patients.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadState ->
                // Muestra la barra de progreso solo en la carga inicial.
                val isInitialLoading = loadState.refresh is LoadState.Loading
                binding.progressBar.isVisible = isInitialLoading && adapter.itemCount == 0

                // El SwipeRefreshLayout se encarga de mostrar su propio indicador al deslizar.
                // Solo necesitamos ocultarlo cuando la operación de refresco termina.
                if (loadState.refresh !is LoadState.Loading) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                // Muestra la vista de "vacío" si la carga ha terminado y no hay elementos.
                binding.emptyState.root.isVisible = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0

                // Captura y muestra errores de cualquier fuente (inicial, agregar, preceder).
                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                    ?: loadState.refresh as? LoadState.Error

                errorState?.let {
                    showError(it.error.localizedMessage ?: getString(R.string.error_loading_patients))
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
