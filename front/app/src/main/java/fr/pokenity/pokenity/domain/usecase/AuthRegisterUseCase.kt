package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.AuthenticatedSession
import fr.pokenity.data.repository.AuthRepository

class AuthRegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        characterId: String? = null
    ): AuthenticatedSession {
        return authRepository.register(
            username = username,
            email = email,
            password = password,
            characterId = characterId
        )
    }
}
