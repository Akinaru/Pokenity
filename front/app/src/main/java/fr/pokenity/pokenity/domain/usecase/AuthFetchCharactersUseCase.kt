package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.AuthCharacter
import fr.pokenity.data.repository.AuthRepository

class AuthFetchCharactersUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): List<AuthCharacter> {
        return authRepository.fetchCharacters()
    }
}
