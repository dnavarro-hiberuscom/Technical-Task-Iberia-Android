package es.technical_task_iberia_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import es.technical_task_iberia_android.presentation.detail.DetailRoute
import es.technical_task_iberia_android.presentation.search.SearchRoute
import kotlinx.serialization.Serializable

/** Search screen, start of the graph. */
@Serializable
internal data object SearchDestination

/** Detail of one starship; only the id travels in the route. */
@Serializable
internal data class DetailDestination(val starshipId: Int)

/**
 * Root navigation graph. Screens come from `:presentation`; this file only wires them.
 * The search entry stays in the back stack while the detail is open, so going back keeps the
 * query and the results.
 */
@Composable
internal fun StarshipsNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SearchDestination,
        modifier = modifier,
    ) {
        composable<SearchDestination> {
            SearchRoute(
                onStarshipClick = { starshipId -> navController.navigate(DetailDestination(starshipId)) },
            )
        }
        composable<DetailDestination> { entry ->
            DetailRoute(
                starshipId = entry.toRoute<DetailDestination>().starshipId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
