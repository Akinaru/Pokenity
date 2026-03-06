package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradeOfferSelection
import fr.pokenity.data.model.TradePokemon
import fr.pokenity.data.repository.SocialRepository

class AcceptTradeUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(tradeId: String, selectedOffered: List<TradePokemon>, givenPokemons: List<TradeOfferSelection>): Trade {
        return socialRepository.acceptTrade(tradeId, selectedOffered, givenPokemons)
    }
}
