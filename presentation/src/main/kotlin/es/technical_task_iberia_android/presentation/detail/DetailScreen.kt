package es.technical_task_iberia_android.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/** Detail of the starship [starshipId]. `:app` only passes the id and a back callback. */
@Composable
fun DetailRoute(
    starshipId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailRoute(
        viewModel = koinViewModel<DetailViewModel>(parameters = { parametersOf(starshipId) }),
        onBack = onBack,
        modifier = modifier,
    )
}

/** Same screen with an explicit ViewModel, so tests can provide one built with fakes. */
@Composable
internal fun DetailRoute(
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(state = state, onBack = onBack, onRetry = viewModel::onRetry, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detail_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    // IconButton already provides the minimum touch target.
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.detail_back_action),
                            modifier = Modifier.size(StarshipsTheme.sizes.icon),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (state) {
                DetailUiState.Loading -> LoadingContent()
                is DetailUiState.Content -> StarshipDetails(state.starship)
                is DetailUiState.Error -> ErrorContent(state.reason, onRetry)
            }
        }
    }
}

@Composable
private fun StarshipDetails(starship: Starship) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(StarshipsTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(StarshipsTheme.spacing.medium),
    ) {
        Text(
            text = starship.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        DetailField(stringResource(R.string.detail_label_model), starship.model)
        DetailField(stringResource(R.string.detail_label_manufacturer), starship.manufacturer)
        DetailField(
            label = stringResource(R.string.detail_label_length),
            value = when (val length = starship.length) {
                is Quantity.Known -> stringResource(R.string.detail_length_value, length.value)
                Quantity.Unknown -> stringResource(R.string.detail_value_unknown)
            },
        )
        DetailField(
            label = stringResource(R.string.detail_label_crew),
            value = when (val crew = starship.crew) {
                is Quantity.Known -> crew.value
                Quantity.Unknown -> stringResource(R.string.detail_value_unknown)
            },
        )
    }
}

/** Label and value read together as one item ("Model, T-65 X-wing"). */
@Composable
private fun DetailField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(StarshipsTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(StarshipsTheme.spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(StarshipsTheme.sizes.progressIndicator))
        Text(
            text = stringResource(R.string.detail_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorContent(reason: DetailError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(StarshipsTheme.spacing.large),
        verticalArrangement = Arrangement.spacedBy(StarshipsTheme.spacing.medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(reason.messageRes()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        if (reason != DetailError.NotFound) {
            Button(onClick = onRetry) { Text(stringResource(R.string.detail_retry_action)) }
        }
    }
}

private fun DetailError.messageRes(): Int = when (this) {
    DetailError.Connectivity -> R.string.detail_error_connectivity
    DetailError.NotFound -> R.string.detail_error_not_found
    DetailError.Unexpected -> R.string.detail_error_unexpected
}

private val previewStarship = Starship(
    id = 12,
    name = "X-wing",
    model = "T-65 X-wing",
    manufacturer = "Incom Corporation",
    length = Quantity.Known("12.5"),
    crew = Quantity.Unknown,
)

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    StarshipsTheme { DetailScreen(DetailUiState.Content(previewStarship), {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenErrorPreview() {
    StarshipsTheme { DetailScreen(DetailUiState.Error(DetailError.Connectivity), {}, {}) }
}
