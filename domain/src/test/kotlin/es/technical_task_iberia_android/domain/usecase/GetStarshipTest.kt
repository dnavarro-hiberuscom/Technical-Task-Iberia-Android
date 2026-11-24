package es.technical_task_iberia_android.domain.usecase

import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipException
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class GetStarshipTest {

    private val repository = mockk<StarshipRepository>()
    private val getStarship = GetStarship(repository)

    private val xWing = Starship(
        id = 12,
        name = "X-wing",
        model = "T-65 X-wing",
        manufacturer = "Incom Corporation",
        length = Quantity.Known("12.5"),
        crew = Quantity.Unknown,
    )

    @Test
    fun `returns the starship of the repository`() = runTest {
        coEvery { repository.getStarship(12) } returns xWing

        assertEquals(xWing, getStarship(12))
        coVerify(exactly = 1) { repository.getStarship(12) }
    }

    @Test
    fun `not found reaches the caller`() = runTest {
        coEvery { repository.getStarship(999) } throws StarshipException.NotFound()

        try {
            getStarship(999)
            fail("Expected StarshipException.NotFound")
        } catch (expected: StarshipException.NotFound) {
            // expected
        }
    }
}
