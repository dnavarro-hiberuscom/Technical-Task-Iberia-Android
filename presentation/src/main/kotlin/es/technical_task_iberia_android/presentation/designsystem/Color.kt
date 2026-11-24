package es.technical_task_iberia_android.presentation.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Palette. Only the colour schemes below read these values; screens use MaterialTheme.colorScheme.
private val Indigo10 = Color(0xFF001A41)
private val Indigo20 = Color(0xFF002E69)
private val Indigo30 = Color(0xFF004494)
private val Indigo40 = Color(0xFF1F5BB8)
private val Indigo80 = Color(0xFFAEC6FF)
private val Indigo90 = Color(0xFFD8E2FF)
private val Slate10 = Color(0xFF151B2C)
private val Slate20 = Color(0xFF2A3042)
private val Slate30 = Color(0xFF404659)
private val Slate40 = Color(0xFF575E71)
private val Slate80 = Color(0xFFBFC6DC)
private val Slate90 = Color(0xFFDBE2F9)
private val Amber10 = Color(0xFF2B1700)
private val Amber20 = Color(0xFF472A00)
private val Amber30 = Color(0xFF653E00)
private val Amber40 = Color(0xFF855300)
private val Amber80 = Color(0xFFFFB95C)
private val Amber90 = Color(0xFFFFDDB7)
private val Red10 = Color(0xFF410002)
private val Red20 = Color(0xFF690005)
private val Red30 = Color(0xFF93000A)
private val Red40 = Color(0xFFBA1A1A)
private val Red80 = Color(0xFFFFB4AB)
private val Red90 = Color(0xFFFFDAD6)
private val Neutral6 = Color(0xFF111318)
private val Neutral10 = Color(0xFF1A1B20)
private val Neutral12 = Color(0xFF1E1F25)
private val Neutral17 = Color(0xFF282A2F)
private val Neutral22 = Color(0xFF33353A)
private val Neutral90 = Color(0xFFE2E2E9)
private val Neutral94 = Color(0xFFEEEDF4)
private val Neutral96 = Color(0xFFF3F3FA)
private val Neutral98 = Color(0xFFFAF9FF)
private val NeutralVariant30 = Color(0xFF44474F)
private val NeutralVariant50 = Color(0xFF74777F)
private val NeutralVariant80 = Color(0xFFC4C6D0)
private val NeutralVariant90 = Color(0xFFE0E2EC)

/**
 * Light scheme. Every container colour is paired with its `on` colour so text keeps at least
 * WCAG AA contrast (4.5:1) against the surface it is drawn on.
 */
internal val LightColors: ColorScheme = lightColorScheme(
    primary = Indigo40,
    onPrimary = Color.White,
    primaryContainer = Indigo90,
    onPrimaryContainer = Indigo10,
    secondary = Slate40,
    onSecondary = Color.White,
    secondaryContainer = Slate90,
    onSecondaryContainer = Slate10,
    tertiary = Amber40,
    onTertiary = Color.White,
    tertiaryContainer = Amber90,
    onTertiaryContainer = Amber10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Neutral98,
    onBackground = Neutral10,
    surface = Neutral98,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    surfaceContainer = Neutral94,
    surfaceContainerHigh = Neutral90,
    surfaceContainerLow = Neutral96,
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
)

/** Dark scheme with the same pairing rules as [LightColors]. */
internal val DarkColors: ColorScheme = darkColorScheme(
    primary = Indigo80,
    onPrimary = Indigo20,
    primaryContainer = Indigo30,
    onPrimaryContainer = Indigo90,
    secondary = Slate80,
    onSecondary = Slate20,
    secondaryContainer = Slate30,
    onSecondaryContainer = Slate90,
    tertiary = Amber80,
    onTertiary = Amber20,
    tertiaryContainer = Amber30,
    onTertiaryContainer = Amber90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Neutral6,
    onBackground = Neutral90,
    surface = Neutral6,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    surfaceContainer = Neutral12,
    surfaceContainerHigh = Neutral17,
    surfaceContainerLow = Neutral10,
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant30,
)
