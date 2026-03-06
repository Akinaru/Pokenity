package fr.pokenity.data.model

data class Trade(
    val id: String,
    val status: TradeStatus,
    val proposerId: String,
    val recipientId: String?,
    val proposer: UserProfile?,
    val recipient: UserProfile?,
    val offeredPokemons: List<TradePokemon> = emptyList(),
    val offeredPokemon: TradePokemon?,
    val receivedPokemon: TradePokemon?,
    val requestedPokemons: List<TradePokemon> = emptyList(),
    val createdAt: String?,
    val expiresAt: String?
)
