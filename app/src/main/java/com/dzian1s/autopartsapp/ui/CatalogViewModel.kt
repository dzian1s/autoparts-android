package com.dzian1s.autopartsapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzian1s.autopartsapp.data.ProductDto
import com.dzian1s.autopartsapp.data.Repository
import kotlinx.coroutines.launch

data class CatalogState(
    val loading: Boolean = false,
    val items: List<ProductDto> = emptyList(),
    val error: Throwable? = null
)

class CatalogViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    var state by mutableStateOf(CatalogState())
        private set

    fun load() {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { repo.products() }
                .onSuccess { state = CatalogState(loading = false, items = it, error = null) }
                .onFailure { e -> state = CatalogState(loading = false, items = emptyList(), error = e) }
        }
    }
}
