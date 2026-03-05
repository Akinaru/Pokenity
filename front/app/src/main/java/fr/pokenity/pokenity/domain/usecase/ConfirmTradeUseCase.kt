package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.Trade
import fr.pokenity.data.repository.SocialRepository

class ConfirmTradeUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(tradeId: String): Trade {
        return socialRepository.confirmTrade(tradeId)
    }
}
