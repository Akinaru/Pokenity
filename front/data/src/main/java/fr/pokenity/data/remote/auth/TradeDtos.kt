package fr.pokenity.data.remote.auth

import com.google.gson.annotations.SerializedName

// --- Response wrappers ---

internal data class TradeListResponseDto(
    @SerializedName("trades") val trades: List<TradeDto>
)

internal data class TradeResponseDto(
    @SerializedName("trade") val trade: TradeDto
)

// --- Trade ---

internal data class TradeDto(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("proposerId") val proposerId: String,
    @SerializedName("recipientId") val recipientId: String?,
    @SerializedName("proposer") val proposer: TradeUserLightDto?,
    @SerializedName("recipient") val recipient: TradeUserLightDto?,
    @SerializedName("offeredPokemons") val offeredPokemons: List<TradePokemonDto>?,
    @SerializedName("offeredPokemon") val offeredPokemon: TradePokemonDto?,
    @SerializedName("receivedPokemon") val receivedPokemon: TradePokemonDto?,
    @SerializedName("requestedPokemons") val requestedPokemons: List<TradePokemonDto>?,
    @SerializedName("acceptedAt") val acceptedAt: String?,
    @SerializedName("confirmedAt") val confirmedAt: String?,
    @SerializedName("completedAt") val completedAt: String?,
    @SerializedName("canceledAt") val canceledAt: String?,
    @SerializedName("declinedAt") val declinedAt: String?,
    @SerializedName("expiresAt") val expiresAt: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

internal data class TradeUserLightDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("xp") val xp: Int?
)

internal data class TradePokemonDto(
    @SerializedName("resourceType") val resourceType: String?,
    @SerializedName("resourceId") val resourceId: Int?,
    @SerializedName("resourceName") val resourceName: String?,
    @SerializedName("isShiny") val isShiny: Boolean? = null,
    @SerializedName("quantity") val quantity: Int? = null
)

// --- Request bodies ---

internal data class CreateTradeRequestBody(
    @SerializedName("offeredPokemons") val offeredPokemons: List<OfferedPokemonBody>,
    @SerializedName("requestedPokemons") val requestedPokemons: List<RequestedPokemonBody>
)

internal data class OfferedPokemonBody(
    @SerializedName("inventoryItemId") val inventoryItemId: String,
    @SerializedName("quantity") val quantity: Int
)

internal data class RequestedPokemonBody(
    @SerializedName("resourceId") val resourceId: Int,
    @SerializedName("resourceName") val resourceName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("isShiny") val isShiny: Boolean = false
)

internal data class AcceptTradeRequestBody(
    @SerializedName("selectedOffered") val selectedOffered: List<SelectedOfferedBody>,
    @SerializedName("givenPokemons") val givenPokemons: List<GivenPokemonBody>
)

internal data class SelectedOfferedBody(
    @SerializedName("resourceId") val resourceId: Int,
    @SerializedName("isShiny") val isShiny: Boolean,
    @SerializedName("quantity") val quantity: Int
)

internal data class GivenPokemonBody(
    @SerializedName("inventoryItemId") val inventoryItemId: String,
    @SerializedName("quantity") val quantity: Int
)

// --- Inventory ---

internal data class InventoryResponseDto(
    @SerializedName("user") val user: AuthUserDto?,
    @SerializedName("inventory") val inventory: List<InventoryItemDto>
)

internal data class InventoryItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("resourceType") val resourceType: String?,
    @SerializedName("resourceId") val resourceId: Int?,
    @SerializedName("resourceName") val resourceName: String?,
    @SerializedName("isShiny") val isShiny: Boolean? = null,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("firstObtainedAt") val firstObtainedAt: String?,
    @SerializedName("lastObtainedAt") val lastObtainedAt: String?
)

// --- User detail wrapper ---

internal data class UserDetailResponseDto(
    @SerializedName("user") val user: AuthUserDto
)
