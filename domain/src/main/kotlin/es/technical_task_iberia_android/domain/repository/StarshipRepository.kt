package es.technical_task_iberia_android.domain.repository

import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship

/**
 * Source of starships. Implemented in `:data`; callers only see this contract.
 *
 * Both operations are main-safe, cooperate with cancellation and report failures as
 * [es.technical_task_iberia_android.domain.model.StarshipException].
 */
interface StarshipRepository {

    /** Starships whose name or model matches [query] (first page of the source). */
    suspend fun searchStarships(query: SearchQuery): List<Starship>

    /** The starship identified by [id]. */
    suspend fun getStarship(id: Int): Starship
}
