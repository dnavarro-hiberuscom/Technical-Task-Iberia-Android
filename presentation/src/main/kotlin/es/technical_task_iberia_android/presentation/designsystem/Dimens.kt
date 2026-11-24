package es.technical_task_iberia_android.presentation.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale. Screens pick a step from here instead of writing `dp` literals. */
@Immutable
data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
)

/** Component sizes shared by the screens. */
@Immutable
data class Sizes(
    val maxDetailImageHeight: Dp = 504.dp,
    val detailContentMaxWidth: Dp = 600.dp,
    val inlineDetailMinWidth: Dp = 256.dp,
    val searchContentMaxWidth: Dp = 640.dp,
    val inlineSearchMinWidth: Dp = 260.dp,
    val detailTitleFadeHeight: Dp = 72.dp,
    val detailTitleAccentWidth: Dp = 52.dp,
    val hangarArt: Dp = 288.dp,
    val starshipThumbnailWidth: Dp = 116.dp,
    val starshipThumbnailHeight: Dp = 88.dp,
    val outlineStroke: Dp = 1.dp,
    val insignia: Dp = 56.dp,
    /** Minimum touch target for any action (Material and Android accessibility guidance). */
    val minTouchTarget: Dp = 48.dp,
    /** Standard icon size. */
    val icon: Dp = 24.dp,
    /** Indeterminate progress indicator shown while loading. */
    val progressIndicator: Dp = 48.dp,
)

internal val LocalSpacing = staticCompositionLocalOf { Spacing() }
internal val LocalSizes = staticCompositionLocalOf { Sizes() }
