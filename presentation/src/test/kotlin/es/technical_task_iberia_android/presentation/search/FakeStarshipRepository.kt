package es.technical_task_iberia_android.presentation.search

import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
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
    private val pending = mutableMapOf<String, CompletableDeferred<List<Starship>>>()

    override suspend fun searchStarships(query: SearchQuery): List<Starship> {
        started += query.value
        val answer = CompletableDeferred<List<Starship>>().also { pending[query.value] = it }
        try {
            return answer.await()
        } catch (e: CancellationException) {
            cancelled += query.value
            throw e
        }
    }

    override suspend fun getStarship(id: Int): Starship = error("Not used by the search tests")

    fun complete(query: String, result: List<Starship>) {
        pending.getValue(query).complete(result)
    }

    fun fail(query: String, error: Throwable) {
        pending.getValue(query).completeExceptionally(error)
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
