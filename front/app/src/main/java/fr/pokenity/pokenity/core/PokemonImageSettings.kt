package fr.pokenity.pokenity.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PokemonImageType(
    val key: String,
    val label: String,
    val folder: String,
    val extension: String,
    val supportsShiny: Boolean
) {
    OFFICIAL_ARTWORK(
        key = "official-artwork",
        label = "Official Artwork",
        folder = "official-artwork",
        extension = "png",
        supportsShiny = true
    ),
    HOME(
        key = "home",
        label = "Home",
        folder = "home",
        extension = "png",
        supportsShiny = true
    ),
    DREAM_WORLD(
        key = "dream-world",
        label = "Dream World",
        folder = "dream-world",
        extension = "svg",
        supportsShiny = false
    )
}

object PokemonImageSettings {
    private val _imageType = MutableStateFlow(PokemonImageType.OFFICIAL_ARTWORK)
    val imageType: StateFlow<PokemonImageType> = _imageType.asStateFlow()

    private val _isShiny = MutableStateFlow(false)
    val isShiny: StateFlow<Boolean> = _isShiny.asStateFlow()

    fun setImageType(type: PokemonImageType) {
        _imageType.value = type
        if (!type.supportsShiny) {
            _isShiny.value = false
        }
    }

    fun setShiny(enabled: Boolean) {
        val currentType = _imageType.value
        _isShiny.value = if (currentType.supportsShiny) enabled else false
    }

    fun toggleShiny() {
        setShiny(!_isShiny.value)
    }
}

fun pokemonImageUrl(id: Int, imageType: PokemonImageType, shiny: Boolean): String {
    val isShiny = shiny && imageType.supportsShiny
    val shinySegment = if (isShiny) "shiny/" else ""

    return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/${imageType.folder}/$shinySegment$id.${imageType.extension}"
}

fun pokemonImageUrlCandidates(id: Int, imageType: PokemonImageType, shiny: Boolean): List<String> {
    val isShiny = shiny && imageType.supportsShiny
    val shinySegment = if (isShiny) "shiny/" else ""

    val selected = pokemonImageUrl(id, imageType, shiny)
    val official = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$shinySegment$id.png"
    val home = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$shinySegment$id.png"
    val basic = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${shinySegment}$id.png"

    return linkedSetOf(selected, official, home, basic).toList()
}
