package com.medical.app.ui.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.medical.app.data.entities.Consulta
import com.medical.app.data.repository.ConsultaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PagingViewModel @Inject constructor(
    private val consultaRepository: ConsultaRepository
) : ViewModel() {

    val pagingData: Flow<PagingData<Consulta>> = 
        consultaRepository.getConsultasPager().cachedIn(viewModelScope)

    // The loadData and refresh methods are handled by the Paging library's refresh mechanism,
    // which is triggered by the adapter's refresh() method.
    // We will call adapter.refresh() from the fragment.
}
