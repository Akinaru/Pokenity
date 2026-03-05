package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.repository.AuthRepository

class AuthCheckEmailExistsUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Boolean {
        return authRepository.checkEmailExists(email = email)
    }
}
