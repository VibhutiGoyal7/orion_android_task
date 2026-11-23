package com.vibhuti.orion.di

import com.vibhuti.orion.BuildConfig
import com.vibhuti.orion.data.remote.ApiService
import com.vibhuti.orion.data.repository.ItemRepository
import com.vibhuti.orion.data.repository.ItemRepositoryImpl
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Manual DI: one place that builds the object graph. Swap for Hilt if the task grows. */
class AppContainer(baseUrl: String = "https://jsonplaceholder.typicode.com/") {

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ApiService::class.java)

    val itemRepository: ItemRepository = ItemRepositoryImpl(api)
}
