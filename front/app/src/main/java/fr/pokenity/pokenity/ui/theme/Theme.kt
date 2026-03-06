package fr.pokenity.pokenity.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PokeYellow,
    secondary = PokeBlue,
    tertiary = PokeRed,
    background = Slate,
    surface = Color(0xFF151515),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PokeBlue,
    secondary = PokeRed,
    tertiary = PokeYellow,
    background = PokeWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = DarkInk,
    onBackground = DarkInk,
    onSurface = DarkInk,
    onSurfaceVariant = DarkInk
)

@Composable
fun PokenityTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
