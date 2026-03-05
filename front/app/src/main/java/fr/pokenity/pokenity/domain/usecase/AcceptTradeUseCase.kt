package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.Trade
import fr.pokenity.data.repository.SocialRepository

class AcceptTradeUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(tradeId: String, offeredInventoryItemId: String): Trade {
        return socialRepository.acceptTrade(tradeId, offeredInventoryItemId)
    }
}
