package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.AuthenticatedUser
import fr.pokenity.data.repository.AuthRepository

class  AuthFetchCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthenticatedUser {
        return authRepository.fetchAuthenticatedUser()
    }
}
