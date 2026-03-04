package fr.pokenity.pokenity.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun AppBackground(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val backgroundResId = remember(context, darkTheme) {
        val preferred = if (darkTheme) "background" else "background_white"
        val fallback = if (darkTheme) "background_white" else "background"
        val preferredResId = context.resources.getIdentifier(preferred, "drawable", context.packageName)
        if (preferredResId != 0) {
            preferredResId
        } else {
            context.resources.getIdentifier(fallback, "drawable", context.packageName)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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
