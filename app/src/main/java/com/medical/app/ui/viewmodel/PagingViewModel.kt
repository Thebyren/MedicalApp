package com.medical.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.medical.app.data.remote.dto.YourDataModel
import com.medical.app.domain.repository.PagingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PagingViewModel @Inject constructor(
    private val repository: PagingRepository
) : ViewModel() {

    private val _pagingData = MutableStateFlow<PagingData<YourDataModel>>(PagingData.empty())
    val pagingData: StateFlow<PagingData<YourDataModel>> = _pagingData

    private var currentQuery: String? = null
    
    init {
        loadData()
    }

    /**
     * Carga los datos iniciales
     */
    fun loadData(query: String? = null) {
        // Si la consulta es la misma, no hacemos nada
        if (query == currentQuery) return
        
        currentQuery = query
        
        viewModelScope.launch {
            repository.getPagingData(query)
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _pagingData.value = pagingData
                }
        }
    }
    
    /**
     * Actualiza los datos actuales
     */
    fun refresh() {
        loadData(currentQuery)
    }
}
