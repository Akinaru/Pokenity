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
    Font(R.font.pokemon_pixel_font, FontWeight.Normal),
    Font(R.font.pokemon_pixel_font, FontWeight.Medium),
    Font(R.font.pokemon_pixel_font, FontWeight.SemiBold),
    Font(R.font.pokemon_pixel_font, FontWeight.Bold),
    Font(R.font.pokemon_pixel_font, FontWeight.ExtraBold)
)

val AppTitleFontFamily = FontFamily(
    Font(R.font.pixellari, FontWeight.Normal),
    Font(R.font.pixellari, FontWeight.Medium),
    Font(R.font.pixellari, FontWeight.SemiBold),
    Font(R.font.pixellari, FontWeight.Bold),
    Font(R.font.pixellari, FontWeight.ExtraBold)
)

private val BaseTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = AppTitleFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppTitleFontFamily,
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
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = AppTitleFontFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = AppTitleFontFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = AppTitleFontFamily),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = AppTitleFontFamily),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = AppTitleFontFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = AppTitleFontFamily),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = AppTitleFontFamily),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = AppTitleFontFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = AppTitleFontFamily),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = AppFontFamily),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = AppFontFamily),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = AppFontFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = AppFontFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = AppFontFamily),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = AppFontFamily)
)
