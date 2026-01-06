package com.dzian1s.autopartsapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("partNumber") val partNumber: String,
    @SerialName("oemNumber") val oemNumber: String,
    @SerialName("priceCents") val priceCents: Int,
    @SerialName("isActive") val isActive: Boolean,
)

@Serializable
data class SearchResponse(
    val mode: String,
    val items: List<ProductDto>
)

@Serializable
data class CreateOrderItemDto(val productId: String, val qty: Int)

@Serializable
data class CreateOrderRequest(
    val clientId: String,
    val customerName: String,
    val customerPhone: String,
    val customerComment: String? = null,
    val items: List<CreateOrderItemDto>
)

@Serializable
data class CreateOrderResponse(val orderId: String)

@Serializable
data class OrderListItemDto(
    val id: String,
    val createdAt: String,
    val status: String,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val itemsCount: Int = 0,
    val totalCents: Int = 0
)

@Serializable
data class OrderItemDto(
    val productId: String,
    val name: String,
    val qty: Int,
    val priceCents: Int
)

@Serializable
data class OrderDetailsDto(
    val id: String,
    val createdAt: String,
    val status: String,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val customerComment: String? = null,
    val items: List<OrderItemDto>
)