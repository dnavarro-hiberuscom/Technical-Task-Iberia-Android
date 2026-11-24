package es.technical_task_iberia_android

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.model.StarshipPage
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import es.technical_task_iberia_android.navigation.StarshipsNavHost
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

/**
 * Full flow on the real root graph and the real Koin modules, with only the repository replaced
 * by controlled data: search, open a row, check the detail, go back to the original query.
 */
class SearchToDetailFlowTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val xWing = Starship(12, "X-wing", "T-65 X-wing", "Incom Corporation", Quantity.Known("12.5"), Quantity.Known("1"))
    private val yWing = Starship(11, "Y-wing", "BTL Y-wing", "Koensayr Manufacturing", Quantity.Known("14"), Quantity.Known("2"))

    private val controlledData = module {
        single<StarshipRepository> {
            object : StarshipRepository {
                override suspend fun searchStarships(query: SearchQuery, page: Int) =
                    StarshipPage(if (query.value == "wing") listOf(xWing, yWing) else emptyList())

                override suspend fun getStarship(id: Int) = listOf(xWing, yWing).first { it.id == id }
            }
        }
    }

    @Before
    fun overrideRepository() = loadKoinModules(controlledData)

    @After
    fun restoreRepository() = unloadKoinModules(controlledData)

    private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

    private fun waitForText(text: String) = rule.waitUntil(timeoutMillis = TimeoutMillis) {
        rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun searchOpenDetailAndGoBackKeepsTheQuery() {
        rule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                StarshipsTheme { StarshipsNavHost() }
            }
        }

        rule.onNode(hasSetTextAction()).performTextInput("wing")
        waitForText("X-wing")

        rule.onNodeWithText("X-wing").performClick()

        waitForText(string(R.string.detail_label_manufacturer))
        rule.onNodeWithText("T-65 X-wing").assertIsDisplayed()
        rule.onNodeWithText("Incom Corporation").assertIsDisplayed()
        rule.onNodeWithText(string(R.string.detail_length_value, "12.5")).assertIsDisplayed()
        rule.onNodeWithText("1").assertIsDisplayed()

        rule.onNodeWithContentDescription(string(R.string.detail_back_action)).performClick()

        rule.onNode(hasSetTextAction()).assertTextContains("wing")
        rule.onNodeWithText("Y-wing").assertIsDisplayed()
        rule.onNodeWithText("X-wing").assertIsDisplayed()
    }

    private companion object {
        const val TimeoutMillis = 5_000L
    }
}
