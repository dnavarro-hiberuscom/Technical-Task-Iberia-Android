package es.technical_task_iberia_android.domain.repository

import es.technical_task_iberia_android.domain.model.FirstSearchPage
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipPage

/**
 * Source of starships. Implemented in `:data`; callers only see this contract.
 *
 * Both operations are main-safe, cooperate with cancellation and report failures as
 * [es.technical_task_iberia_android.domain.model.StarshipException].
 */
interface StarshipRepository {

    /** Starships whose name or model matches [query], from the requested [page]. */
    suspend fun searchStarships(query: SearchQuery, page: Int = FirstSearchPage): StarshipPage

    /** The starship identified by [id]. */
    suspend fun getStarship(id: Int): Starship
}
