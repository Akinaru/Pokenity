package fr.pokenity.data.remote.box

import com.google.gson.annotations.SerializedName

internal data class BoxesResponseDto(
    @SerializedName("boxes") val boxes: List<BoxDto>?
)

internal data class BoxResponseDto(
    @SerializedName("box") val box: BoxDto?
)

internal data class OpenBoxResponseDto(
    @SerializedName("box") val box: OpenBoxInfoDto?,
    @SerializedName("reward") val reward: OpenBoxRewardDto?,
    @SerializedName("drawSequence") val drawSequence: List<OpenBoxDrawItemDto>? = null,
    @SerializedName("inventoryItem") val inventoryItem: OpenBoxInventoryItemDto?,
    @SerializedName("boxOpening") val boxOpening: OpenBoxHistoryDto?,
    @SerializedName("user") val user: OpenBoxUserDto?
)

internal data class BoxDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("pokeballImage") val pokeballImage: String,
    @SerializedName("totalDropRate") val totalDropRate: Double? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("entries") val entries: List<BoxEntryDto>? = null
)

internal data class BoxEntryDto(
    @SerializedName("id") val id: String,
    @SerializedName("resourceType") val resourceType: String,
    @SerializedName("resourceId") val resourceId: Int,
    @SerializedName("resourceName") val resourceName: String,
    @SerializedName("dropRate") val dropRate: Double? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

internal data class OpenBoxInfoDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("pokeballImage") val pokeballImage: String? = null
)

internal data class OpenBoxRewardDto(
    @SerializedName("resourceType") val resourceType: String? = null,
    @SerializedName("resourceId") val resourceId: Int? = null,
    @SerializedName("resourceName") val resourceName: String? = null,
    @SerializedName("isShiny") val isShiny: Boolean? = null,
    @SerializedName("dropRate") val dropRate: Double? = null
)

internal data class OpenBoxDrawItemDto(
    @SerializedName("resourceType") val resourceType: String? = null,
    @SerializedName("resourceId") val resourceId: Int? = null,
    @SerializedName("resourceName") val resourceName: String? = null,
    @SerializedName("dropRate") val dropRate: Double? = null,
    @SerializedName("isShiny") val isShiny: Boolean? = null
)

internal data class OpenBoxInventoryItemDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("isShiny") val isShiny: Boolean? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("lastObtainedAt") val lastObtainedAt: String? = null
)

internal data class OpenBoxHistoryDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("isShiny") val isShiny: Boolean? = null,
    @SerializedName("openedAt") val openedAt: String? = null
)

internal data class OpenBoxUserDto(
    @SerializedName("xp") val xp: Int? = null
)

internal data class BoxErrorDto(
    @SerializedName("error") val error: String?
)
