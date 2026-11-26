package es.technical_task_iberia_android.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.StarshipException
import es.technical_task_iberia_android.domain.usecase.SearchStarshipsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * State holder of the search screen.
 *
 * Typed texts form a Flow collected with `collectLatest`: a new text cancels the previous block at
 * once. The debounce wait lives inside that block, so an in-flight request is cancelled as soon as
 * the text changes, not 300 ms later.
 */
internal class SearchViewModel(
    private val searchStarshipsUseCase: SearchStarshipsUseCase,
) : ViewModel() {

    // Separate edits can have the same text (type, clear, retype before the collector resumes).
    private class QueryEdit(val text: String)

    private var nextPageJob: Job? = null
    private val typedText = MutableStateFlow(QueryEdit(""))
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            typedText.collectLatest { edit -> search(edit) }
        }
    }

    /** The user edited the field: show the text now, drop old results and start a new search. */
    fun onQueryChange(text: String) {
        if (text == typedText.value.text) return
        nextPageJob?.cancel()
        val hasQuery = SearchQuery.from(text) != null
        _uiState.update { previous ->
            SearchUiState(
                query = text,
                content = if (hasQuery) SearchContent.Loading else SearchContent.Initial,
                cancelledQuery = previous.cancelledQuery.takeIf { hasQuery && previous.content == SearchContent.Loading },
            )
        }
        typedText.value = QueryEdit(text)
    }

    fun onClearQuery() = onQueryChange("")

    fun onLoadMore() {
        val edit = typedText.value
        val results = _uiState.value.content as? SearchContent.Results ?: return
        val nextPage = results.nextPage ?: return
        if (results.nextPageStatus == NextPageStatus.Loading) return

        _uiState.update { it.copy(content = results.copy(nextPageStatus = NextPageStatus.Loading)) }
        nextPageJob = viewModelScope.launch {
            val content = try {
                val page = searchStarshipsUseCase(edit.text, nextPage)
                SearchContent.Results(
                    starships = (results.starships + page.starships).distinctBy { it.id },
                    nextPage = page.nextPage,
                )
            } catch (_: StarshipException) {
                results.copy(nextPageStatus = NextPageStatus.Failed)
            }
            publishContent(edit, content)
        }
    }

    private suspend fun search(edit: QueryEdit) {
        val text = edit.text
        if (SearchQuery.from(text) == null) return // collectLatest already cancelled the previous work

        delay(SearchDebounce)
        val content = try {
            val page = searchStarshipsUseCase(text)
            if (page.starships.isEmpty() && page.nextPage == null) {
                SearchContent.Empty
            } else {
                SearchContent.Results(page.starships.distinctBy { it.id }, page.nextPage)
            }
        } catch (e: CancellationException) {
            // The debounce sits outside this try. Only a search that actually started can report cancellation.
            if (!currentCoroutineContext().isActive && viewModelScope.isActive && typedText.value !== edit) {
                _uiState.update { current ->
                    if (current.content == SearchContent.Loading) current.copy(cancelledQuery = text.trim()) else current
                }
            }
            throw e
        } catch (e: StarshipException) {
            SearchContent.Error(e.toSearchError())
        }
        publishContent(edit, content)
    }

    private suspend fun publishContent(edit: QueryEdit, content: SearchContent) {
        // A newer text may have cancelled this block while the answer was arriving.
        currentCoroutineContext().ensureActive()
        _uiState.update { current ->
            if (typedText.value === edit) current.copy(content = content) else current
        }
    }

    private fun StarshipException.toSearchError(): SearchError = when (this) {
        is StarshipException.Connectivity -> SearchError.Connectivity
        is StarshipException.NotFound, is StarshipException.Unexpected -> SearchError.Unexpected
    }
}
