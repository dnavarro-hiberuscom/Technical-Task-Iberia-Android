package es.technical_task_iberia_android.presentation.search

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertTextContains
import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipPage
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import es.technical_task_iberia_android.domain.usecase.SearchStarshipsUseCase
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Compose tests with controlled data: no network involved. */
class SearchScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private class StubRepository : StarshipRepository {
        val xWing = Starship(12, "X-wing", "T-65 X-wing", "Incom Corporation", Quantity.Known("12.5"), Quantity.Known("1"))
        val yWing = Starship(11, "Y-wing", "BTL Y-wing", "Koensayr Manufacturing", Quantity.Known("14"), Quantity.Known("2"))

        override suspend fun searchStarships(query: SearchQuery, page: Int): StarshipPage =
            StarshipPage(if (query.value == "wing") listOf(xWing, yWing) else emptyList())

        override suspend fun getStarship(id: Int): Starship = error("Not used")
    }

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    private fun launch(repository: StarshipRepository = StubRepository(), onClick: (Int) -> Unit = {}) {
        val viewModel = SearchViewModel(SearchStarshipsUseCase(repository))
        rule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                StarshipsTheme {
                    SearchRoute(viewModel = viewModel, onStarshipClick = onClick)
                }
            }
        }
    }

    private fun typeWing() {
        rule.onNode(hasSetTextAction()).performTextInput("wing")
        rule.waitUntil(timeoutMillis = ResultsTimeoutMillis) {
            rule.onAllNodesWithTextCount("X-wing") > 0
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodes(hasText(text)).fetchSemanticsNodes().size

    private fun assertOnlyPromptBelowSearchField() {
        val otherText = SemanticsMatcher.keyIsDefined(SemanticsProperties.Text) and
            !hasSetTextAction() and !hasText(string(R.string.search_title))
        rule.onAllNodes(otherText).assertCountEquals(1)
        rule.onNodeWithText(string(R.string.search_prompt)).assertIsDisplayed()
    }

    @Test
    fun initialStateHasAnEmptyFieldAndNoCatalogueEntries() {
        launch()

        assertOnlyPromptBelowSearchField()
        rule.onNode(hasSetTextAction()).assertTextEquals(string(R.string.search_field_label), "")
        rule.onNodeWithContentDescription(string(R.string.search_clear_action)).assertDoesNotExist()
    }

    @Test
    fun searchFieldCanBeFoundByItsSemanticsAndCleared() {
        launch()
        typeWing()

        rule.onNode(hasSetTextAction()).assertTextContains("wing")
        rule.onNodeWithContentDescription(string(R.string.search_clear_action)).performClick()

        rule.onNode(hasSetTextAction()).assertTextEquals(string(R.string.search_field_label), "")
        assertOnlyPromptBelowSearchField()
        rule.onNodeWithText("X-wing").assertDoesNotExist()
    }

    @Test
    fun resultsAreListedAndARowCanBeSelected() {
        var clickedId: Int? = null
        launch { clickedId = it }
        typeWing()

        rule.onNodeWithText(string(R.string.search_prompt)).assertDoesNotExist()
        rule.onNodeWithText("Y-wing").assertIsDisplayed()
        rule.onNodeWithText(string(R.string.search_result_summary, "T-65 X-wing", "Incom Corporation"), substring = true)
            .assertIsDisplayed()

        rule.onNodeWithText("X-wing").performClick()

        assertEquals(12, clickedId)
    }

    @Test
    fun loadingMoreAppendsTheNextPageAndRemovesTheButtonOnTheLastPage() {
        val stub = StubRepository()
        val repository = object : StarshipRepository by stub {
            override suspend fun searchStarships(query: SearchQuery, page: Int): StarshipPage =
                when (page) {
                    1 -> StarshipPage(listOf(stub.xWing), nextPage = 2)
                    2 -> StarshipPage(listOf(stub.yWing))
                    else -> error("Unexpected page: $page")
                }
        }
        launch(repository)
        typeWing()

        val list = rule.onNode(hasScrollToIndexAction())
        val loadMore = rule.onNodeWithText(string(R.string.search_load_more))
        list.performScrollToIndex(2)
        loadMore.assertIsDisplayed().performClick()

        rule.waitUntil(ResultsTimeoutMillis) { rule.onAllNodesWithTextCount("Y-wing") > 0 }
        rule.onNodeWithText("Y-wing").assertIsDisplayed()
        loadMore.assertDoesNotExist()
        list.performScrollToIndex(1)
        rule.onNodeWithText("X-wing").assertIsDisplayed()
    }

    @Test
    fun replacingAnActiveSearchDisplaysTheCancelledQueryAndOnlyTheNewResults() {
        val stub = StubRepository()
        val started = CompletableDeferred<Unit>()
        val repository = object : StarshipRepository by stub {
            override suspend fun searchStarships(query: SearchQuery, page: Int): StarshipPage {
                if (query.value == "wi") {
                    started.complete(Unit)
                    awaitCancellation()
                }
                return stub.searchStarships(query, page)
            }
        }
        launch(repository)
        rule.onNode(hasSetTextAction()).performTextInput("wi")
        rule.waitUntil(ResultsTimeoutMillis) { started.isCompleted }
        rule.onNodeWithText(string(R.string.search_loading, "wi")).assertIsDisplayed()

        rule.onNode(hasSetTextAction()).performTextReplacement("wing")
        rule.waitUntil(ResultsTimeoutMillis) { rule.onAllNodesWithTextCount("X-wing") > 0 }
        rule.onNode(hasSetTextAction()).assertTextContains(string(R.string.search_cancelled, "wi"))

        rule.onNodeWithContentDescription(string(R.string.search_clear_action)).performClick()
        rule.onNode(hasSetTextAction()).assertTextEquals(string(R.string.search_field_label), "")
        assertOnlyPromptBelowSearchField()
    }

    @Test
    fun noMatchesShowsTheEmptyMessageWithTheQuery() {
        launch()
        rule.onNode(hasSetTextAction()).performTextInput("zzzz")

        val message = string(R.string.search_empty_message, "zzzz")
        rule.waitUntil(timeoutMillis = ResultsTimeoutMillis) {
            rule.onAllNodesWithTextCount(message) > 0
        }
        rule.onNodeWithText(message).assertIsDisplayed()
        rule.onNodeWithText(string(R.string.search_empty_hint)).assertIsDisplayed()
    }

    private companion object {
        /** Generous upper bound for the debounce plus the stub answer on a slow emulator. */
        const val ResultsTimeoutMillis = 5_000L
    }
}
