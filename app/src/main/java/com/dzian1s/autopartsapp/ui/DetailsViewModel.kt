package com.dzian1s.autopartsapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.dzian1s.autopartsapp.data.ProductDto
import com.dzian1s.autopartsapp.data.Repository

data class DetailsState(
    val loading: Boolean = false,
    val item: ProductDto? = null,
    val error: String? = null
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
                .onSuccess { state = DetailsState(loading = false, item = it) }
                .onFailure { state = DetailsState(loading = false, item = null, error = it.message) }
        }
    }
}
