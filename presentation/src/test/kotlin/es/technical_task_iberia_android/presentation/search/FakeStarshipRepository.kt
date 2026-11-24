package es.technical_task_iberia_android.presentation.search

import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipPage
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred

/**
 * Repository double whose searches stay pending until the test completes them, and which records
 * which searches started and which were cancelled.
 */
internal class FakeStarshipRepository : StarshipRepository {

    val started = mutableListOf<String>()
    val cancelled = mutableListOf<String>()
    private val pending = mutableMapOf<Pair<String, Int>, CompletableDeferred<StarshipPage>>()

    val requestedPages = mutableListOf<Pair<String, Int>>()
    val cancelledPages = mutableListOf<Pair<String, Int>>()

    override suspend fun searchStarships(query: SearchQuery, page: Int): StarshipPage {
        started += query.value
        val key = query.value to page
        requestedPages += key
        val answer = CompletableDeferred<StarshipPage>().also { pending[key] = it }
        try {
            return answer.await()
        } catch (e: CancellationException) {
            cancelled += query.value
            cancelledPages += key
            throw e
        }
    }

    override suspend fun getStarship(id: Int): Starship = error("Not used by the search tests")

    fun complete(query: String, result: List<Starship>, page: Int = 1, nextPage: Int? = null) {
        pending.getValue(query to page).complete(StarshipPage(result, nextPage))
    }

    fun fail(query: String, error: Throwable, page: Int = 1) {
        pending.getValue(query to page).completeExceptionally(error)
    }
}

internal fun starship(id: Int, name: String) = Starship(
    id = id,
    name = name,
    model = "Model $name",
    manufacturer = "Maker $name",
    length = Quantity.Known("10"),
    crew = Quantity.Unknown,
)
