package es.technical_task_iberia_android.presentation.detail

import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipException
import es.technical_task_iberia_android.domain.model.StarshipPage
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import es.technical_task_iberia_android.domain.usecase.GetStarshipUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val xWing = Starship(12, "X-wing", "T-65 X-wing", "Incom Corporation", Quantity.Known("12.5"), Quantity.Unknown)

    private class FakeRepository : StarshipRepository {
        var calls = 0
        var failure: StarshipException? = null
        lateinit var starship: Starship

        override suspend fun searchStarships(query: SearchQuery, page: Int): StarshipPage = error("Not used")

        override suspend fun getStarship(id: Int): Starship {
            calls++
            failure?.let { throw it }
            return starship
        }
    }

    private val repository = FakeRepository().also { it.starship = xWing }

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = DetailViewModel(12, GetStarshipUseCase(repository))

    @Test
    fun `starts loading and then shows the starship`() = runTest(dispatcher) {
        val viewModel = viewModel()
        assertEquals(DetailUiState.Loading, viewModel.uiState.value)

        runCurrent()

        assertEquals(DetailUiState.Content(xWing), viewModel.uiState.value)
    }

    @Test
    fun `not found is a typed error`() = runTest(dispatcher) {
        repository.failure = StarshipException.NotFound()
        val viewModel = viewModel()
        runCurrent()

        assertEquals(DetailUiState.Error(DetailError.NotFound), viewModel.uiState.value)
    }

    @Test
    fun `connectivity error can be retried`() = runTest(dispatcher) {
        repository.failure = StarshipException.Connectivity()
        val viewModel = viewModel()
        runCurrent()
        assertEquals(DetailUiState.Error(DetailError.Connectivity), viewModel.uiState.value)

        repository.failure = null
        viewModel.onRetry()
        assertEquals(DetailUiState.Loading, viewModel.uiState.value)
        runCurrent()

        assertEquals(DetailUiState.Content(xWing), viewModel.uiState.value)
        assertEquals(2, repository.calls)
    }

    @Test
    fun `unexpected failure is a typed error`() = runTest(dispatcher) {
        repository.failure = StarshipException.Unexpected()
        val viewModel = viewModel()
        runCurrent()

        assertEquals(DetailUiState.Error(DetailError.Unexpected), viewModel.uiState.value)
    }
}
