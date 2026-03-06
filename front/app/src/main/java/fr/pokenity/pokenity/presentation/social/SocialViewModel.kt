package fr.pokenity.pokenity.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.model.InventoryItem
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
                    val currentUserId = _uiState.value.currentUserId
                    trades
                        .filter { it.status == fr.pokenity.data.model.TradeStatus.WAITING_CONFIRMATION && it.proposerId == currentUserId }
                        .forEach { trade -> confirmTrade(trade.id) }
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
            _uiState.value = _uiState.value.copy(selectedInventoryItems = emptyList())
            return
        }

        val current = _uiState.value.selectedInventoryItems
        val alreadySelected = current.any { it.id == item.id }

        val updated = when {
            alreadySelected -> current.filterNot { it.id == item.id }
            current.size >= 5 -> current
            else -> current + item
        }

        _uiState.value = _uiState.value.copy(selectedInventoryItems = updated)
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
            imageUrl = pokemon.imageUrl
        )
        _uiState.value = _uiState.value.copy(
            selectedRequestedPokemons = current + tradePokemon
        )
    }

    fun removeRequestedPokemonAt(index: Int) {
        val current = _uiState.value.selectedRequestedPokemons.toMutableList()
        if (index in current.indices) current.removeAt(index)
        _uiState.value = _uiState.value.copy(selectedRequestedPokemons = current)
    }

    fun createTrade() {
        val items = _uiState.value.selectedInventoryItems
        if (items.isEmpty()) return
        val requestedPokemons = _uiState.value.selectedRequestedPokemons
        if (requestedPokemons.isEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            var createdCount = 0
            var firstError: Throwable? = null

            items.forEach { item ->
                val result = runCatching { createTradeUseCase(item.id, requestedPokemons) }
                result.onSuccess { createdCount += 1 }
                result.onFailure {
                    if (firstError == null) firstError = it
                }
            }

            if (firstError != null && createdCount == 0) {
                val error = firstError
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Erreur lors de la creation de l'echange"
                )
                return@launch
            }

            if (firstError != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "$createdCount echange(s) propose(s). Certains echanges ont echoue.",
                    selectedInventoryItems = emptyList(),
                    selectedRequestedPokemons = emptyList(),
                    showPokedexSelector = false,
                    showInventorySelector = false
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = if (createdCount == 1) "Echange propose !" else "$createdCount echanges proposes !",
                selectedInventoryItems = emptyList(),
                selectedRequestedPokemons = emptyList(),
                showPokedexSelector = false,
                showInventorySelector = false
            )
        }
    }

    fun acceptTrade(tradeId: String, offeredInventoryItemId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null, acceptingTradeId = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { acceptTradeUseCase(tradeId, offeredInventoryItemId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Echange accepte !",
                        inventoryVersion = _uiState.value.inventoryVersion + 1
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
        _uiState.value = _uiState.value.copy(acceptingTradeId = tradeId)
        if (_uiState.value.myInventory.isEmpty()) {
            loadMyInventory()
        }
    }

    fun dismissAcceptDialog() {
        _uiState.value = _uiState.value.copy(acceptingTradeId = null)
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
