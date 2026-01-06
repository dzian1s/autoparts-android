package com.dzian1s.autopartsapp.ui

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzian1s.autopartsapp.data.ProductDto
import com.dzian1s.autopartsapp.data.Repository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchState(
    val query: String = "",
    val loading: Boolean = false,
    val mode: String? = null,
    val items: List<ProductDto> = emptyList(),
    val error: Throwable? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repo: Repository = Repository()
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val retryTick = MutableStateFlow(0)

    var state by mutableStateOf(SearchState())
        private set

    init {
        viewModelScope.launch {
            val debouncedQuery = queryFlow
                .debounce(350)
                .distinctUntilChanged()

            combine(debouncedQuery, retryTick) { q, _ -> q }
                .collectLatest { q ->
                    state = state.copy(query = q)

                    val trimmed = q.trim()
                    if (trimmed.isEmpty()) {
                        state = state.copy(loading = false, mode = null, items = emptyList(), error = null)
                        return@collectLatest
                    }

                    state = state.copy(loading = true, error = null)

                    runCatching { repo.search(trimmed) }
                        .onSuccess { res ->
                            state = state.copy(
                                loading = false,
                                mode = res.mode,
                                items = res.items,
                                error = null
                            )
                        }
                        .onFailure { e ->
                            state = state.copy(
                                loading = false,
                                mode = null,
                                items = emptyList(),
                                error = e
                            )
                        }
                }
        }
    }

    fun onQueryChange(q: String) {
        state = state.copy(query = q)
        queryFlow.value = q
    }

    fun retry() {
        retryTick.value = retryTick.value + 1
    }
}