package es.technical_task_iberia_android.presentation.detail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Compose tests of the detail screen with controlled state. */
class DetailScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    private fun show(state: DetailUiState, onBack: () -> Unit = {}, onRetry: () -> Unit = {}) {
        rule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                StarshipsTheme { DetailScreen(state, onBack, onRetry) }
            }
        }
    }

    private val known = Starship(12, "X-wing", "T-65 X-wing", "Incom Corporation", Quantity.Known("12.5"), Quantity.Known("1"))

    @Test
    fun showsNameModelManufacturerLengthAndCrew() {
        show(DetailUiState.Content(known))

        rule.onNodeWithText("X-wing").assertIsDisplayed()
        rule.onNodeWithText("T-65 X-wing").assertIsDisplayed()
        rule.onNodeWithText("Incom Corporation").assertIsDisplayed()
        rule.onNodeWithText(string(R.string.detail_length_value, "12.5")).assertIsDisplayed()
        rule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun unknownValuesAreShownAsUnknownNotAsNumbers() {
        show(DetailUiState.Content(known.copy(length = Quantity.Unknown, crew = Quantity.Unknown)))

        rule.onAllNodesWithTextCount(string(R.string.detail_value_unknown)).let { assertTrue(it == 2) }
        rule.onNodeWithText(string(R.string.detail_length_value, "12.5")).assertDoesNotExist()
    }

    @Test
    fun backActionHasALocalisedNameAndWorks() {
        var backs = 0
        show(DetailUiState.Content(known), onBack = { backs++ })

        rule.onNodeWithContentDescription(string(R.string.detail_back_action)).performClick()

        assertTrue(backs == 1)
    }

    @Test
    fun connectivityErrorOffersRetry() {
        var retries = 0
        show(DetailUiState.Error(DetailError.Connectivity), onRetry = { retries++ })

        rule.onNodeWithText(string(R.string.detail_error_connectivity)).assertIsDisplayed()
        rule.onNodeWithText(string(R.string.detail_retry_action)).performClick()

        assertTrue(retries == 1)
    }

    @Test
    fun notFoundHasNoRetry() {
        show(DetailUiState.Error(DetailError.NotFound))

        rule.onNodeWithText(string(R.string.detail_error_not_found)).assertIsDisplayed()
        rule.onNodeWithText(string(R.string.detail_retry_action)).assertDoesNotExist()
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodes(androidx.compose.ui.test.hasText(text)).fetchSemanticsNodes().size
}
