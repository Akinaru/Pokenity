package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.AuthInventoryItem
import fr.pokenity.data.repository.AuthRepository

class AuthFetchInventoryUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): List<AuthInventoryItem> {
        return authRepository.fetchInventory()
    }
}
