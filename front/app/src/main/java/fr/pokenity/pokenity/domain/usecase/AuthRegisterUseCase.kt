package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.AuthenticatedSession
import fr.pokenity.pokenity.domain.repository.AuthRepository

class AuthRegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String): AuthenticatedSession {
        return authRepository.register(username = username, email = email, password = password)
    }
}
