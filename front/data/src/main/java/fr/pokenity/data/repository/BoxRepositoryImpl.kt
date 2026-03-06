package fr.pokenity.data.repository

import fr.pokenity.data.core.AuthSessionState
import fr.pokenity.data.model.BoxOpenBox
import fr.pokenity.data.model.BoxOpenDrawItem
import fr.pokenity.data.model.BoxOpenHistory
import fr.pokenity.data.model.BoxOpenInventoryItem
import fr.pokenity.data.model.BoxOpenResult
import fr.pokenity.data.model.BoxOpenReward
import fr.pokenity.data.model.BoxOpenUser
import fr.pokenity.data.model.LootBox
import fr.pokenity.data.model.LootBoxEntry
import fr.pokenity.data.remote.box.BoxApiService
import fr.pokenity.data.remote.box.BoxDto
import fr.pokenity.data.remote.box.BoxEntryDto
import fr.pokenity.data.remote.box.OpenBoxResponseDto

class BoxRepositoryImpl internal constructor(
    private val boxApiService: BoxApiService
) : BoxRepository {

    private fun requireToken(): String {
        return AuthSessionState.token.value
            ?: throw IllegalStateException("Aucune session active.")
    }

    override suspend fun getBoxes(): List<LootBox> {
        return boxApiService.getBoxes().map { it.toDomain() }
    }

    override suspend fun getBoxById(boxId: String): LootBox {
        return boxApiService.getBoxById(boxId).toDomain()
    }

    override suspend fun openBox(boxId: String): BoxOpenResult {
        val token = requireToken()
        return boxApiService.openBox(token = token, boxId = boxId).toDomain()
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

    private fun OpenBoxResponseDto.toDomain(): BoxOpenResult {
        val rewardDto = reward ?: throw IllegalStateException("Recompense manquante dans la reponse.")
        val boxDto = box
        val resolvedShiny = rewardDto.isShiny
            ?: inventoryItem?.isShiny
            ?: boxOpening?.isShiny
            ?: false
        return BoxOpenResult(
            box = BoxOpenBox(
                id = boxDto?.id ?: "",
                name = boxDto?.name ?: "",
                pokeballImage = boxDto?.pokeballImage ?: ""
            ),
            reward = BoxOpenReward(
                resourceType = rewardDto.resourceType ?: "pokemon",
                resourceId = rewardDto.resourceId ?: 0,
                resourceName = rewardDto.resourceName ?: "pokemon",
                isShiny = resolvedShiny,
                dropRate = rewardDto.dropRate ?: 0.0
            ),
            drawSequence = (drawSequence ?: emptyList()).map { drawItem ->
                BoxOpenDrawItem(
                    resourceType = drawItem.resourceType ?: "pokemon",
                    resourceId = drawItem.resourceId ?: 0,
                    resourceName = drawItem.resourceName ?: "pokemon",
                    isShiny = drawItem.isShiny ?: false,
                    dropRate = drawItem.dropRate ?: 0.0
                )
            },
            inventoryItem = inventoryItem?.let { inventory ->
                BoxOpenInventoryItem(
                    id = inventory.id ?: "",
                    isShiny = inventory.isShiny ?: resolvedShiny,
                    quantity = inventory.quantity ?: 0,
                    lastObtainedAt = inventory.lastObtainedAt
                )
            },
            boxOpening = boxOpening?.let { opening ->
                BoxOpenHistory(
                    id = opening.id ?: "",
                    isShiny = opening.isShiny ?: resolvedShiny,
                    openedAt = opening.openedAt
                )
            },
            user = user?.let { openUser ->
                BoxOpenUser(xp = openUser.xp ?: 0)
            }
        )
    }
}
