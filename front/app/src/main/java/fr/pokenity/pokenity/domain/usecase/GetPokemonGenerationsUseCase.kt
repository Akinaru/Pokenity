package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonGenerationsUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(): List<PokemonFilterOption> {
        return pokemonRepository.getPokemonGenerations()
    }
}
