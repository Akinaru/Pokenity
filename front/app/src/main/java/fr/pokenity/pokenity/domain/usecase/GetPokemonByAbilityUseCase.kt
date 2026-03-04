package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetPokemonByAbilityUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(abilityName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByAbility(abilityName)
    }
}
