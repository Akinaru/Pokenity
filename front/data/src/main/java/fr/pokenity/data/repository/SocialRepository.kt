package fr.pokenity.data.repository

import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.TradeOfferSelection
import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradePokemon
import fr.pokenity.data.model.UserProfile

interface SocialRepository {
    suspend fun getOpenTrades(): List<Trade>
    suspend fun getMyTrades(): List<Trade>
    suspend fun createTrade(offeredPokemons: List<TradeOfferSelection>, requestedPokemons: List<TradePokemon>): Trade
    suspend fun acceptTrade(tradeId: String): Trade
    suspend fun confirmTrade(tradeId: String): Trade
    suspend fun cancelTrade(tradeId: String): Trade
    suspend fun declineTrade(tradeId: String): Trade
    suspend fun getUsers(): List<UserProfile>
    suspend fun getMyInventory(): List<InventoryItem>
}
