package fr.pokenity.data.repository

import fr.pokenity.data.core.AuthSessionState
import fr.pokenity.data.model.AuthCharacter
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradePokemon
import fr.pokenity.data.model.TradeStatus
import fr.pokenity.data.model.UserProfile
import fr.pokenity.data.remote.auth.AuthApiService
import fr.pokenity.data.remote.auth.AcceptTradeRequestBody
import fr.pokenity.data.remote.auth.AuthCharacterDto
import fr.pokenity.data.remote.auth.AuthUserDto
import fr.pokenity.data.remote.auth.CreateTradeRequestBody
import fr.pokenity.data.remote.auth.InventoryItemDto
import fr.pokenity.data.remote.auth.TradeDto
import fr.pokenity.data.remote.auth.TradePokemonDto
import fr.pokenity.data.remote.auth.TradeUserLightDto

class SocialRepositoryImpl internal constructor(
    private val authApiService: AuthApiService
) : SocialRepository {

    private fun requireToken(): String {
        return AuthSessionState.token.value
            ?: throw IllegalStateException("Aucune session active.")
    }

    override suspend fun getOpenTrades(): List<Trade> {
        val token = requireToken()
        return authApiService.getOpenTrades(token).map { it.toDomain() }
    }

    override suspend fun getMyTrades(): List<Trade> {
        val token = requireToken()
        return authApiService.getMyTrades(token).map { it.toDomain() }
    }

    override suspend fun createTrade(offeredInventoryItemId: String, targetUserId: String?): Trade {
        val token = requireToken()
        val body = CreateTradeRequestBody(
            offeredInventoryItemId = offeredInventoryItemId,
            targetUserId = targetUserId
        )
        return authApiService.createTrade(token, body).toDomain()
    }

    override suspend fun acceptTrade(tradeId: String, offeredInventoryItemId: String): Trade {
        val token = requireToken()
        val body = AcceptTradeRequestBody(offeredInventoryItemId = offeredInventoryItemId)
        return authApiService.acceptTrade(token, tradeId, body).toDomain()
    }

    override suspend fun confirmTrade(tradeId: String): Trade {
        val token = requireToken()
        return authApiService.confirmTrade(token, tradeId).toDomain()
    }

    override suspend fun cancelTrade(tradeId: String): Trade {
        val token = requireToken()
        return authApiService.cancelTrade(token, tradeId).toDomain()
    }

    override suspend fun declineTrade(tradeId: String): Trade {
        val token = requireToken()
        return authApiService.declineTrade(token, tradeId).toDomain()
    }

    override suspend fun getUsers(): List<UserProfile> {
        return authApiService.getUsers().map { it.toUserProfile() }
    }

    override suspend fun getMyInventory(): List<InventoryItem> {
        val token = requireToken()
        return authApiService.getMyInventory(token).inventory.map { it.toDomain() }
    }

    // --- Mapping helpers ---

    private fun TradeDto.toDomain(): Trade {
        return Trade(
            id = id,
            status = TradeStatus.fromString(status),
            proposerId = proposerId,
            recipientId = recipientId,
            proposer = proposer?.toDomain(),
            recipient = recipient?.toDomain(),
            offeredPokemon = offeredPokemon?.toDomain(),
            receivedPokemon = receivedPokemon?.toDomain(),
            createdAt = createdAt,
            expiresAt = expiresAt
        )
    }

    private fun TradeUserLightDto.toDomain(): UserProfile {
        return UserProfile(
            id = id,
            username = username,
            xp = xp ?: 0
        )
    }

    private fun TradePokemonDto.toDomain(): TradePokemon {
        return TradePokemon(
            resourceType = resourceType ?: "pokemon",
            resourceId = resourceId ?: 0,
            resourceName = resourceName ?: "",
            imageUrl = pokemonSpriteUrl(resourceId)
        )
    }

    private fun AuthUserDto.toUserProfile(): UserProfile {
        return UserProfile(
            id = id,
            username = username,
            xp = 0,
            character = character?.let { dto ->
                val resolvedAvatar = authApiService.normalizeMediaUrl(
                    dto.avatarFileName?.ifBlank { null } ?: dto.avatarUrlRaw
                )
                val resolvedImage = authApiService.normalizeMediaUrl(
                    dto.imageFileName?.ifBlank { null } ?: dto.imageUrlRaw
                )
                AuthCharacter(
                    id = dto.id,
                    name = dto.name,
                    avatarUrl = resolvedAvatar,
                    imageUrl = resolvedImage
                )
            }
        )
    }

    private fun InventoryItemDto.toDomain(): InventoryItem {
        return InventoryItem(
            id = id,
            resourceType = resourceType ?: "pokemon",
            resourceId = resourceId ?: 0,
            resourceName = resourceName ?: "",
            quantity = quantity,
            imageUrl = pokemonSpriteUrl(resourceId)
        )
    }

    private fun pokemonSpriteUrl(id: Int?): String {
        if (id == null || id <= 0) return ""
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
    }
}
