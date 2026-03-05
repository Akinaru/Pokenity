package fr.pokenity.data.model

data class InventoryItem(
    val id: String,
    val resourceType: String,
    val resourceId: Int,
    val resourceName: String,
    val quantity: Int,
    val imageUrl: String
)
