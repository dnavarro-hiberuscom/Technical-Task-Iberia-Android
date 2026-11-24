package es.technical_task_iberia_android.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.technical_task_iberia_android.domain.model.Quantity
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.ArchiveHeroControl
import es.technical_task_iberia_android.presentation.designsystem.ArchiveHeroScrim
import es.technical_task_iberia_android.presentation.designsystem.ArchiveInk
import es.technical_task_iberia_android.presentation.designsystem.ArchiveOrange
import es.technical_task_iberia_android.presentation.designsystem.ArchivePaper
import es.technical_task_iberia_android.presentation.designsystem.ArchivePoster
import es.technical_task_iberia_android.presentation.designsystem.StarshipImage
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
    val hasContent = state is DetailUiState.Content
    val safeInsets = WindowInsets.safeDrawing
    Scaffold(
        modifier = modifier,
        containerColor = ArchiveInk,
        contentWindowInsets = if (hasContent) safeInsets.exclude(WindowInsets.statusBars) else safeInsets,
        topBar = {
            if (!hasContent) DetailTopBar(onBack)
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (state) {
                DetailUiState.Loading -> LoadingContent()
                is DetailUiState.Content -> StarshipDetails(state.starship, onBack)
                is DetailUiState.Error -> ErrorContent(state.reason, onRetry)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.detail_title),
                modifier = Modifier.semantics { heading() },
            )
        },
        navigationIcon = { DetailBackButton(onBack) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ArchiveInk,
            titleContentColor = ArchivePaper,
        ),
    )
}

@Composable
private fun DetailBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onBack, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.detail_back_action),
            tint = ArchivePaper,
            modifier = Modifier.size(StarshipsTheme.sizes.icon),
        )
    }
}

@Composable
private fun StarshipDetails(starship: Starship, onBack: () -> Unit) {
    val spacing = StarshipsTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ArchiveInk)
            .verticalScroll(rememberScrollState())
            .padding(bottom = spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DetailHero(starship, onBack)
        Surface(
            modifier = Modifier.widthIn(max = StarshipsTheme.sizes.detailContentMaxWidth).fillMaxWidth()
                .padding(horizontal = spacing.medium, vertical = spacing.large),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            BoxWithConstraints(Modifier.padding(horizontal = spacing.medium)) {
                val stackFields = maxWidth / LocalDensity.current.fontScale < StarshipsTheme.sizes.inlineDetailMinWidth
                Column {
                    DetailField(
                        label = stringResource(R.string.detail_label_manufacturer),
                        value = starship.manufacturer,
                        stacked = stackFields,
                    )
                    DetailField(
                        label = stringResource(R.string.detail_label_length),
                        value = when (val length = starship.length) {
                            is Quantity.Known -> stringResource(R.string.detail_length_value, length.value)
                            Quantity.Unknown -> stringResource(R.string.detail_value_unknown)
                        },
                        stacked = stackFields,
                    )
                    DetailField(
                        label = stringResource(R.string.detail_label_crew),
                        value = when (val crew = starship.crew) {
                            is Quantity.Known -> crew.value
                            Quantity.Unknown -> stringResource(R.string.detail_value_unknown)
                        },
                        stacked = stackFields,
                        showDivider = false,
                    )
                }
            }
        }
    }
}

private const val DetailImageAspectRatio = 0.88f
private const val ToolbarScrimFadeStart = 0.45f
private val TransparentPoster = ArchivePoster.copy(alpha = 0f)

@Composable
private fun DetailHero(starship: Starship, onBack: () -> Unit) {
    val sizes = StarshipsTheme.sizes
    Column(Modifier.fillMaxWidth().background(ArchivePoster)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val imageHeight = minOf(maxWidth / DetailImageAspectRatio, sizes.maxDetailImageHeight)
            Box(Modifier.fillMaxWidth()) {
                StarshipImage(starshipId = starship.id, modifier = Modifier.matchParentSize())
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = imageHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    DetailHeroToolbar(onBack)
                    DetailHeroTitle(starship)
                }
            }
        }
        Spacer(Modifier.fillMaxWidth().height(StarshipsTheme.spacing.extraSmall).background(ArchiveOrange))
    }
}

@Composable
private fun DetailHeroToolbar(onBack: () -> Unit) {
    val spacing = StarshipsTheme.spacing
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to ArchivePoster,
                    ToolbarScrimFadeStart to ArchiveHeroScrim,
                    1f to TransparentPoster,
                ),
            )
            .statusBarsPadding()
            .padding(start = spacing.medium, bottom = spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DetailBackButton(
            onBack = onBack,
            modifier = Modifier.padding(top = spacing.small).background(ArchiveHeroControl, CircleShape),
        )
        Icon(
            painter = painterResource(R.drawable.ic_archive_stripe),
            contentDescription = null,
            tint = ArchiveOrange,
            modifier = Modifier.size(StarshipsTheme.sizes.insignia),
        )
    }
}

@Composable
private fun DetailHeroTitle(starship: Starship) {
    val spacing = StarshipsTheme.spacing
    val sizes = StarshipsTheme.sizes
    val modelLabel = stringResource(R.string.detail_label_model)

    Column(Modifier.fillMaxWidth()) {
        Spacer(
            Modifier.fillMaxWidth().height(sizes.detailTitleFadeHeight)
                .background(Brush.verticalGradient(listOf(TransparentPoster, ArchiveHeroScrim))),
        )
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(ArchiveHeroScrim, ArchivePoster)))
                .padding(start = spacing.large, end = spacing.large, bottom = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            Spacer(Modifier.width(sizes.detailTitleAccentWidth).height(spacing.extraSmall).background(ArchiveOrange))
            Text(
                text = starship.name,
                style = MaterialTheme.typography.displayLarge,
                color = ArchivePaper,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = starship.model,
                style = MaterialTheme.typography.titleLarge,
                color = ArchivePaper,
                modifier = Modifier.semantics { contentDescription = modelLabel },
            )
        }
    }
}

/** Label and value are exposed together for each specification. */
@Composable
private fun DetailField(
    label: String,
    value: String,
    stacked: Boolean,
    showDivider: Boolean = true,
) {
    val spacing = StarshipsTheme.spacing
    val fieldModifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {}
        .padding(vertical = spacing.medium)

    Column {
        if (stacked) {
            Column(fieldModifier, verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Row(
                modifier = fieldModifier,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
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
