package es.technical_task_iberia_android.presentation.search

import es.technical_task_iberia_android.domain.model.StarshipException
import es.technical_task_iberia_android.domain.usecase.SearchStarshipsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeStarshipRepository()
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = SearchViewModel(SearchStarshipsUseCase(repository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val state get() = viewModel.uiState.value

    /** Types [text] and lets the debounce pass so the search starts. */
    private fun TestScope.type(text: String) {
        viewModel.onQueryChange(text)
        advanceTimeBy(SearchDebounce)
        runCurrent()
    }

    @Test
    fun `loading another page keeps results and retries the same page after a failure`() = runTest(dispatcher) {
        val first = starship(12, "X-wing")
        val second = starship(11, "Y-wing")
        type("wing")
        repository.complete("wing", listOf(first), nextPage = 2)
        runCurrent()

        viewModel.onLoadMore()
        viewModel.onLoadMore()
        runCurrent()
        assertEquals(SearchContent.Results(listOf(first), 2, NextPageStatus.Loading), state.content)
        assertEquals(listOf("wing" to 1, "wing" to 2), repository.requestedPages)

        repository.fail("wing", StarshipException.Connectivity(), page = 2)
        runCurrent()
        assertEquals(SearchContent.Results(listOf(first), 2, NextPageStatus.Failed), state.content)

        viewModel.onLoadMore()
        runCurrent()
        repository.complete("wing", listOf(first, second), page = 2)
        runCurrent()
        viewModel.onLoadMore()
        runCurrent()

        assertEquals(SearchContent.Results(listOf(first, second)), state.content)
        assertEquals(listOf("wing" to 1, "wing" to 2, "wing" to 2), repository.requestedPages)
    }

    @Test
    fun `a new query cancels the next page before its own debounce completes`() = runTest(dispatcher) {
        type("wing")
        repository.complete("wing", listOf(starship(12, "X-wing")), nextPage = 2)
        runCurrent()
        viewModel.onLoadMore()
        runCurrent()

        viewModel.onQueryChange("falcon")
        runCurrent()

        assertEquals(listOf("wing" to 2), repository.cancelledPages)
        assertEquals(listOf("wing" to 1, "wing" to 2), repository.requestedPages)
        assertEquals(SearchContent.Loading, state.content)

        repository.complete("wing", listOf(starship(11, "Y-wing")), page = 2)
        advanceTimeBy(SearchDebounce)
        runCurrent()
        val falcon = starship(10, "Millennium Falcon")
        repository.complete("falcon", listOf(falcon))
        runCurrent()

        assertEquals(SearchContent.Results(listOf(falcon)), state.content)
    }

    @Test
    fun `starts empty`() = runTest(dispatcher) {
        runCurrent()

        assertEquals(SearchUiState(), state)
    }

    @Test
    fun `typed text is shown immediately and old content is replaced by loading`() = runTest(dispatcher) {
        viewModel.onQueryChange("wi")

        assertEquals("wi", state.query)
        assertEquals(SearchContent.Loading, state.content)
        assertTrue(repository.started.isEmpty())
    }

    @Test
    fun `search starts only after the debounce`() = runTest(dispatcher) {
        viewModel.onQueryChange("wing")
        advanceTimeBy(SearchDebounce - 1.milliseconds)
        runCurrent()
        assertTrue(repository.started.isEmpty())

        advanceTimeBy(1.milliseconds)
        runCurrent()
        assertEquals(listOf("wing"), repository.started)
    }

    @Test
    fun `quick typing starts a single search for the last text`() = runTest(dispatcher) {
        "falcon".indices.forEach { i ->
            viewModel.onQueryChange("falcon".take(i + 1))
            advanceTimeBy(100.milliseconds)
        }
        advanceTimeBy(SearchDebounce)
        runCurrent()

        assertEquals(listOf("falcon"), repository.started)
        assertNull(state.cancelledQuery)
    }

    @Test
    fun `results of the search are shown`() = runTest(dispatcher) {
        type("wing")
        repository.complete("wing", listOf(starship(12, "X-wing")))
        runCurrent()

        assertEquals(SearchContent.Results(listOf(starship(12, "X-wing"))), state.content)
    }

    @Test
    fun `no matches shows the empty state`() = runTest(dispatcher) {
        type("zzzz")
        repository.complete("zzzz", emptyList())
        runCurrent()

        assertEquals(SearchContent.Empty, state.content)
    }

    @Test
    fun `new text cancels the in-flight search before the new debounce elapses`() = runTest(dispatcher) {
        type("wing")
        assertEquals(listOf("wing"), repository.started) // "wing" is in flight

        viewModel.onQueryChange("falcon")
        runCurrent() // no virtual time has passed since typing "falcon"

        // Regression guard: with debounce placed before collectLatest this would still be empty
        // until the 300 ms of "falcon" had elapsed.
        assertEquals(listOf("wing"), repository.cancelled)
        assertEquals("wing", state.cancelledQuery)
        assertEquals(listOf("wing"), repository.started)
    }

    @Test
    fun `clearing and immediately retyping the same query replaces the pending search`() = runTest(dispatcher) {
        type("wing")
        viewModel.onClearQuery()
        viewModel.onQueryChange("wing")
        runCurrent()
        assertEquals(listOf("wing"), repository.cancelled)
        advanceTimeBy(SearchDebounce)
        runCurrent()
        assertEquals(listOf("wing", "wing"), repository.started)
    }

    @Test
    fun `editing after a completed search does not report a cancellation`() = runTest(dispatcher) {
        type("wing")
        repository.complete("wing", emptyList())
        runCurrent()
        viewModel.onQueryChange("falcon")
        runCurrent()
        assertNull(state.cancelledQuery)
    }

    @Test
    fun `no state after a new text shows stale results or errors`() = runTest(dispatcher) {
        val seen = mutableListOf<SearchUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.toList(seen) }

        type("wing")
        viewModel.onQueryChange("falcon")
        runCurrent()
        // Late answers of the cancelled search must be ignored.
        runCatching { repository.complete("wing", listOf(starship(12, "X-wing"))) }
        runCatching { repository.fail("wing", StarshipException.Connectivity()) }
        advanceTimeBy(SearchDebounce)
        runCurrent()
        repository.complete("falcon", listOf(starship(10, "Millennium Falcon")))
        runCurrent()

        val afterFalcon = seen.dropWhile { it.query != "falcon" }
        assertTrue(afterFalcon.none { it.content is SearchContent.Error })
        assertTrue(afterFalcon.none { (it.content as? SearchContent.Results)?.starships?.any { s -> s.name == "X-wing" } == true })
        assertEquals(SearchContent.Results(listOf(starship(10, "Millennium Falcon"))), state.content)
    }

    @Test
    fun `clearing the field cancels the search and goes back to the initial state`() = runTest(dispatcher) {
        type("wing")

        viewModel.onClearQuery()
        runCurrent()

        assertEquals(listOf("wing"), repository.cancelled)
        assertEquals(SearchUiState(), state)

        // Nothing comes back variant, not even after debounce.
        advanceTimeBy(SearchDebounce * 2)
        runCurrent()
        assertEquals(SearchUiState(), state)
    }

    @Test
    fun `clearing during the debounce prevents the search`() = runTest(dispatcher) {
        viewModel.onQueryChange("wing")
        advanceTimeBy(SearchDebounce - 1.milliseconds)
        viewModel.onClearQuery()
        advanceTimeBy(SearchDebounce * 2)
        runCurrent()

        assertTrue(repository.started.isEmpty())
        assertEquals(SearchUiState(), state)
    }

    @Test
    fun `blank text goes back to the initial state without searching`() = runTest(dispatcher) {
        viewModel.onQueryChange("   ")
        advanceTimeBy(SearchDebounce * 2)
        runCurrent()

        assertTrue(repository.started.isEmpty())
        assertEquals(SearchContent.Initial, state.content)
        assertEquals("   ", state.query)
    }

    @Test
    fun `connectivity failure is shown as a typed error and a new search recovers`() = runTest(dispatcher) {
        type("wing")
        repository.fail("wing", StarshipException.Connectivity())
        runCurrent()
        assertEquals(SearchContent.Error(SearchError.Connectivity), state.content)

        type("falcon")
        repository.complete("falcon", listOf(starship(10, "Millennium Falcon")))
        runCurrent()
        assertTrue(state.content is SearchContent.Results)
    }

    @Test
    fun `unexpected failure is shown as a typed error`() = runTest(dispatcher) {
        type("wing")
        repository.fail("wing", StarshipException.Unexpected())
        runCurrent()

        assertEquals(SearchContent.Error(SearchError.Unexpected), state.content)
    }
}
