package fr.pokenity.pokenity.presentation.map

import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary

data class MapUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val regions: List<PokemonFilterOption> = emptyList(),
    val locations: List<PokemonFilterOption> = emptyList(),
    val areas: List<PokemonFilterOption> = emptyList(),
    val pokemons: List<PokemonSummary> = emptyList(),
    val selectedRegion: PokemonFilterOption? = null,
    val selectedLocation: PokemonFilterOption? = null,
    val selectedArea: PokemonFilterOption? = null
)
