package fr.pokenity.pokenity.presentation.social

import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradePokemon
import fr.pokenity.data.model.UserProfile

enum class SocialTab {
    OPEN_TRADES,
    MY_TRADES,
    PROPOSE_TRADE,
    ACCOUNTS
}

data class SocialUiState(
    val selectedTab: SocialTab = SocialTab.OPEN_TRADES,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Current user id (to determine trade role: proposer vs recipient)
    val currentUserId: String? = null,

    // Open trades tab
    val openTrades: List<Trade> = emptyList(),

    // My trades
    val myTrades: List<Trade> = emptyList(),

    // Propose trade tab
    val myInventory: List<InventoryItem> = emptyList(),
    val selectedInventoryItems: List<InventoryItem> = emptyList(),

    // Wishlist: Pokemon requested by the proposer (1-5)
    val selectedRequestedPokemons: List<TradePokemon> = emptyList(),
    val pokemonSearchQuery: String = "",
    val pokemonSearchResults: List<PokemonSummary> = emptyList(),
    val allPokemonList: List<PokemonSummary> = emptyList(),
    val isPokemonListLoading: Boolean = false,
    val showPokedexSelector: Boolean = false,
    val showInventorySelector: Boolean = false,

    // Accounts tab
    val users: List<UserProfile> = emptyList(),

    // Accept trade dialog
    val acceptingTradeId: String? = null,

    // Incremented when a trade action changes inventory (accept/confirm),
    // observed by MainActivity to refresh AccountViewModel
    val inventoryVersion: Int = 0
)
