package es.technical_task_iberia_android.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * The single Material 3 theme of the app. `:app` applies it once at the root, so every screen and
 * state (loading, empty, error) shares the same colours, type, shapes and spacing.
 *
 * Dynamic colour is off on purpose: the fixed schemes guarantee the contrast pairs in [LightColors]
 * and [DarkColors].
 */
@Composable
fun StarshipsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSizes provides Sizes(),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = StarshipsTypography,
            shapes = StarshipsShapes,
            content = content,
        )
    }
}

/** Access to the app tokens that Material does not cover. */
object StarshipsTheme {
    val spacing: Spacing
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current

    val sizes: Sizes
        @Composable @ReadOnlyComposable
        get() = LocalSizes.current
}
