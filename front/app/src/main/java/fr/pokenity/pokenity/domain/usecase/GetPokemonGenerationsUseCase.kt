package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.data.repository.PokemonRepository

class GetPokemonGenerationsUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(): List<PokemonFilterOption> {
        return pokemonRepository.getPokemonGenerations()
    }
}
