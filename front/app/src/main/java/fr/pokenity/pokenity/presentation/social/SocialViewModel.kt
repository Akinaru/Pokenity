package fr.pokenity.pokenity.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.TradeOfferSelection
import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.model.TradePokemon
import fr.pokenity.pokenity.domain.usecase.AcceptTradeUseCase
import fr.pokenity.pokenity.domain.usecase.CancelTradeUseCase
import fr.pokenity.pokenity.domain.usecase.ConfirmTradeUseCase
import fr.pokenity.pokenity.domain.usecase.CreateTradeUseCase
import fr.pokenity.pokenity.domain.usecase.DeclineTradeUseCase
import fr.pokenity.pokenity.domain.usecase.GetMyInventoryUseCase
import fr.pokenity.pokenity.domain.usecase.GetMyTradesUseCase
import fr.pokenity.pokenity.domain.usecase.GetOpenTradesUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonListUseCase
import fr.pokenity.pokenity.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialViewModel(
    private val getOpenTradesUseCase: GetOpenTradesUseCase,
    private val getMyTradesUseCase: GetMyTradesUseCase,
    private val createTradeUseCase: CreateTradeUseCase,
    private val acceptTradeUseCase: AcceptTradeUseCase,
    private val confirmTradeUseCase: ConfirmTradeUseCase,
    private val cancelTradeUseCase: CancelTradeUseCase,
    private val declineTradeUseCase: DeclineTradeUseCase,
    private val getUsersUseCase: GetUsersUseCase,
    private val getMyInventoryUseCase: GetMyInventoryUseCase,
    private val getPokemonListUseCase: GetPokemonListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    init {
        loadOpenTrades()
    }

    fun selectTab(tab: SocialTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab, errorMessage = null, successMessage = null)
        when (tab) {
            SocialTab.OPEN_TRADES -> loadOpenTrades()
            SocialTab.MY_TRADES -> loadMyTrades()
            SocialTab.PROPOSE_TRADE -> loadInventoryAndPokemonList()
            SocialTab.ACCOUNTS -> loadUsers()
        }
    }

    fun setCurrentUserId(userId: String?) {
        _uiState.value = _uiState.value.copy(currentUserId = userId)
    }

    fun loadOpenTrades() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getOpenTradesUseCase() }
                .onSuccess { trades ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        openTrades = trades
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors du chargement des echanges"
                    )
                }
        }
    }

    fun loadMyTrades() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getMyTradesUseCase() }
                .onSuccess { trades ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myTrades = trades
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors du chargement de mes echanges"
                    )
                }
        }
    }

    private fun loadInventoryAndPokemonList() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val inventory = getMyInventoryUseCase()
                inventory
            }
                .onSuccess { inventory ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myInventory = inventory
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors du chargement"
                    )
                }
        }
        // Load full Pokemon list for autocomplete if not already loaded
        if (_uiState.value.allPokemonList.isEmpty()) {
            loadAllPokemonList()
        }
    }

    private fun loadAllPokemonList() {
        _uiState.value = _uiState.value.copy(isPokemonListLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getPokemonListUseCase(limit = 1302, offset = 0) }
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        isPokemonListLoading = false,
                        allPokemonList = list
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isPokemonListLoading = false
                    )
                }
        }
    }

    private fun loadUsers() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getUsersUseCase() }
                .onSuccess { users ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        users = users
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors du chargement des utilisateurs"
                    )
                }
        }
    }

    fun selectInventoryItem(item: InventoryItem?) {
        if (item == null) {
            _uiState.value = _uiState.value.copy(
                selectedInventoryItems = emptyList(),
                selectedOfferedQuantities = emptyMap()
            )
            return
        }

        val current = _uiState.value.selectedInventoryItems
        val currentQuantities = _uiState.value.selectedOfferedQuantities
        val alreadySelected = current.any { it.id == item.id }

        val updated = when {
            alreadySelected -> current.filterNot { it.id == item.id }
            current.size >= 5 -> current
            else -> current + item
        }

        val updatedQuantities = currentQuantities.toMutableMap()
        if (alreadySelected) {
            updatedQuantities.remove(item.id)
        } else if (current.size < 5) {
            updatedQuantities[item.id] = (updatedQuantities[item.id] ?: 1).coerceAtMost(item.quantity)
        }

        _uiState.value = _uiState.value.copy(
            selectedInventoryItems = updated,
            selectedOfferedQuantities = updatedQuantities
        )
    }

    fun updateOfferedQuantity(itemId: String, quantity: Int) {
        val selectedItem = _uiState.value.selectedInventoryItems.find { it.id == itemId } ?: return
        val bounded = quantity.coerceIn(1, selectedItem.quantity)
        val updated = _uiState.value.selectedOfferedQuantities.toMutableMap()
        updated[itemId] = bounded
        _uiState.value = _uiState.value.copy(selectedOfferedQuantities = updated)
    }

    fun openInventorySelector() {
        _uiState.value = _uiState.value.copy(showInventorySelector = true)
    }

    fun closeInventorySelector() {
        _uiState.value = _uiState.value.copy(showInventorySelector = false)
    }

    fun openPokedexSelector() {
        _uiState.value = _uiState.value.copy(showPokedexSelector = true)
    }

    fun closePokedexSelector() {
        _uiState.value = _uiState.value.copy(showPokedexSelector = false)
    }

    // --- Pokemon search for wishlist ---

    fun updatePokemonSearchQuery(query: String) {
        val allList = _uiState.value.allPokemonList
        val alreadySelected = _uiState.value.selectedRequestedPokemons.map { it.resourceId }.toSet()

        val results = if (query.isBlank()) {
            emptyList()
        } else {
            val lowerQuery = query.lowercase().trim()
            val asNumber = lowerQuery.toIntOrNull()
            allList.filter { pokemon ->
                pokemon.id !in alreadySelected && (
                    pokemon.name.lowercase().contains(lowerQuery) ||
                    (asNumber != null && pokemon.id == asNumber)
                )
            }.take(10)
        }

        _uiState.value = _uiState.value.copy(
            pokemonSearchQuery = query,
            pokemonSearchResults = results
        )
    }

    fun addRequestedPokemon(pokemon: PokemonSummary) {
        val current = _uiState.value.selectedRequestedPokemons
        if (current.size >= 5) return
        if (current.any { it.resourceId == pokemon.id }) return

        val tradePokemon = TradePokemon(
            resourceType = "POKEMON",
            resourceId = pokemon.id,
            resourceName = pokemon.name,
            isShiny = false,
            quantity = 1,
            imageUrl = pokemon.imageUrl
        )
        _uiState.value = _uiState.value.copy(
            selectedRequestedPokemons = current + tradePokemon,
            selectedRequestedQuantities = _uiState.value.selectedRequestedQuantities + (pokemon.id to 1)
        )
    }

    fun removeRequestedPokemonAt(index: Int) {
        val current = _uiState.value.selectedRequestedPokemons.toMutableList()
        var removedResourceId: Int? = null
        if (index in current.indices) {
            removedResourceId = current[index].resourceId
            current.removeAt(index)
        }
        val updatedQuantities = _uiState.value.selectedRequestedQuantities.toMutableMap()
        if (removedResourceId != null) {
            updatedQuantities.remove(removedResourceId)
        }
        _uiState.value = _uiState.value.copy(
            selectedRequestedPokemons = current,
            selectedRequestedQuantities = updatedQuantities
        )
    }

    fun updateRequestedQuantity(resourceId: Int, quantity: Int) {
        val exists = _uiState.value.selectedRequestedPokemons.any { it.resourceId == resourceId }
        if (!exists) return
        val bounded = quantity.coerceIn(1, 999)
        val updated = _uiState.value.selectedRequestedQuantities.toMutableMap()
        updated[resourceId] = bounded
        _uiState.value = _uiState.value.copy(selectedRequestedQuantities = updated)
    }

    fun createTrade() {
        val items = _uiState.value.selectedInventoryItems
        if (items.isEmpty()) return
        val offeredQuantities = _uiState.value.selectedOfferedQuantities
        val requestedQuantities = _uiState.value.selectedRequestedQuantities
        val requestedPokemons = _uiState.value.selectedRequestedPokemons.map { pokemon ->
            pokemon.copy(quantity = (requestedQuantities[pokemon.resourceId] ?: pokemon.quantity).coerceAtLeast(1))
        }
        if (requestedPokemons.isEmpty()) return

        val offeredSelections = items.map { item ->
            TradeOfferSelection(
                inventoryItemId = item.id,
                quantity = (offeredQuantities[item.id] ?: 1).coerceIn(1, item.quantity)
            )
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { createTradeUseCase(offeredSelections, requestedPokemons) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Annonce d'echange creee !",
                        selectedInventoryItems = emptyList(),
                        selectedOfferedQuantities = emptyMap(),
                        selectedRequestedPokemons = emptyList(),
                        selectedRequestedQuantities = emptyMap(),
                        showPokedexSelector = false,
                        showInventorySelector = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors de la creation de l'echange"
                    )
                }
        }
    }

    fun acceptTrade(tradeId: String) {
        val state = _uiState.value
        val trade = state.openTrades.find { it.id == tradeId } ?: return

        val selectedOffered = trade.offeredPokemons.filter { pokemon ->
            val key = "${pokemon.resourceId}:${pokemon.isShiny}"
            state.acceptDialogSelectedOffered.contains(key)
        }
        val givenPokemons = state.acceptDialogGivenItems.map { (inventoryItemId, quantity) ->
            TradeOfferSelection(inventoryItemId = inventoryItemId, quantity = quantity)
        }

        if (selectedOffered.isEmpty() || givenPokemons.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Veuillez selectionner au moins un pokemon a recevoir et un a donner."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null, acceptingTradeId = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { acceptTradeUseCase(tradeId, selectedOffered, givenPokemons) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Echange accepte. En attente de confirmation du proposant.",
                        inventoryVersion = _uiState.value.inventoryVersion + 1,
                        acceptDialogSelectedOffered = emptySet(),
                        acceptDialogGivenItems = emptyMap()
                    )
                    loadOpenTrades()
                    loadMyInventory()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors de l'acceptation"
                    )
                }
        }
    }

    fun confirmTrade(tradeId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { confirmTradeUseCase(tradeId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Echange confirme !",
                        inventoryVersion = _uiState.value.inventoryVersion + 1
                    )
                    loadMyTrades()
                    loadMyInventory()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors de la confirmation"
                    )
                }
        }
    }

    fun cancelTrade(tradeId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { cancelTradeUseCase(tradeId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Echange annule."
                    )
                    loadMyTrades()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors de l'annulation"
                    )
                }
        }
    }

    fun declineTrade(tradeId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { declineTradeUseCase(tradeId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Echange refuse."
                    )
                    loadMyTrades()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors du refus"
                    )
                }
        }
    }

    fun showAcceptDialog(tradeId: String) {
        val trade = _uiState.value.openTrades.find { it.id == tradeId }
        val allOfferedKeys = trade?.offeredPokemons?.map { "${it.resourceId}:${it.isShiny}" }?.toSet() ?: emptySet()
        _uiState.value = _uiState.value.copy(
            acceptingTradeId = tradeId,
            acceptDialogSelectedOffered = allOfferedKeys,
            acceptDialogGivenItems = emptyMap()
        )
        if (_uiState.value.myInventory.isEmpty()) {
            loadMyInventory()
        }
    }

    fun dismissAcceptDialog() {
        _uiState.value = _uiState.value.copy(
            acceptingTradeId = null,
            acceptDialogSelectedOffered = emptySet(),
            acceptDialogGivenItems = emptyMap()
        )
    }

    fun toggleOfferedSelection(key: String) {
        val current = _uiState.value.acceptDialogSelectedOffered.toMutableSet()
        if (current.contains(key)) {
            if (current.size > 1) current.remove(key)
        } else {
            current.add(key)
        }
        _uiState.value = _uiState.value.copy(acceptDialogSelectedOffered = current)
    }

    fun toggleGivenItem(inventoryItemId: String, suggestedQty: Int) {
        val current = _uiState.value.acceptDialogGivenItems.toMutableMap()
        if (current.containsKey(inventoryItemId)) {
            current.remove(inventoryItemId)
        } else {
            val inventoryItem = _uiState.value.myInventory.find { it.id == inventoryItemId }
            val maxQty = inventoryItem?.quantity ?: suggestedQty
            current[inventoryItemId] = suggestedQty.coerceIn(1, maxQty)
        }
        _uiState.value = _uiState.value.copy(acceptDialogGivenItems = current)
    }

    fun updateGivenQuantity(inventoryItemId: String, quantity: Int) {
        val inventoryItem = _uiState.value.myInventory.find { it.id == inventoryItemId } ?: return
        val bounded = quantity.coerceIn(1, inventoryItem.quantity)
        val current = _uiState.value.acceptDialogGivenItems.toMutableMap()
        current[inventoryItemId] = bounded
        _uiState.value = _uiState.value.copy(acceptDialogGivenItems = current)
    }

    private fun loadMyInventory() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getMyInventoryUseCase() }
                .onSuccess { inventory ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myInventory = inventory
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors du chargement de l'inventaire"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val socialRepository = AppContainer.socialRepository
                val pokemonRepository = AppContainer.pokemonRepository
                return SocialViewModel(
                    getOpenTradesUseCase = GetOpenTradesUseCase(socialRepository),
                    getMyTradesUseCase = GetMyTradesUseCase(socialRepository),
                    createTradeUseCase = CreateTradeUseCase(socialRepository),
                    acceptTradeUseCase = AcceptTradeUseCase(socialRepository),
                    confirmTradeUseCase = ConfirmTradeUseCase(socialRepository),
                    cancelTradeUseCase = CancelTradeUseCase(socialRepository),
                    declineTradeUseCase = DeclineTradeUseCase(socialRepository),
                    getUsersUseCase = GetUsersUseCase(socialRepository),
                    getMyInventoryUseCase = GetMyInventoryUseCase(socialRepository),
                    getPokemonListUseCase = GetPokemonListUseCase(pokemonRepository)
                ) as T
            }
        }
    }
}
