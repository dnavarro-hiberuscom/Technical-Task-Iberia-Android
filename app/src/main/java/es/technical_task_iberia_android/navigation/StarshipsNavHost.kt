package es.technical_task_iberia_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.technical_task_iberia_android.presentation.search.SearchRoute
import kotlinx.serialization.Serializable

/** Search screen, start of the graph. */
@Serializable
internal data object SearchDestination

/** Root navigation graph. Screens come from `:presentation`; this file only wires them. */
@Composable
internal fun StarshipsNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SearchDestination,
        modifier = modifier,
    ) {
        composable<SearchDestination> {
            SearchRoute()
        }
    }
}
