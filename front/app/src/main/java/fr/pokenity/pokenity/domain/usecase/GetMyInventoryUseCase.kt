package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.repository.SocialRepository

class GetMyInventoryUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(): List<InventoryItem> {
        return socialRepository.getMyInventory()
    }
}
