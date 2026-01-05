package com.dzian1s.autopartsapp.ui

import com.dzian1s.autopartsapp.data.ProductDto
import androidx.compose.runtime.mutableStateMapOf

data class CartItem(val product: ProductDto, val qty: Int)

class CartState {
    private val map = mutableStateMapOf<String, CartItem>()

    val items: List<CartItem>
        get() = map.values.toList()

    fun add(p: ProductDto) {
        val existing = map[p.id]
        map[p.id] = if (existing == null) CartItem(p, 1) else existing.copy(qty = existing.qty + 1)
    }

    fun removeOne(p: ProductDto) {
        val existing = map[p.id] ?: return
        if (existing.qty <= 1) map.remove(p.id)
        else map[p.id] = existing.copy(qty = existing.qty - 1)
    }

    fun clear() {
        map.clear()
    }

    fun totalCents(): Int = map.values.sumOf { it.product.priceCents * it.qty }
}
