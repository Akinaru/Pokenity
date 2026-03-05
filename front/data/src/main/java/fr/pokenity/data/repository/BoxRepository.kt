package fr.pokenity.data.repository

import fr.pokenity.data.model.LootBox

interface BoxRepository {
    suspend fun getBoxes(): List<LootBox>
    suspend fun getBoxById(boxId: String): LootBox
}
