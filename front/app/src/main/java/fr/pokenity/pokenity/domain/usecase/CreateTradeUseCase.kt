package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradeOfferSelection
import fr.pokenity.data.model.TradePokemon
import fr.pokenity.data.repository.SocialRepository

class CreateTradeUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(
        offeredPokemons: List<TradeOfferSelection>,
        requestedPokemons: List<TradePokemon>
    ): Trade {
        return socialRepository.createTrade(offeredPokemons, requestedPokemons)
    }
}
