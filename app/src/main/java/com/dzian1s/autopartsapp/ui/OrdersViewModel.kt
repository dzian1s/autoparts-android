package com.dzian1s.autopartsapp.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzian1s.autopartsapp.data.OrderListItemDto
import com.dzian1s.autopartsapp.data.Repository
import kotlinx.coroutines.launch

data class OrdersState(
    val loading: Boolean = false,
    val items: List<OrderListItemDto> = emptyList(),
    val error: Throwable? = null
)

class OrdersViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    var state by mutableStateOf(OrdersState())
        private set

    fun load(clientId: String) {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { repo.ordersByClient(clientId) }
                .onSuccess { state = OrdersState(loading = false, items = it, error = null) }
                .onFailure { e -> state = OrdersState(loading = false, items = emptyList(), error = e) }
        }
    }
}
