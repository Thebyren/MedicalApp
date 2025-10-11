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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConsultaListFragment : Fragment() {

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
    }
    
    private fun setupRecyclerView() {
        adapter = PagingAdapter()
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            this.adapter = this@ConsultaListFragment.adapter
            
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
        
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collect { loadStates ->
                binding.swipeRefreshLayout.isRefreshing = 
                    loadStates.refresh is androidx.paging.LoadState.Loading
                
                val errorState = loadStates.refresh as? androidx.paging.LoadState.Error
                errorState?.let {
                    // You can show a toast or a snackbar with the error message here
                }
            }
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
