package es.technical_task_iberia_android.presentation.search

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertTextContains
import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import es.technical_task_iberia_android.domain.usecase.SearchStarships
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme
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

        override suspend fun searchStarships(query: SearchQuery): List<Starship> =
            if (query.value == "wing") listOf(xWing, yWing) else emptyList()

        override suspend fun getStarship(id: Int): Starship = error("Not used")
    }

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    private fun launch(onClick: (Int) -> Unit = {}) {
        val viewModel = SearchViewModel(SearchStarships(StubRepository()))
        rule.setContent {
            StarshipsTheme {
                SearchRoute(viewModel = viewModel, onStarshipClick = onClick)
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

    @Test
    fun initialStateShowsTheHintAndAnEmptyField() {
        launch()

        rule.onNodeWithText(string(R.string.search_initial_message)).assertIsDisplayed()
        rule.onNode(hasSetTextAction()).assertTextEquals(string(R.string.search_field_label), string(R.string.search_field_supporting), "")
        rule.onNodeWithContentDescription(string(R.string.search_clear_action)).assertDoesNotExist()
    }

    @Test
    fun searchFieldCanBeFoundByItsSemanticsAndCleared() {
        launch()
        typeWing()

        rule.onNode(hasSetTextAction()).assertTextContains("wing")
        rule.onNodeWithContentDescription(string(R.string.search_clear_action)).performClick()

        rule.onNode(hasSetTextAction()).assertTextEquals(string(R.string.search_field_label), string(R.string.search_field_supporting), "")
        rule.onNodeWithText(string(R.string.search_initial_message)).assertIsDisplayed()
        rule.onNodeWithText("X-wing").assertDoesNotExist()
    }

    @Test
    fun resultsAreListedAndARowCanBeSelected() {
        var clickedId: Int? = null
        launch { clickedId = it }
        typeWing()

        rule.onNodeWithText("Y-wing").assertIsDisplayed()
        rule.onNodeWithText(string(R.string.search_result_summary, "T-65 X-wing", "Incom Corporation"), substring = true)
            .assertIsDisplayed()

        rule.onNodeWithText("X-wing").performClick()

        assertEquals(12, clickedId)
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
    }

    private companion object {
        /** Generous upper bound for the debounce plus the stub answer on a slow emulator. */
        const val ResultsTimeoutMillis = 5_000L
    }
}
