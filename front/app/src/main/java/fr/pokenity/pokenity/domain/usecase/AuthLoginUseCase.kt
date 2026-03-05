package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.AuthenticatedSession
import fr.pokenity.data.repository.AuthRepository

class AuthLoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(identifier: String, password: String): AuthenticatedSession {
        return authRepository.login(identifier = identifier, password = password)
    }
}
