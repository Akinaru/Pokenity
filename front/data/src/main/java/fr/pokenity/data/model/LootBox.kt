package fr.pokenity.data.model

data class LootBox(
    val id: String,
    val name: String,
    val pokeballImage: String,
    val totalDropRate: Double,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val entries: List<LootBoxEntry> = emptyList()
)

data class LootBoxEntry(
    val id: String,
    val resourceType: String,
    val resourceId: Int,
    val resourceName: String,
    val dropRate: Double,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
