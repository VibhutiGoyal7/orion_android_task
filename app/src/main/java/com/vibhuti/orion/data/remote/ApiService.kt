package com.vibhuti.orion.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class ItemDto(val id: Int, val title: String, val body: String = "")

interface ApiService {
    @GET("posts")
    suspend fun getItems(): List<ItemDto>
}
