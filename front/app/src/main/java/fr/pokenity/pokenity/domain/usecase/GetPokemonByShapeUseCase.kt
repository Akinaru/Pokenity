package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.repository.PokemonRepository

class GetPokemonByShapeUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(shapeName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByShape(shapeName)
    }
}
