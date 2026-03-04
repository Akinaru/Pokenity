package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonTypesUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(): List<String> {
        return pokemonRepository.getPokemonTypes()
    }
}
