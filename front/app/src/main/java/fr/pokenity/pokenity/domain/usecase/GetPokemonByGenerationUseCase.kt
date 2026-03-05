package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.repository.PokemonRepository

class GetPokemonByGenerationUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(generationName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByGeneration(generationName)
    }
}
