package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonByRegionUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(regionName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByRegion(regionName)
    }
}
