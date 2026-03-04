package fr.pokenity.pokenity.presentation.compare

import fr.pokenity.pokenity.domain.model.PokemonDetail

data class PokemonCompareUiState(
    val isLoading: Boolean = true,
    val basePokemon: PokemonDetail? = null,
    val comparedPokemon: PokemonDetail? = null,
    val errorMessage: String? = null
)

