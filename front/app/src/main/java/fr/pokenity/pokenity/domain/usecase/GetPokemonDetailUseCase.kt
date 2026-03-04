package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonDetail
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonDetailUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(id: Int): PokemonDetail {
        return pokemonRepository.getPokemonDetail(id)
    }
}
