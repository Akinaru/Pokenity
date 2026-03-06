package fr.pokenity.data.repository

import fr.pokenity.data.core.AuthSessionState
import fr.pokenity.data.model.AuthCharacter
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.TradeOfferSelection
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
import fr.pokenity.data.remote.auth.OfferedPokemonBody
import fr.pokenity.data.remote.auth.RequestedPokemonBody
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

    override suspend fun createTrade(offeredPokemons: List<TradeOfferSelection>, requestedPokemons: List<TradePokemon>): Trade {
        val token = requireToken()
        val body = CreateTradeRequestBody(
            offeredPokemons = offeredPokemons.map { offered ->
                OfferedPokemonBody(
                    inventoryItemId = offered.inventoryItemId,
                    quantity = offered.quantity
                )
            },
            requestedPokemons = requestedPokemons.map { rp ->
                RequestedPokemonBody(
                    resourceId = rp.resourceId,
                    resourceName = rp.resourceName,
                    quantity = rp.quantity,
                    isShiny = rp.isShiny
                )
            }
        )
        return authApiService.createTrade(token, body).toDomain()
    }

    override suspend fun acceptTrade(tradeId: String): Trade {
        val token = requireToken()
        val body = AcceptTradeRequestBody()
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
            offeredPokemons = offeredPokemons?.map { it.toDomain() } ?: emptyList(),
            offeredPokemon = offeredPokemon?.toDomain(),
            receivedPokemon = receivedPokemon?.toDomain(),
            requestedPokemons = requestedPokemons?.map { it.toDomain() } ?: emptyList(),
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
            isShiny = isShiny == true,
            quantity = quantity ?: 1,
            imageUrl = pokemonSpriteUrl(resourceId, isShiny == true)
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
            isShiny = isShiny == true,
            quantity = quantity,
            imageUrl = pokemonSpriteUrl(resourceId, isShiny == true)
        )
    }

    private fun pokemonSpriteUrl(id: Int?, isShiny: Boolean = false): String {
        if (id == null || id <= 0) return ""
        val shinySegment = if (isShiny) "shiny/" else ""
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${shinySegment}$id.png"
    }
}
