package fr.pokenity.pokenity.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import fr.pokenity.data.core.PokemonVisualPreset
import fr.pokenity.data.core.typeImageUrlCandidates

@Composable
fun TypeSpriteImage(
    typeId: Int,
    contentDescription: String,
    visualPreset: PokemonVisualPreset?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val candidates = remember(typeId, visualPreset?.key) {
        typeImageUrlCandidates(typeId = typeId, visualPreset = visualPreset)
    }
    var candidateIndex by rememberSaveable(typeId, visualPreset?.key) { mutableIntStateOf(0) }

    AsyncImage(
        model = candidates[candidateIndex],
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onError = {
            if (candidateIndex < candidates.lastIndex) {
                candidateIndex += 1
            }
        }
    )
}
