package fr.pokenity.pokenity.presentation.box

import fr.pokenity.data.model.LootBox

data class BoxDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val box: LootBox? = null
)
