package es.technical_task_iberia_android.presentation.designsystem

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Fixed print colours keep the masthead, artwork and search control consistent in both modes.
internal val ArchiveInk = Color(0xFF03384C)
internal val ArchivePaper = Color(0xFFFAF6EF)
internal val ArchiveOrange = Color(0xFFFF791C)
internal val ArchiveType = Color(0xFF101E23)
private val ArchiveCopper = Color(0xFFA83D06)
internal val ArchivePoster = Color(0xFF04242F)
internal val ArchiveHeroScrim = ArchivePoster.copy(alpha = 0.90f)
internal val ArchiveHeroControl = Color(0xCC203B45)

internal val LightColors = lightColorScheme(
    primary = ArchiveCopper,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC4),
    onPrimaryContainer = Color(0xFF552000),
    secondary = ArchiveInk,
    onSecondary = ArchivePaper,
    secondaryContainer = Color(0xFFD6E9EA),
    onSecondaryContainer = ArchiveInk,
    background = ArchivePaper,
    onBackground = ArchiveType,
    surface = ArchivePaper,
    surfaceContainerLow = Color(0xFFFFFCF7),
    onSurface = ArchiveType,
    surfaceVariant = Color(0xFFECE7DD),
    onSurfaceVariant = Color(0xFF495256),
    outline = Color(0xFF797C79),
    outlineVariant = Color(0xFFCDC9C0),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFFFAB78),
    onPrimary = Color(0xFF552000),
    primaryContainer = Color(0xFF723115),
    onPrimaryContainer = Color(0xFFFFDBC4),
    secondary = Color(0xFFA7D3DE),
    onSecondary = Color(0xFF053544),
    secondaryContainer = Color(0xFF244956),
    onSecondaryContainer = Color(0xFFD6E9EA),
    background = Color(0xFF111E25),
    onBackground = ArchivePaper,
    surface = Color(0xFF111E25),
    surfaceContainerLow = Color(0xFF17272F),
    onSurface = ArchivePaper,
    surfaceVariant = Color(0xFF203039),
    onSurfaceVariant = Color(0xFFB6C5CB),
    outline = Color(0xFF87989F),
    outlineVariant = Color(0xFF3C4D55),
)

@Composable
internal fun archiveSearchFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = ArchiveType,
    unfocusedTextColor = ArchiveType,
    focusedContainerColor = ArchivePaper,
    unfocusedContainerColor = ArchivePaper,
    cursorColor = ArchiveCopper,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedLabelColor = ArchiveType,
    unfocusedLabelColor = ArchiveType,
    focusedLeadingIconColor = ArchiveCopper,
    unfocusedLeadingIconColor = ArchiveCopper,
    focusedTrailingIconColor = ArchiveType,
    unfocusedTrailingIconColor = ArchiveType,
    focusedSupportingTextColor = ArchivePaper,
    unfocusedSupportingTextColor = ArchivePaper,
)
