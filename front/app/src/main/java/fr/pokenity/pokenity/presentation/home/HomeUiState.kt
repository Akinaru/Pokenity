package fr.pokenity.pokenity.presentation.home

import fr.pokenity.data.model.LootBox

data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val latestBoxes: List<LootBox> = emptyList()
)
