package es.technical_task_iberia_android.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.technical_task_iberia_android.presentation.R
import es.technical_task_iberia_android.presentation.designsystem.ArchivePoster
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
        onLoadMore = viewModel::onLoadMore,
        onStarshipClick = onStarshipClick,
        modifier = modifier,
    )
}

@Composable
internal fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onStarshipClick: (id: Int) -> Unit,
    modifier: Modifier = Modifier,
    onLoadMore: () -> Unit = {},
) {
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        modifier = modifier.background(ArchivePoster).statusBarsPadding(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {
            if (!keyboardOpen) HangarHeader()
            SearchField(state.query, state.cancelledQuery, onQueryChange, onClearQuery)
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (val content = state.content) {
                    SearchContent.Initial -> SearchPrompt(Modifier.align(Alignment.Center))
                    SearchContent.Loading -> SearchLoading(
                        text = stringResource(R.string.search_loading, state.query.trim()),
                        modifier = Modifier.align(Alignment.Center),
                    )
                    SearchContent.Empty -> NoSearchResults(
                        query = state.query.trim(),
                        modifier = Modifier.align(Alignment.Center),
                    )
                    is SearchContent.Error -> SearchMessage(
                        stringResource(content.reason.messageRes()),
                        error = true,
                    )
                    is SearchContent.Results -> SearchResults(
                        results = content,
                        onLoadMore = onLoadMore,
                        onStarshipClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            onStarshipClick(it)
                        },
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
        }
    }
}

private fun SearchError.messageRes(): Int = when (this) {
    SearchError.Connectivity -> R.string.search_error_connectivity
    SearchError.Unexpected -> R.string.search_error_unexpected
}
