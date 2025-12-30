package com.dzian1s.autopartsapp.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @GET("api/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: String): ProductDto

    @GET("api/search")
    suspend fun search(@Query("q") q: String): SearchResponse

    @POST("api/orders")
    suspend fun createOrder(@Body req: CreateOrderRequest): CreateOrderResponse
}

object Api {
    // твой IP из проверки с телефона:
    private const val BASE_URL = "http://10.178.157.56:8080/"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}
