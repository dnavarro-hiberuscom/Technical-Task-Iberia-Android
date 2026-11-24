package es.technical_task_iberia_android.data.repository

import es.technical_task_iberia_android.data.cache.SearchCache
import es.technical_task_iberia_android.data.network.StarshipService
import es.technical_task_iberia_android.data.network.toDomain
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipException
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * [StarshipRepository] backed by SWAPI. It owns the search cache: a valid entry avoids the
 * network; an expired or missing one is fetched and stored. Errors and cancellations are never cached.
 */
internal class StarshipRepositoryImpl(
    private val service: StarshipService,
    private val cache: SearchCache,
) : StarshipRepository {

    override suspend fun searchStarships(query: SearchQuery): List<Starship> {
        cache.get(query)?.let { return it }
        val starships = request { service.searchStarships(query.value).results.map { it.toDomain() } }
        cache.put(query, starships)
        return starships
    }

    override suspend fun getStarship(id: Int): Starship =
        request { service.getStarship(id).toDomain() }

    /**
     * Runs [block] translating failures into [StarshipException]. `CancellationException` is never
     * caught, and an `IOException` raised by a cancelled call is not reported as a failure.
     */
    private suspend fun <T> request(block: suspend () -> T): T =
        try {
            block()
        } catch (e: HttpException) {
            throw if (e.code() == HttpNotFound) StarshipException.NotFound(e) else StarshipException.Unexpected(e)
        } catch (e: IOException) {
            currentCoroutineContext().ensureActive()
            throw StarshipException.Connectivity(e)
        } catch (e: SerializationException) {
            throw StarshipException.Unexpected(e)
        }

    private companion object {
        const val HttpNotFound = 404
    }
}
