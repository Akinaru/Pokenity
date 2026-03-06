package fr.pokenity.data.model

data class LootBox(
    val id: String,
    val name: String,
    val pokeballImage: String,
    val totalDropRate: Double,
    val stats: LootBoxStats = LootBoxStats(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val entries: List<LootBoxEntry> = emptyList()
)

data class LootBoxStats(
    val totalOpenings: Int = 0,
    val myOpenings: Int = 0
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

data class BoxOpenResult(
    val box: BoxOpenBox,
    val reward: BoxOpenReward,
    val drawSequence: List<BoxOpenDrawItem>,
    val inventoryItem: BoxOpenInventoryItem?,
    val boxOpening: BoxOpenHistory?,
    val user: BoxOpenUser?
)

data class BoxOpenBox(
    val id: String,
    val name: String,
    val pokeballImage: String
)

data class BoxOpenReward(
    val resourceType: String,
    val resourceId: Int,
    val resourceName: String,
    val isShiny: Boolean,
    val dropRate: Double
)

data class BoxOpenDrawItem(
    val resourceType: String,
    val resourceId: Int,
    val resourceName: String,
    val isShiny: Boolean,
    val dropRate: Double
)

data class BoxOpenInventoryItem(
    val id: String,
    val isShiny: Boolean,
    val quantity: Int,
    val lastObtainedAt: String?
)

data class BoxOpenHistory(
    val id: String,
    val isShiny: Boolean,
    val openedAt: String?
)

data class BoxOpenUser(
    val xp: Int
)
