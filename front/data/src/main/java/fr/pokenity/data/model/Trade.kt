package fr.pokenity.data.model

data class Trade(
    val id: String,
    val status: TradeStatus,
    val proposerId: String,
    val recipientId: String?,
    val proposer: UserProfile?,
    val recipient: UserProfile?,
    val offeredPokemon: TradePokemon?,
    val receivedPokemon: TradePokemon?,
    val createdAt: String?,
    val expiresAt: String?
)
