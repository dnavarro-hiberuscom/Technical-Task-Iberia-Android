package es.technical_task_iberia_android.domain.usecase

import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.repository.StarshipRepository

/**
 * Searches starships by the text the user typed.
 *
 * Owns the query rules: the text is trimmed and a blank query never reaches the repository.
 */
class SearchStarships(private val repository: StarshipRepository) {

    /** Returns the matches for [rawQuery], or an empty list without any call when it is blank. */
    suspend operator fun invoke(rawQuery: String): List<Starship> {
        val query = SearchQuery.from(rawQuery) ?: return emptyList()
        return repository.searchStarships(query)
    }
}
