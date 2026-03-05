package fr.pokenity.pokenity.presentation.social

import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.UserProfile

enum class SocialTab {
    OPEN_TRADES,
    PROPOSE_TRADE,
    ACCOUNTS
}

data class SocialUiState(
    val selectedTab: SocialTab = SocialTab.OPEN_TRADES,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Open trades tab
    val openTrades: List<Trade> = emptyList(),

    // My trades
    val myTrades: List<Trade> = emptyList(),

    // Propose trade tab
    val myInventory: List<InventoryItem> = emptyList(),
    val selectedInventoryItem: InventoryItem? = null,
    val selectedTargetUser: UserProfile? = null,

    // Accounts tab
    val users: List<UserProfile> = emptyList()
)
