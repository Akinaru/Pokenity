package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.Trade
import fr.pokenity.data.repository.SocialRepository

class GetOpenTradesUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(): List<Trade> {
        return socialRepository.getOpenTrades()
    }
}
