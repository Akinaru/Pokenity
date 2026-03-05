package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonDetail
import fr.pokenity.data.repository.PokemonRepository

class GetPokemonDetailUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(id: Int): PokemonDetail {
        return pokemonRepository.getPokemonDetail(id)
    }
}
