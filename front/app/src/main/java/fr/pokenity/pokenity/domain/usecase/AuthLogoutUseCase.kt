package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.repository.AuthRepository

class AuthLogoutUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke() {
        authRepository.logout()
    }
}
