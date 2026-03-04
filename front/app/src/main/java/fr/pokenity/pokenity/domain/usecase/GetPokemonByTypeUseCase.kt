package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonByTypeUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(typeName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByType(typeName)
    }
}
