package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.AuthenticatedSession
import fr.pokenity.pokenity.domain.repository.AuthRepository

class AuthLoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(identifier: String, password: String): AuthenticatedSession {
        return authRepository.login(identifier = identifier, password = password)
    }
}
