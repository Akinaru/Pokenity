package fr.pokenity.pokenity.presentation.detail

import fr.pokenity.pokenity.domain.model.PokemonDetail

data class PokemonDetailUiState(
    val isLoading: Boolean = true,
    val pokemon: PokemonDetail? = null,
    val errorMessage: String? = null
)
