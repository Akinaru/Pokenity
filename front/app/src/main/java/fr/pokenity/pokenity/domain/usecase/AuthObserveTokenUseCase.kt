package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.pokenity.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class AuthObserveTokenUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<String?> = authRepository.token
}
