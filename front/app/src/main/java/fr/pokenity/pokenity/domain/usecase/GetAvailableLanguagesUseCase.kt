package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.LanguageOption
import fr.pokenity.pokenity.domain.repository.PokemonRepository

class GetAvailableLanguagesUseCase(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(): List<LanguageOption> {
        return repository.getAvailableLanguages()
    }
}
