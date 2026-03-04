package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.AuthCharacter
import fr.pokenity.pokenity.domain.repository.AuthRepository

class AuthFetchCharactersUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): List<AuthCharacter> {
        return authRepository.fetchCharacters()
    }
}
