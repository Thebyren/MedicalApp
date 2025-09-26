package com.medical.app.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.medical.app.data.paging.YourPagingSource
import com.medical.app.data.remote.ApiService
import com.medical.app.data.remote.dto.YourDataModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PagingRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * Obtiene los datos paginados
     * @param query Término de búsqueda opcional
     * @param pageSize Tamaño de página (por defecto 20)
     */
    fun getPagingData(query: String? = null, pageSize: Int = 20): Flow<PagingData<YourDataModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                initialLoadSize = pageSize * 2
            ),
            pagingSourceFactory = { YourPagingSource(apiService, query) }
        ).flow
    }
}
