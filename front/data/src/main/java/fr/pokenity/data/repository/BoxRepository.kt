package fr.pokenity.data.repository

import fr.pokenity.data.model.LootBox
import fr.pokenity.data.model.BoxOpenResult

interface BoxRepository {
    suspend fun getBoxes(): List<LootBox>
    suspend fun getBoxById(boxId: String): LootBox
    suspend fun openBox(boxId: String): BoxOpenResult
}
