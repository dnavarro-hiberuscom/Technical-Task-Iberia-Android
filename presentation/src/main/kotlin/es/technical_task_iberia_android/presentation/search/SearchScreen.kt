package es.technical_task_iberia_android.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme
import org.koin.androidx.compose.koinViewModel

/**
 * Entry screen of the app. `:app` only passes a callback that receives the id of the selected starship.
 */
@Composable
fun SearchRoute(
    onStarshipClick: (id: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchRoute(
        viewModel = koinViewModel<SearchViewModel>(),
        onStarshipClick = onStarshipClick,
        modifier = modifier,
    )
}

/** Same screen with an explicit ViewModel, so tests can provide one built with fakes. */
@Composable
internal fun SearchRoute(
    viewModel: SearchViewModel,
    onStarshipClick: (id: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::onClearQuery,
        onStarshipClick = onStarshipClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onStarshipClick: (id: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.search_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = StarshipsTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(StarshipsTheme.spacing.small),
        ) {
            SearchField(
                query = state.query,
                onQueryChange = onQueryChange,
                onClearQuery = onClearQuery,
            )
            SearchContentArea(
                query = state.query,
                content = state.content,
                onStarshipClick = onStarshipClick,
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.search_field_label)) },
        supportingText = { Text(stringResource(R.string.search_field_supporting)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                // IconButton already provides the minimum touch target.
                IconButton(onClick = onClearQuery) {
                    Icon(
                        painter = painterResource(R.drawable.ic_clear),
                        contentDescription = stringResource(R.string.search_clear_action),
                        modifier = Modifier.size(StarshipsTheme.sizes.icon),
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun SearchContentArea(
    query: String,
    content: SearchContent,
    onStarshipClick: (id: Int) -> Unit,
) {
    when (content) {
        SearchContent.Initial -> CenteredMessage(stringResource(R.string.search_initial_message))
        SearchContent.Loading -> LoadingIndicator()
        SearchContent.Empty -> CenteredMessage(stringResource(R.string.search_empty_message, query.trim()))
        is SearchContent.Error -> CenteredMessage(
            text = stringResource(content.reason.messageRes()),
            color = MaterialTheme.colorScheme.error,
        )
        is SearchContent.Results -> ResultsList(content.starships, onStarshipClick)
    }
}

@Composable
private fun CenteredMessage(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(StarshipsTheme.spacing.large),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(StarshipsTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(StarshipsTheme.spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(StarshipsTheme.sizes.progressIndicator))
        Text(
            text = stringResource(R.string.search_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultsList(
    starships: List<Starship>,
    onStarshipClick: (id: Int) -> Unit,
) {
    Column {
        Text(
            text = pluralStringResource(R.plurals.search_results_count, starships.size, starships.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = StarshipsTheme.spacing.small),
        )
        LazyColumn {
            items(items = starships, key = { it.id }) { starship ->
                // One focusable, clickable element per row: ListItem content is merged by clickable.
                ListItem(
                    headlineContent = { Text(starship.name) },
                    supportingContent = {
                        Text(stringResource(R.string.search_result_summary, starship.model, starship.manufacturer))
                    },
                    modifier = Modifier.clickable { onStarshipClick(starship.id) },
                )
                HorizontalDivider()
            }
        }
    }
}

private fun SearchError.messageRes(): Int = when (this) {
    SearchError.Connectivity -> R.string.search_error_connectivity
    SearchError.Unexpected -> R.string.search_error_unexpected
}

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
