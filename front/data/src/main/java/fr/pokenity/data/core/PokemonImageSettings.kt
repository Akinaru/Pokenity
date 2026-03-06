package fr.pokenity.data.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PokemonVisualPreset(
    val key: String,
    val label: String,
    val pokemonGeneration: String,
    val pokemonVersion: String,
    val typeGeneration: String,
    val typeVersion: String,
    val supportsShiny: Boolean
)

val PokemonVisualPresets: List<PokemonVisualPreset> = listOf(
    PokemonVisualPreset(
        key = "default",
        label = "Default",
        pokemonGeneration = "",
        pokemonVersion = "",
        typeGeneration = "generation-viii",
        typeVersion = "sword-shield",
        supportsShiny = true
    ),
    PokemonVisualPreset(
        key = "gen-i-red-blue",
        label = "Gen I - Red/Blue",
        pokemonGeneration = "generation-i",
        pokemonVersion = "red-blue",
        typeGeneration = "generation-viii",
        typeVersion = "sword-shield",
        supportsShiny = false
    ),
    PokemonVisualPreset(
        key = "gen-ii-crystal",
        label = "Gen II - Crystal",
        pokemonGeneration = "generation-ii",
        pokemonVersion = "crystal",
        typeGeneration = "generation-viii",
        typeVersion = "sword-shield",
        supportsShiny = true
    ),
    PokemonVisualPreset(
        key = "gen-iii-emerald",
        label = "Gen III - Emerald",
        pokemonGeneration = "generation-iii",
        pokemonVersion = "emerald",
        typeGeneration = "generation-iii",
        typeVersion = "emerald",
        supportsShiny = false
    ),
    PokemonVisualPreset(
        key = "gen-iv-platinum",
        label = "Gen IV - Platinum",
        pokemonGeneration = "generation-iv",
        pokemonVersion = "platinum",
        typeGeneration = "generation-iv",
        typeVersion = "platinum",
        supportsShiny = false
    ),
    PokemonVisualPreset(
        key = "gen-v-black-white",
        label = "Gen V - Black/White",
        pokemonGeneration = "generation-v",
        pokemonVersion = "black-white",
        typeGeneration = "generation-v",
        typeVersion = "black-white",
        supportsShiny = true
    ),
    PokemonVisualPreset(
        key = "gen-vi-xy",
        label = "Gen VI - X/Y",
        pokemonGeneration = "generation-vi",
        pokemonVersion = "x-y",
        typeGeneration = "generation-vi",
        typeVersion = "x-y",
        supportsShiny = true
    ),
    PokemonVisualPreset(
        key = "gen-vii-usum",
        label = "Gen VII - Ultra Sun/Ultra Moon",
        pokemonGeneration = "generation-vii",
        pokemonVersion = "ultra-sun-ultra-moon",
        typeGeneration = "generation-vii",
        typeVersion = "ultra-sun-ultra-moon",
        supportsShiny = true
    ),
    PokemonVisualPreset(
        key = "gen-viii-sword-shield",
        label = "Gen VIII - Sword/Shield",
        pokemonGeneration = "generation-viii",
        pokemonVersion = "icons",
        typeGeneration = "generation-viii",
        typeVersion = "sword-shield",
        supportsShiny = false
    ),
    PokemonVisualPreset(
        key = "gen-ix-scarlet-violet",
        label = "Gen IX - Scarlet/Violet",
        pokemonGeneration = "generation-ix",
        pokemonVersion = "scarlet-violet",
        typeGeneration = "generation-ix",
        typeVersion = "scarlet-violet",
        supportsShiny = false
    )
)

enum class PokemonImageType(
    val key: String,
    val label: String,
    val folder: String,
    val extension: String,
    val supportsShiny: Boolean
) {
    SHOWDOWN(
        key = "showdown",
        label = "Showdown (GIF)",
        folder = "showdown",
        extension = "gif",
        supportsShiny = true
    ),
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
    private val _imageType = MutableStateFlow(PokemonImageType.SHOWDOWN)
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

fun pokemonImageUrlCandidates(
    id: Int,
    imageType: PokemonImageType,
    shiny: Boolean,
    visualPreset: PokemonVisualPreset? = null
): List<String> {
    val versionCandidates = visualPreset?.let {
        pokemonVersionImageUrlCandidates(id = id, preset = it, shiny = shiny)
    }.orEmpty()

    val isShiny = shiny && imageType.supportsShiny
    val shinySegment = if (isShiny) "shiny/" else ""

    val selected = pokemonImageUrl(id, imageType, shiny)
    val official = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$shinySegment$id.png"
    val home = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$shinySegment$id.png"
    val basic = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${shinySegment}$id.png"

    return linkedSetOf(*(versionCandidates.toTypedArray()), selected, official, home, basic).toList()
}

fun pokemonVersionImageUrlCandidates(
    id: Int,
    preset: PokemonVisualPreset,
    shiny: Boolean
): List<String> {
    if (preset.key == "default") return emptyList()

    val base = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/${preset.pokemonGeneration}/${preset.pokemonVersion}"
    val versionDefault = "$base/$id.png"
    val versionShiny = "$base/shiny/$id.png"
    val versionAnimated = "$base/animated/$id.gif"
    val versionAnimatedShiny = "$base/animated/shiny/$id.gif"

    return if (shiny && preset.supportsShiny) {
        linkedSetOf(versionShiny, versionAnimatedShiny, versionDefault, versionAnimated).toList()
    } else {
        linkedSetOf(versionDefault, versionAnimated).toList()
    }
}

fun typeImageUrlCandidates(typeId: Int, visualPreset: PokemonVisualPreset?): List<String> {
    val selected = if (visualPreset == null || visualPreset.key == "default") {
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/types/generation-viii/sword-shield/$typeId.png"
    } else {
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/types/${visualPreset.typeGeneration}/${visualPreset.typeVersion}/$typeId.png"
    }

    val swordShield = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/types/generation-viii/sword-shield/$typeId.png"
    val scarletViolet = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/types/generation-ix/scarlet-violet/$typeId.png"

    return linkedSetOf(selected, swordShield, scarletViolet).toList()
}
