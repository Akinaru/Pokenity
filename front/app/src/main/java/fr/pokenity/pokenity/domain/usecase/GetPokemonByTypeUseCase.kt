package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.repository.PokemonRepository

class GetPokemonByTypeUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(typeName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByType(typeName)
    }
}
