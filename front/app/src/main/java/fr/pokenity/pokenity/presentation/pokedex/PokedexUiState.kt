package fr.pokenity.pokenity.presentation.pokedex

import fr.pokenity.pokenity.domain.model.PokemonSummary

enum class PokedexSection {
    ALL,
    TYPE,
    GENERATION
}

data class PokedexUiState(
    val isLoading: Boolean = true,
    val pokemon: List<PokemonSummary> = emptyList(),
    val types: List<String> = emptyList(),
    val generations: List<String> = emptyList(),
    val selectedSection: PokedexSection = PokedexSection.ALL,
    val errorMessage: String? = null
)
