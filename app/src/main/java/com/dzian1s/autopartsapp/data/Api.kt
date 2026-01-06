package com.dzian1s.autopartsapp.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.dzian1s.autopartsapp.BuildConfig

interface ApiService {
    @GET("api/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: String): ProductDto

    @GET("api/search")
    suspend fun search(@Query("q") q: String): SearchResponse

    @POST("api/orders")
    suspend fun createOrder(@Body req: CreateOrderRequest): CreateOrderResponse

    @GET("api/orders/by-client/{clientId}")
    suspend fun getOrdersByClient(
        @Path("clientId") clientId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Long = 0
    ): List<OrderListItemDto>

    @GET("api/orders/{id}")
    suspend fun getOrderDetails(@Path("id") id: String): OrderDetailsDto
}

object Api {

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
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}
