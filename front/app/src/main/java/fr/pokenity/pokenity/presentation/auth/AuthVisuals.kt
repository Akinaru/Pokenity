package fr.pokenity.pokenity.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import fr.pokenity.pokenity.R

val AuthAccentYellow = Color(0xFFFFCC18)
val AuthInputBackground = Color(0xFFD0EAFD)
val AuthInputText = Color(0xFF180707)

val AuthFontFamily = FontFamily(
    Font(R.font.clash_display_extralight, FontWeight.ExtraLight),
    Font(R.font.clash_display_light, FontWeight.Light),
    Font(R.font.clash_display_regular, FontWeight.Normal),
    Font(R.font.clash_display_medium, FontWeight.Medium),
    Font(R.font.clash_display_semibold, FontWeight.SemiBold),
    Font(R.font.clash_display_bold, FontWeight.Bold)
)

@Composable
fun AuthBackgroundContainer(
    backgroundDrawableName: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val backgroundResId = remember(context, backgroundDrawableName) {
        context.resources.getIdentifier(backgroundDrawableName, "drawable", context.packageName)
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (backgroundResId != 0) {
                Image(
                    painter = painterResource(id = backgroundResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            content()
        }
    }
}
