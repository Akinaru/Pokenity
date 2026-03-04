package fr.pokenity.pokenity.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import fr.pokenity.pokenity.core.PokemonImageType
import fr.pokenity.pokenity.core.pokemonImageUrlCandidates

@Composable
fun PokemonSpriteImage(
    pokemonId: Int,
    contentDescription: String,
    imageType: PokemonImageType,
    shiny: Boolean,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val candidates = remember(pokemonId, imageType, shiny) {
        pokemonImageUrlCandidates(pokemonId, imageType, shiny)
    }
    var candidateIndex by rememberSaveable(pokemonId, imageType, shiny) { mutableIntStateOf(0) }

    AsyncImage(
        model = candidates[candidateIndex],
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        onError = {
            if (candidateIndex < candidates.lastIndex) {
                candidateIndex += 1
            }
        }
    )
}
