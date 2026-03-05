package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradePokemon
import fr.pokenity.data.repository.SocialRepository

class CreateTradeUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(
        offeredInventoryItemId: String,
        requestedPokemons: List<TradePokemon>
    ): Trade {
        return socialRepository.createTrade(offeredInventoryItemId, requestedPokemons)
    }
}
