package com.dzian1s.autopartsapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzian1s.autopartsapp.data.ProductDto
import com.dzian1s.autopartsapp.data.Repository
import kotlinx.coroutines.launch

data class DetailsState(
    val loading: Boolean = false,
    val item: ProductDto? = null,
    val error: Throwable? = null
)

class DetailsViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    var state by mutableStateOf(DetailsState())
        private set

    fun load(id: String) {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { repo.product(id) }
                .onSuccess { state = DetailsState(loading = false, item = it, error = null) }
                .onFailure { e -> state = DetailsState(loading = false, item = null, error = e) }
        }
    }
}
