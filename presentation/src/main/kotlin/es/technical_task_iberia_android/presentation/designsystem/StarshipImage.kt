package es.technical_task_iberia_android.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import es.technical_task_iberia_android.presentation.R

private const val StarshipImageBaseUrl = "https://starwarsgallery.netlify.app/assets/starships"

@Composable
internal fun StarshipImage(starshipId: Int, modifier: Modifier = Modifier) {
    val locator = painterResource(R.drawable.blueprint_locator)
    val model = if (LocalInspectionMode.current) null else "$StarshipImageBaseUrl/$starshipId.jpg"

    Box(modifier.background(ArchiveInk)) {
        AsyncImage(
            model = model,
            contentDescription = null,
            placeholder = locator,
            error = locator,
            fallback = locator,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
