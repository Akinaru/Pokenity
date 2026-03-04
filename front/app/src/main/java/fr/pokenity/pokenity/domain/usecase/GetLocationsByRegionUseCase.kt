package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetLocationsByRegionUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(regionName: String): List<PokemonFilterOption> {
        return pokemonRepository.getLocationsByRegion(regionName)
    }
}
