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
import androidx.compose.ui.text.font.FontFamily
import fr.pokenity.pokenity.ui.theme.AppFontFamily

val AuthAccentYellow = Color(0xFFFFCC18)
val AuthInputBackground = Color(0xFFD0EAFD)
val AuthInputText = Color(0xFF000000)
val AuthInputPlaceholder = Color(0xFF4B5563)

val AuthFontFamily: FontFamily = AppFontFamily

@Composable
fun AuthBackgroundContainer(
    backgroundDrawableName: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val backgroundResId = remember(context, backgroundDrawableName) {
        if (backgroundDrawableName.isNullOrBlank()) {
            0
        } else {
            context.resources.getIdentifier(backgroundDrawableName, "drawable", context.packageName)
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color.Transparent) {
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
