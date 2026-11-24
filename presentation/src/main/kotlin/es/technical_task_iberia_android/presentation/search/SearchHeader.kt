package es.technical_task_iberia_android.presentation.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.ArchiveHeroControl
import es.technical_task_iberia_android.presentation.designsystem.ArchiveOrange
import es.technical_task_iberia_android.presentation.designsystem.ArchivePaper
import es.technical_task_iberia_android.presentation.designsystem.ArchivePoster
import es.technical_task_iberia_android.presentation.designsystem.StarshipsTheme
import es.technical_task_iberia_android.presentation.designsystem.archiveSearchFieldColors

private const val HangarViewportFraction = 0.42f
private const val HangarTitleFadeEnd = 0.44f
private const val HangarFloorFadeStart = 0.81f

@Composable
internal fun HangarHeader() {
    val spacing = StarshipsTheme.spacing
    val sizes = StarshipsTheme.sizes
    val transparentPoster = ArchivePoster.copy(alpha = 0f)

    BoxWithConstraints(Modifier.fillMaxWidth().background(ArchivePoster)) {
        val artHeight = minOf(sizes.hangarArt, maxHeight * HangarViewportFraction)
        Box(Modifier.fillMaxWidth().heightIn(min = artHeight)) {
            Image(
                painter = painterResource(R.drawable.rebel_hangar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(
                        0f to ArchivePoster,
                        HangarTitleFadeEnd to transparentPoster,
                        HangarFloorFadeStart to transparentPoster,
                        1f to ArchivePoster,
                    ),
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = spacing.large, end = spacing.large, top = spacing.large),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Text(
                    text = stringResource(R.string.search_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = ArchivePaper,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                Box(
                    modifier = Modifier.size(sizes.insignia).background(ArchiveHeroControl, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_rebel_crest),
                        contentDescription = null,
                        tint = ArchiveOrange,
                        modifier = Modifier.size(sizes.icon + spacing.small),
                    )
                }
            }
        }
    }
}

@Composable
internal fun SearchField(
    query: String,
    cancelledQuery: String?,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
) {
    val spacing = StarshipsTheme.spacing
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier.fillMaxWidth().background(ArchivePoster),
        contentAlignment = Alignment.Center,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.widthIn(max = StarshipsTheme.sizes.searchContentMaxWidth).fillMaxWidth()
                .padding(start = spacing.medium, end = spacing.medium, top = spacing.small, bottom = spacing.medium),
            label = { Text(stringResource(R.string.search_field_label)) },
            supportingText = if (cancelledQuery == null) {
                null
            } else {
                {
                    Text(
                        text = stringResource(R.string.search_cancelled, cancelledQuery),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            },
            leadingIcon = {
                Icon(painterResource(R.drawable.ic_search), contentDescription = null)
            },
            trailingIcon = if (query.isEmpty()) null else {
                {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            painterResource(R.drawable.ic_clear),
                            contentDescription = stringResource(R.string.search_clear_action),
                        )
                    }
                }
            },
            colors = archiveSearchFieldColors(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )
    }
}
