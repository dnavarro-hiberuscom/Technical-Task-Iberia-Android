package es.technical_task_iberia_android.presentation.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val base = Typography()

internal val StarshipsTypography = Typography(
    displayLarge = base.displayLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        letterSpacing = (-1.5).sp,
    ),
    headlineLarge = base.headlineLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        lineHeight = 42.sp,
        letterSpacing = (-1.5).sp,
    ),
    titleLarge = base.titleLarge.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    titleMedium = base.titleMedium.copy(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif, letterSpacing = 0.sp),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
)
