package com.medical.app.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Interfaz base para los repositorios de la aplicación.
 * Define operaciones CRUD básicas que pueden ser compartidas entre repositorios.
 */
interface BaseRepository<T, ID> {
    suspend fun insert(entity: T): Long
    suspend fun update(entity: T): Int
    suspend fun delete(entity: T): Int
    suspend fun getById(id: ID): T?
    fun getAll(): Flow<List<T>>
}
