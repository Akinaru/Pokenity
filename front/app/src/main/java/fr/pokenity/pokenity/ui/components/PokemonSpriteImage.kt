package fr.pokenity.pokenity.ui.components

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.data.core.PokemonVisualPreset
import fr.pokenity.data.core.pokemonImageUrlCandidates

@Composable
fun PokemonSpriteImage(
    pokemonId: Int,
    contentDescription: String,
    imageType: PokemonImageType,
    shiny: Boolean,
    visualPreset: PokemonVisualPreset? = null,
    colorFilter: ColorFilter? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
    val candidates = remember(pokemonId, imageType, shiny, visualPreset) {
        pokemonImageUrlCandidates(
            id = pokemonId,
            imageType = imageType,
            shiny = shiny,
            visualPreset = visualPreset
        )
    }
    var candidateIndex by rememberSaveable(pokemonId, imageType, shiny, visualPreset?.key) { mutableIntStateOf(0) }

    AsyncImage(
        imageLoader = imageLoader,
        model = candidates[candidateIndex],
        contentDescription = contentDescription,
        colorFilter = colorFilter,
        contentScale = contentScale,
        modifier = modifier,
        onError = {
            if (candidateIndex < candidates.lastIndex) {
                candidateIndex += 1
            }
        }
    )
}
