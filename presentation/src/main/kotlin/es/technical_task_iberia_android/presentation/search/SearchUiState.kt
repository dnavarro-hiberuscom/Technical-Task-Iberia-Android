package es.technical_task_iberia_android.presentation.search

import es.technical_task_iberia_android.domain.model.Starship

/** Everything the search screen shows. [query] is what the user typed, shown immediately. */
internal data class SearchUiState(
    val query: String = "",
    val content: SearchContent = SearchContent.Initial,
    val cancelledQuery: String? = null,
)

/** What the area under the search field shows. */
internal sealed interface SearchContent {

    /** Nothing typed yet. */
    data object Initial : SearchContent

    /** A search is waiting or running. Previous results are never shown here. */
    data object Loading : SearchContent

    data class Results(
        val starships: List<Starship>,
        val nextPage: Int? = null,
        val nextPageStatus: NextPageStatus = NextPageStatus.Idle,
    ) : SearchContent

    /** The search worked and nothing matched. */
    data object Empty : SearchContent

    data class Error(val reason: SearchError) : SearchContent
}

internal enum class NextPageStatus {
    Idle,
    Loading,
    Failed,
}

/** Typed failure; the screen turns it into a localised message. */
internal enum class SearchError {
    Connectivity,
    Unexpected,
}
