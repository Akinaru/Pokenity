package fr.pokenity.data.model

data class AuthInventoryItem(
    val resourceType: String,
    val resourceId: Int,
    val isShiny: Boolean,
    val quantity: Int
)
