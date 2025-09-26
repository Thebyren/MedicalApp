package com.medical.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.medical.app.data.local.dao.PatientDao
import com.medical.app.data.model.Patient
import java.io.IOException
import javax.inject.Inject

/**
 * Fuente de datos para la paginación de pacientes.
 * Se encarga de cargar las páginas de datos según sea necesario.
 */
class PatientPagingSource(
    private val patientDao: PatientDao,
    private val query: String = ""
) : PagingSource<Int, Patient>() {

    companion object {
        private const val INITIAL_PAGE_INDEX = 0
    }

    override fun getRefreshKey(state: PagingState<Int, Patient>): Int? {
        // Obtenemos la página más reciente accedida
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Patient> {
        // Si la página es nula, iniciamos desde la primera página
        val page = params.key ?: INITIAL_PAGE_INDEX
        val pageSize = params.loadSize

        return try {
            // Obtenemos los pacientes de la base de datos
            val patients = if (query.isBlank()) {
                patientDao.getPatientsPaged(
                    offset = page * pageSize,
                    limit = pageSize
                )
            } else {
                patientDao.searchPatientsPaged(
                    query = "%$query%",
                    offset = page * pageSize,
                    limit = pageSize
                )
            }

            // Si la respuesta está vacía y no es la primera página, devolvemos un error
            if (patients.isEmpty() && page > INITIAL_PAGE_INDEX) {
                return LoadResult.Error(IOException("No hay más datos disponibles"))
            }

            // Calculamos las claves de paginación
            val prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1
            val nextKey = if (patients.isEmpty()) null else page + 1

            // Devolvemos el resultado exitoso con los datos y las claves de paginación
            LoadResult.Page(
                data = patients,
                prevKey = prevKey,
                nextKey = nextKey,
                itemsBefore = if (page == 0) 0 else page * pageSize,
                itemsAfter = if (patients.size < pageSize) 0 else Int.MAX_VALUE
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
