package fr.pokenity.pokenity.presentation.pokedex

import fr.pokenity.pokenity.domain.model.PokemonSummary

data class PokedexUiState(
    val isLoading: Boolean = true,
    val pokemon: List<PokemonSummary> = emptyList(),
    val errorMessage: String? = null
)
