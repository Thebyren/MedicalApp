package com.medical.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medical.app.databinding.FragmentYourListBinding
import com.medical.app.ui.adapter.PagingAdapter
import com.medical.app.ui.viewmodel.PagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class YourListFragment : Fragment() {

    private var _binding: FragmentYourListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PagingViewModel by viewModels()
    private lateinit var adapter: PagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYourListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
        
        // Cargar datos iniciales
        viewModel.loadData()
    }
    
    private fun setupRecyclerView() {
        adapter = PagingAdapter()
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            this.adapter = this@YourListFragment.adapter
            
            // Agregar espacio entre elementos
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    context,
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pagingData.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        
        // Opcional: Mostrar/ocultar indicador de carga
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadStates ->
                binding.swipeRefreshLayout.isRefreshing = 
                    loadStates.refresh is androidx.paging.LoadState.Loading
                
                // Manejar estados de error si es necesario
                val errorState = loadStates.refresh as? androidx.paging.LoadState.Error
                errorState?.let {
                    // Mostrar mensaje de error
                }
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
