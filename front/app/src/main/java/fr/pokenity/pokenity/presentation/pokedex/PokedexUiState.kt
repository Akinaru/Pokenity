package fr.pokenity.pokenity.presentation.pokedex

import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.data.model.PokemonSummary

enum class PokedexSection {
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
    val selectedType: PokemonFilterOption? = null,
    val selectedGeneration: PokemonFilterOption? = null,
    val selectedAbility: PokemonFilterOption? = null,
    val selectedHabitat: PokemonFilterOption? = null,
    val selectedRegion: PokemonFilterOption? = null,
    val selectedShape: PokemonFilterOption? = null,
    val errorMessage: String? = null
) {
    val selectedTypeLabel: String?
        get() = selectedType?.label
    val selectedGenerationLabel: String?
        get() = selectedGeneration?.label
    val selectedAbilityLabel: String?
        get() = selectedAbility?.label
    val selectedHabitatLabel: String?
        get() = selectedHabitat?.label
    val selectedRegionLabel: String?
        get() = selectedRegion?.label
    val selectedShapeLabel: String?
        get() = selectedShape?.label
    val hasActiveFilters: Boolean
        get() = selectedType != null ||
            selectedGeneration != null ||
            selectedAbility != null ||
            selectedHabitat != null ||
            selectedRegion != null ||
            selectedShape != null
}
