package es.technical_task_iberia_android.presentation.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.StarshipImage
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme

@Composable
internal fun SearchResults(
    results: SearchContent.Results,
    onLoadMore: () -> Unit,
    onStarshipClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = StarshipsTheme.spacing
    val sizes = StarshipsTheme.sizes
    val fontScale = LocalDensity.current.fontScale
    val starships = results.starships
    BoxWithConstraints(modifier.widthIn(max = sizes.searchContentMaxWidth).fillMaxSize()) {
        val stackCards = maxWidth / fontScale < sizes.inlineSearchMinWidth
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = spacing.medium,
                end = spacing.medium,
                top = spacing.medium,
                bottom = spacing.large,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            item {
                val heading = if (results.nextPage != null) {
                    pluralStringResource(R.plurals.search_results_loaded, starships.size, starships.size)
                } else {
                    pluralStringResource(R.plurals.search_results_count, starships.size, starships.size)
                }
                CatalogueHeading(heading)
            }
            items(starships, key = { it.id }) { starship ->
                StarshipRow(starship, stackCards) { onStarshipClick(starship.id) }
            }
            if (results.nextPage != null) {
                item(key = "next-page") {
                    NextPageFooter(results.nextPageStatus, onLoadMore)
                }
            }
        }
    }
}

@Composable
private fun NextPageFooter(status: NextPageStatus, onLoadMore: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = StarshipsTheme.spacing.small)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StarshipsTheme.spacing.extraSmall),
    ) {
        if (status == NextPageStatus.Failed) {
            Text(
                text = stringResource(R.string.search_next_page_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Button(onClick = onLoadMore, enabled = status != NextPageStatus.Loading) {
            Text(
                stringResource(
                    when (status) {
                        NextPageStatus.Idle -> R.string.search_load_more
                        NextPageStatus.Loading -> R.string.search_loading_more
                        NextPageStatus.Failed -> R.string.search_retry_page
                    },
                ),
            )
        }
    }
}

@Composable
private fun CatalogueHeading(text: String) {
    val spacing = StarshipsTheme.spacing
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = spacing.small, bottom = spacing.extraSmall)
            .semantics { heading() },
    )
}

@Composable
private fun StarshipRow(starship: Starship, stacked: Boolean, onClick: () -> Unit) {
    val spacing = StarshipsTheme.spacing
    val sizes = StarshipsTheme.sizes
    val colors = MaterialTheme.colorScheme
    val thumbnail = Modifier.clip(MaterialTheme.shapes.small)
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainerLow,
        border = BorderStroke(sizes.outlineStroke, colors.outlineVariant),
    ) {
        if (stacked) {
            Column(Modifier.padding(spacing.small), verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                StarshipImage(starship.id, Modifier.fillMaxWidth().height(sizes.starshipThumbnailHeight).then(thumbnail))
                StarshipRowText(starship)
            }
        } else {
            Row(
                modifier = Modifier.padding(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                StarshipImage(
                    starship.id,
                    Modifier.size(sizes.starshipThumbnailWidth, sizes.starshipThumbnailHeight).then(thumbnail),
                )
                StarshipRowText(starship, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StarshipRowText(starship: Starship, modifier: Modifier = Modifier) {
    val spacing = StarshipsTheme.spacing
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        ) {
            Text(starship.name, style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.search_result_summary, starship.model, starship.manufacturer),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
        }
        Icon(
            painterResource(R.drawable.ic_forward),
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(StarshipsTheme.sizes.icon),
        )
    }
}
