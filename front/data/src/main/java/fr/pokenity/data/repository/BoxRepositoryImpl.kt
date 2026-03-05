package fr.pokenity.data.repository

import fr.pokenity.data.model.LootBox
import fr.pokenity.data.model.LootBoxEntry
import fr.pokenity.data.remote.box.BoxApiService
import fr.pokenity.data.remote.box.BoxDto
import fr.pokenity.data.remote.box.BoxEntryDto

class BoxRepositoryImpl internal constructor(
    private val boxApiService: BoxApiService
) : BoxRepository {

    override suspend fun getBoxes(): List<LootBox> {
        return boxApiService.getBoxes().map { it.toDomain() }
    }

    override suspend fun getBoxById(boxId: String): LootBox {
        return boxApiService.getBoxById(boxId).toDomain()
    }

    private fun BoxDto.toDomain(): LootBox {
        return LootBox(
            id = id,
            name = name,
            pokeballImage = pokeballImage,
            totalDropRate = totalDropRate ?: 0.0,
            createdAt = createdAt,
            updatedAt = updatedAt,
            entries = (entries ?: emptyList()).map { it.toDomain() }
        )
    }

    private fun BoxEntryDto.toDomain(): LootBoxEntry {
        return LootBoxEntry(
            id = id,
            resourceType = resourceType,
            resourceId = resourceId,
            resourceName = resourceName,
            dropRate = dropRate ?: 0.0,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
