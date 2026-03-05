package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.repository.PokemonRepository

class GetPokemonListUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(limit: Int = 80, offset: Int = 0): List<PokemonSummary> {
        return pokemonRepository.getPokemonList(limit, offset)
    }
}
