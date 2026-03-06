package fr.pokenity.data.model

data class TradePokemon(
    val resourceType: String,
    val resourceId: Int,
    val resourceName: String,
    val isShiny: Boolean,
    val imageUrl: String
)
