package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.data.repository.PokemonRepository

class GetLocationsByRegionUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(regionName: String): List<PokemonFilterOption> {
        return pokemonRepository.getLocationsByRegion(regionName)
    }
}
