package com.medical.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.medical.app.data.remote.ApiService
import com.medical.app.data.remote.dto.YourDataModel
import retrofit2.HttpException
import java.io.IOException

/**
 * Fuente de datos paginada para cargar datos de forma eficiente
 * @param apiService Servicio de API para obtener los datos
 * @param query Parámetros de búsqueda (opcional)
 */
class YourPagingSource(
    private val apiService: ApiService,
    private val query: String? = null
) : PagingSource<Int, YourDataModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, YourDataModel> {
        return try {
            // Empezar desde la página 1 si es la primera carga
            val page = params.key ?: 1
            val pageSize = params.loadSize
            
            // Llamada a la API con los parámetros de paginación
            val response = if (query.isNullOrEmpty()) {
                apiService.getItems(page, pageSize)
            } else {
                apiService.searchItems(query, page, pageSize)
            }
            
            // Calcular las claves de paginación
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (response.items.isNotEmpty()) page + 1 else null
            
            LoadResult.Page(
                data = response.items,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            // Error de red
            LoadResult.Error(e)
        } catch (e: HttpException) {
            // Error HTTP
            LoadResult.Error(e)
        } catch (e: Exception) {
            // Otros errores
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, YourDataModel>): Int? {
        // Para mantener la posición actual al actualizar
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
