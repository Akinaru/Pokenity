package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.model.AuthenticatedUser
import fr.pokenity.pokenity.domain.repository.AuthRepository

class AuthFetchCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthenticatedUser {
        return authRepository.fetchAuthenticatedUser()
    }
}
