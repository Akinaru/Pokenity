package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.repository.PokemonRepository

class GetPokemonByAbilityUseCase(
    private val pokemonRepository: PokemonRepository
) {
    suspend operator fun invoke(abilityName: String): List<PokemonSummary> {
        return pokemonRepository.getPokemonByAbility(abilityName)
    }
}
