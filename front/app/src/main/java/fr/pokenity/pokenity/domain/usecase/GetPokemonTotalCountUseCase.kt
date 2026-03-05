package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.repository.PokemonRepository

class GetPokemonTotalCountUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(): Int {
        return pokemonRepository.getPokemonTotalCount()
    }
}
