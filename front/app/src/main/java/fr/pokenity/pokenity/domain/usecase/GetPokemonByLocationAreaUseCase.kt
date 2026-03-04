package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonByLocationAreaUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(locationAreaName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByLocationArea(locationAreaName)
    }
}
