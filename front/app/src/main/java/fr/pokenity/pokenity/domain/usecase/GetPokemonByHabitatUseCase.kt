package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonByHabitatUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(habitatName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByHabitat(habitatName)
    }
}
