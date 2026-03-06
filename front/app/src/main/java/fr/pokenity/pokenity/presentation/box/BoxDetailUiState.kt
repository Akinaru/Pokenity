package fr.pokenity.pokenity.presentation.box

import fr.pokenity.data.model.LootBox

data class BoxDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val box: LootBox? = null,
    val orderedEntries: List<BoxPokemonUi> = emptyList(),
    val rouletteItems: List<BoxPokemonUi> = emptyList(),
    val rouletteWinningIndex: Int? = null,
    val spinRequestId: Long = 0L,
    val isOpening: Boolean = false,
    val isSpinning: Boolean = false,
    val openingErrorMessage: String? = null,
    val pendingReward: BoxPokemonUi? = null,
    val showRewardDialog: Boolean = false
)

data class BoxPokemonUi(
    val resourceType: String,
    val resourceId: Int,
    val resourceName: String,
    val dropRate: Double
) {
    val imageUrl: String
        get() {
            if (resourceId <= 0) return ""
            return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/$resourceId.gif"
        }

    val gifUrl: String
        get() {
            if (resourceId <= 0) return ""
            return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/showdown/$resourceId.gif"
        }
}
