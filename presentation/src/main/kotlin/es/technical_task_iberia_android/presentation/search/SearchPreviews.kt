package es.technical_task_iberia_android.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme

private val previewStarships = listOf(
    Starship(12, "X-wing", "T-65 X-wing", "Incom Corporation", Quantity.Known("12.5"), Quantity.Known("1")),
    Starship(11, "Y-wing", "BTL Y-wing", "Koensayr Manufacturing", Quantity.Known("14"), Quantity.Known("2")),
)

@Preview(showBackground = true)
@Composable
private fun SearchScreenInitialPreview() {
    StarshipsTheme { SearchScreen(SearchUiState(), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenResultsPreview() {
    StarshipsTheme {
        SearchScreen(SearchUiState("wing", SearchContent.Results(previewStarships)), {}, {}, {})
    }
}
