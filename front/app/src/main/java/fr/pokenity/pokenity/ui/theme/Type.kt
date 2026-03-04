package fr.pokenity.pokenity.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fr.pokenity.pokenity.R

// Point unique pour appliquer la meme police dans toute l'app.
val AppFontFamily = FontFamily(
    Font(R.font.clash_display_extralight, FontWeight.ExtraLight),
    Font(R.font.clash_display_light, FontWeight.Light),
    Font(R.font.clash_display_regular, FontWeight.Normal),
    Font(R.font.clash_display_medium, FontWeight.Medium),
    Font(R.font.clash_display_semibold, FontWeight.SemiBold),
    Font(R.font.clash_display_bold, FontWeight.Bold)
)

private val BaseTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp
    )
)

val Typography = BaseTypography.copy(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = AppFontFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = AppFontFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = AppFontFamily),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = AppFontFamily),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = AppFontFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = AppFontFamily),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = AppFontFamily),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = AppFontFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = AppFontFamily),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = AppFontFamily),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = AppFontFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = AppFontFamily),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = AppFontFamily)
)
