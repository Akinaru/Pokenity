package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.Trade
import fr.pokenity.data.repository.SocialRepository

class DeclineTradeUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(tradeId: String): Trade {
        return socialRepository.declineTrade(tradeId)
    }
}
