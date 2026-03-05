package fr.pokenity.pokenity.presentation.box

import fr.pokenity.data.model.LootBox

data class BoxListUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val boxes: List<LootBox> = emptyList()
)
