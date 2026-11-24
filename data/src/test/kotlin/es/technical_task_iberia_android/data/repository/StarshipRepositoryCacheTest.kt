package es.technical_task_iberia_android.data.repository

import es.technical_task_iberia_android.data.cache.SearchCache
import es.technical_task_iberia_android.data.network.StarshipPageDto
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.StarshipException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class StarshipRepositoryCacheTest {

    private val service = FakeStarshipService()
    private val clock = TestTimeSource()

    // Uses the default lifetime, so these tests also pin it to three minutes.
    private val repository = StarshipRepositoryImpl(service, SearchCache(timeSource = clock))

    private fun query(text: String) = SearchQuery.from(text)!!

    @Before
    fun setUp() {
        service.responses["wing"] = listOf(starshipDto("X-wing", 12), starshipDto("Y-wing", 11))
        service.responses["falcon"] = listOf(starshipDto("Millennium Falcon", 10))
    }

    @Test
    fun `equivalent queries share one network call`() = runTest {
        val first = repository.searchStarships(query("wing"))
        val second = repository.searchStarships(query("  WING "))
        val third = repository.searchStarships(query("Wing"))

        assertEquals(1, service.searchCalls.size)
        assertEquals(first, second)
        assertEquals(first, third)
    }

    @Test
    fun `each page is cached separately with its continuation and expiry`() = runTest {
        service.nextLinks["wing"] = "https://swapi.dev/api/starships/?search=wing&page=2"
        service.laterPages["wing" to 2] = StarshipPageDto(listOf(starshipDto("A-wing", 28)))

        val first = repository.searchStarships(query("wing"))
        clock += 1.minutes
        val second = repository.searchStarships(query("wing"), page = 2)
        clock += 2.minutes

        assertEquals(2, first.nextPage)
        assertEquals(listOf(28), second.starships.map { it.id })
        assertEquals(null, second.nextPage)
        assertEquals(second, repository.searchStarships(query(" WING "), page = 2))
        assertEquals(first, repository.searchStarships(query("wing")))
        assertEquals(listOf(1, 2, 1), service.pageCalls)
    }

    @Test
    fun `different queries have their own entry`() = runTest {
        repository.searchStarships(query("wing"))
        repository.searchStarships(query("falcon"))
        repository.searchStarships(query("wing"))
        repository.searchStarships(query("falcon"))

        assertEquals(listOf("wing", "falcon"), service.searchCalls)
    }

    @Test
    fun `entry is still valid just before three minutes`() = runTest {
        repository.searchStarships(query("wing"))

        clock += 3.minutes - 1.seconds
        repository.searchStarships(query("wing"))

        assertEquals(1, service.searchCalls.size)
    }

    @Test
    fun `entry expires exactly at three minutes and is refreshed`() = runTest {
        repository.searchStarships(query("wing"))

        clock += 3.minutes
        service.responses["wing"] = listOf(starshipDto("A-wing", 28))
        val refreshed = repository.searchStarships(query("wing"))

        assertEquals(2, service.searchCalls.size)
        assertEquals(listOf("A-wing"), refreshed.starships.map { it.name })
    }

    @Test
    fun `refreshed value is reused and its lifetime starts again`() = runTest {
        repository.searchStarships(query("wing"))
        clock += 3.minutes
        repository.searchStarships(query("wing")) // refresh at t = 3 min

        clock += 3.minutes - 1.seconds // t = 5 min 59 s, still inside the new lifetime
        repository.searchStarships(query("wing"))
        assertEquals(2, service.searchCalls.size)

        clock += 1.seconds // new entry is exactly three minutes old
        repository.searchStarships(query("wing"))
        assertEquals(3, service.searchCalls.size)
    }

    @Test
    fun `empty results are cached`() = runTest {
        repository.searchStarships(query("zzzz"))
        val again = repository.searchStarships(query("zzzz"))

        assertTrue(again.starships.isEmpty())
        assertEquals(1, service.searchCalls.size)
    }

    @Test
    fun `failure is not cached and the next search retries the network`() = runTest {
        service.failure = IOException("offline")
        try {
            repository.searchStarships(query("wing"))
            fail("Expected a connectivity failure")
        } catch (expected: StarshipException.Connectivity) {
            // expected
        }

        service.failure = null
        val result = repository.searchStarships(query("wing"))

        assertEquals(2, service.searchCalls.size)
        assertEquals(2, result.starships.size)
    }

    @Test
    fun `cancellation is rethrown and not cached`() = runTest {
        service.failure = CancellationException("cancelled")
        try {
            repository.searchStarships(query("wing"))
            fail("Expected a CancellationException")
        } catch (expected: CancellationException) {
            // expected: never wrapped in a StarshipException
        }

        service.failure = null
        repository.searchStarships(query("wing"))

        assertEquals(2, service.searchCalls.size)
    }
}
