package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.data.repository.PokemonRepository

class GetLocationAreasByLocationUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(locationName: String): List<PokemonFilterOption> {
        return pokemonRepository.getLocationAreasByLocation(locationName)
    }
}
