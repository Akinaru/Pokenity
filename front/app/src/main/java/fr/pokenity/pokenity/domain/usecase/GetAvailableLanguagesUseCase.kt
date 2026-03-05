package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.LanguageOption
import fr.pokenity.data.repository.PokemonRepository

class GetAvailableLanguagesUseCase(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(): List<LanguageOption> {
        return repository.getAvailableLanguages()
    }
}
