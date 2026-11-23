package com.vibhuti.orion.data.repository

import com.vibhuti.orion.data.remote.ApiService
import com.vibhuti.orion.data.remote.ItemDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Item(val id: Int, val title: String, val body: String)

interface ItemRepository {
    suspend fun getItems(): Result<List<Item>>
}

class ItemRepositoryImpl(
    private val api: ApiService,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ItemRepository {
    override suspend fun getItems(): Result<List<Item>> = withContext(io) {
        runCatching { api.getItems().map { it.toDomain() } }
    }
}

private fun ItemDto.toDomain() = Item(id = id, title = title, body = body)
