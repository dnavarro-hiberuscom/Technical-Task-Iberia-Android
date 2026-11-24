package es.technical_task_iberia_android.presentation.search

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.ArchiveInk
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme

@Composable
internal fun SearchPrompt(modifier: Modifier = Modifier) {
    val spacing = StarshipsTheme.spacing
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(horizontal = spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Box(
            modifier = Modifier.size(StarshipsTheme.sizes.insignia)
                .background(colors.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_rebel_crest),
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(StarshipsTheme.sizes.icon),
            )
        }
        Text(
            text = stringResource(R.string.search_prompt),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private const val LoadingRotationDurationMillis = 1_800
private const val FullRotationDegrees = 360f

@Composable
internal fun SearchLoading(text: String, modifier: Modifier = Modifier) {
    val rotation by rememberInfiniteTransition(label = "Search loading").animateFloat(
        initialValue = 0f,
        targetValue = FullRotationDegrees,
        animationSpec = infiniteRepeatable(tween(LoadingRotationDurationMillis, easing = LinearEasing)),
        label = "Emblem rotation",
    )
    Column(
        modifier = modifier
            .padding(horizontal = StarshipsTheme.spacing.large)
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StarshipsTheme.spacing.small),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_rebel_crest),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(StarshipsTheme.sizes.progressIndicator)
                .graphicsLayer { rotationZ = rotation },
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun NoSearchResults(query: String, modifier: Modifier = Modifier) {
    val spacing = StarshipsTheme.spacing
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.large, vertical = spacing.medium)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        Image(
            painter = painterResource(R.drawable.blueprint_locator),
            contentDescription = null,
            modifier = Modifier.size(StarshipsTheme.sizes.insignia)
                .clip(CircleShape)
                .background(ArchiveInk),
        )
        Text(
            text = stringResource(R.string.search_empty_message, query),
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.search_empty_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun SearchMessage(text: String, error: Boolean = false) {
    val spacing = StarshipsTheme.spacing
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth().padding(
            start = spacing.large,
            top = spacing.large,
            end = spacing.large,
            bottom = spacing.small,
        ),
        style = MaterialTheme.typography.bodyLarge,
        color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
