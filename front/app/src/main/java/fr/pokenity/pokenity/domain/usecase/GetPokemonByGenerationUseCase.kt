package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonByGenerationUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(generationName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByGeneration(generationName)
    }
}
