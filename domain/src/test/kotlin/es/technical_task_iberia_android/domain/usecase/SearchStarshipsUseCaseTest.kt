package es.technical_task_iberia_android.domain.usecase

import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipException
import es.technical_task_iberia_android.domain.model.StarshipPage
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SearchStarshipsUseCaseTest {

    private val repository = mockk<StarshipRepository>()
    private val searchStarshipsUseCase = SearchStarshipsUseCase(repository)

    private val xWing = Starship(
        id = 12,
        name = "X-wing",
        model = "T-65 X-wing",
        manufacturer = "Incom Corporation",
        length = Quantity.Known("12.5"),
        crew = Quantity.Known("1"),
    )

    @Test
    fun `blank query returns an empty list without calling the repository`() = runTest {
        listOf("", " ", "   \t\n").forEach { blank ->
            assertTrue(searchStarshipsUseCase(blank).starships.isEmpty())
        }

        coVerify(exactly = 0) { repository.searchStarships(any(), any()) }
    }

    @Test
    fun `query is trimmed before reaching the repository`() = runTest {
        coEvery { repository.searchStarships(any(), any()) } returns StarshipPage(listOf(xWing))

        searchStarshipsUseCase("  wing \n")

        coVerify(exactly = 1) { repository.searchStarships(SearchQuery.from("wing")!!, 1) }
    }

    @Test
    fun `returns what the repository returns`() = runTest {
        coEvery { repository.searchStarships(any(), any()) } returns StarshipPage(listOf(xWing))

        assertEquals(listOf(xWing), searchStarshipsUseCase("wing").starships)
    }

    @Test
    fun `returns an empty list when nothing matches`() = runTest {
        coEvery { repository.searchStarships(any(), any()) } returns StarshipPage(emptyList())

        assertTrue(searchStarshipsUseCase("zzzz").starships.isEmpty())
    }

    @Test
    fun `repository failures reach the caller`() = runTest {
        coEvery { repository.searchStarships(any(), any()) } throws StarshipException.Connectivity()

        try {
            searchStarshipsUseCase("wing")
            fail("Expected a StarshipException")
        } catch (_: StarshipException.Connectivity) {
            // expected
        }
    }
}
