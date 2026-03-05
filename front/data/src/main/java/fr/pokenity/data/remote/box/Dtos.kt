package fr.pokenity.data.remote.box

import com.google.gson.annotations.SerializedName

internal data class BoxesResponseDto(
    @SerializedName("boxes") val boxes: List<BoxDto>?
)

internal data class BoxResponseDto(
    @SerializedName("box") val box: BoxDto?
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

internal data class BoxErrorDto(
    @SerializedName("error") val error: String?
)
