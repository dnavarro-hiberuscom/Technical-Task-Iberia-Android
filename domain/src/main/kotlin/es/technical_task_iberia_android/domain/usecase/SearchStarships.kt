package es.technical_task_iberia_android.domain.usecase

import es.technical_task_iberia_android.domain.model.FirstSearchPage
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.StarshipPage
import es.technical_task_iberia_android.domain.repository.StarshipRepository

/**
 * Searches starships by the text the user typed.
 *
 * Owns the query rules: the text is trimmed and a blank query never reaches the repository.
 */
class SearchStarships(private val repository: StarshipRepository) {

    /** Returns the matches for [rawQuery], or an empty page without any call when it is blank. */
    suspend operator fun invoke(rawQuery: String, page: Int = FirstSearchPage): StarshipPage {
        val query = SearchQuery.from(rawQuery) ?: return StarshipPage(emptyList())
        return repository.searchStarships(query, page)
    }
}
