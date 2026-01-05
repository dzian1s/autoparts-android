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

// ВАЖНО: это ожидаемый формат ответа поиска.
// Если твой backend сейчас возвращает иначе — скажи, я подстрою под фактический JSON.
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