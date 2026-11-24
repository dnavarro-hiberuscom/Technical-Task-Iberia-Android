package es.technical_task_iberia_android.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.StarshipException
import es.technical_task_iberia_android.domain.usecase.SearchStarships
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State holder of the search screen.
 *
 * Typed texts form a Flow collected with `collectLatest`: a new text cancels the previous block at
 * once. The debounce wait lives inside that block, so an in-flight request is cancelled as soon as
 * the text changes, not 300 ms later.
 */
internal class SearchViewModel(
    private val searchStarships: SearchStarships,
) : ViewModel() {

    private val typedText = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            typedText.collectLatest { text -> search(text) }
        }
    }

    /** The user edited the field: show the text now, drop old results and start a new search. */
    fun onQueryChange(text: String) {
        _uiState.update {
            SearchUiState(
                query = text,
                content = if (SearchQuery.from(text) == null) SearchContent.Initial else SearchContent.Loading,
            )
        }
        typedText.value = text
    }

    fun onClearQuery() = onQueryChange("")

    private suspend fun search(text: String) {
        if (SearchQuery.from(text) == null) return // collectLatest already cancelled the previous work

        delay(SearchDebounce)
        val content = try {
            val starships = searchStarships(text)
            if (starships.isEmpty()) SearchContent.Empty else SearchContent.Results(starships)
        } catch (e: CancellationException) {
            throw e
        } catch (e: StarshipException) {
            SearchContent.Error(e.toSearchError())
        }
        // A newer text may have cancelled this block while the answer was arriving.
        currentCoroutineContext().ensureActive()
        _uiState.update { it.copy(content = content) }
    }

    private fun StarshipException.toSearchError(): SearchError = when (this) {
        is StarshipException.Connectivity -> SearchError.Connectivity
        is StarshipException.NotFound, is StarshipException.Unexpected -> SearchError.Unexpected
    }
}
