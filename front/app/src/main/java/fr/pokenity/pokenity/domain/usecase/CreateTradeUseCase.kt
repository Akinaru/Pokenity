package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.Trade
import fr.pokenity.data.repository.SocialRepository

class CreateTradeUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(
        offeredInventoryItemId: String,
        targetUserId: String? = null
    ): Trade {
        return socialRepository.createTrade(offeredInventoryItemId, targetUserId)
    }
}
