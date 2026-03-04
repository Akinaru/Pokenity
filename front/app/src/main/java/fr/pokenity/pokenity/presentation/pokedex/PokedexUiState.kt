package fr.pokenity.pokenity.presentation.pokedex

import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary

enum class PokedexSection {
    ALL,
    TYPE,
    GENERATION,
    ABILITY,
    HABITAT,
    REGION,
    SHAPE
}

data class PokedexUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMorePokemon: Boolean = true,
    val pokemon: List<PokemonSummary> = emptyList(),
    val filteredPokemon: List<PokemonSummary> = emptyList(),
    val types: List<PokemonFilterOption> = emptyList(),
    val generations: List<PokemonFilterOption> = emptyList(),
    val abilities: List<PokemonFilterOption> = emptyList(),
    val habitats: List<PokemonFilterOption> = emptyList(),
    val regions: List<PokemonFilterOption> = emptyList(),
    val shapes: List<PokemonFilterOption> = emptyList(),
    val selectedSection: PokedexSection = PokedexSection.ALL,
    val selectedTypeLabel: String? = null,
    val selectedGenerationLabel: String? = null,
    val selectedAbilityLabel: String? = null,
    val selectedHabitatLabel: String? = null,
    val selectedRegionLabel: String? = null,
    val selectedShapeLabel: String? = null,
    val errorMessage: String? = null
)
