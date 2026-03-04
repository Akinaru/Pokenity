package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetLocationAreasByLocationUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(locationName: String): List<PokemonFilterOption> {
        return pokemonRepository.getLocationAreasByLocation(locationName)
    }
}
