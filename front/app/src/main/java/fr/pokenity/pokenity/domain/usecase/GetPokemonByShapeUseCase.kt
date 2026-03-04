package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonByShapeUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(shapeName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByShape(shapeName)
    }
}
