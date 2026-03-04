package fr.pokenity.pokenity.presentation.pokedex

import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary

enum class PokedexSection {
    ALL,
    TYPE,
    GENERATION
}

data class PokedexUiState(
    val isLoading: Boolean = true,
    val pokemon: List<PokemonSummary> = emptyList(),
    val filteredPokemon: List<PokemonSummary> = emptyList(),
    val types: List<PokemonFilterOption> = emptyList(),
    val generations: List<PokemonFilterOption> = emptyList(),
    val selectedSection: PokedexSection = PokedexSection.ALL,
    val selectedTypeLabel: String? = null,
    val selectedGenerationLabel: String? = null,
    val errorMessage: String? = null
)
