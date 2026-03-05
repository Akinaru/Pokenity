package fr.pokenity.pokenity.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.UserProfile
import fr.pokenity.pokenity.domain.usecase.AcceptTradeUseCase
import fr.pokenity.pokenity.domain.usecase.CancelTradeUseCase
import fr.pokenity.pokenity.domain.usecase.ConfirmTradeUseCase
import fr.pokenity.pokenity.domain.usecase.CreateTradeUseCase
import fr.pokenity.pokenity.domain.usecase.DeclineTradeUseCase
import fr.pokenity.pokenity.domain.usecase.GetMyInventoryUseCase
import fr.pokenity.pokenity.domain.usecase.GetMyTradesUseCase
import fr.pokenity.pokenity.domain.usecase.GetOpenTradesUseCase
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
    private val getMyInventoryUseCase: GetMyInventoryUseCase
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
            SocialTab.PROPOSE_TRADE -> loadInventoryAndUsers()
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

    private fun loadInventoryAndUsers() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val inventory = getMyInventoryUseCase()
                val users = getUsersUseCase()
                Pair(inventory, users)
            }
                .onSuccess { (inventory, users) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myInventory = inventory,
                        users = users
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Erreur lors du chargement"
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
        _uiState.value = _uiState.value.copy(selectedInventoryItem = item)
    }

    fun selectTargetUser(user: UserProfile?) {
        _uiState.value = _uiState.value.copy(selectedTargetUser = user)
    }

    fun createTrade() {
        val item = _uiState.value.selectedInventoryItem ?: return
        val targetUserId = _uiState.value.selectedTargetUser?.id

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { createTradeUseCase(item.id, targetUserId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Echange propose !",
                        selectedInventoryItem = null,
                        selectedTargetUser = null
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
                val repository = AppContainer.socialRepository
                return SocialViewModel(
                    getOpenTradesUseCase = GetOpenTradesUseCase(repository),
                    getMyTradesUseCase = GetMyTradesUseCase(repository),
                    createTradeUseCase = CreateTradeUseCase(repository),
                    acceptTradeUseCase = AcceptTradeUseCase(repository),
                    confirmTradeUseCase = ConfirmTradeUseCase(repository),
                    cancelTradeUseCase = CancelTradeUseCase(repository),
                    declineTradeUseCase = DeclineTradeUseCase(repository),
                    getUsersUseCase = GetUsersUseCase(repository),
                    getMyInventoryUseCase = GetMyInventoryUseCase(repository)
                ) as T
            }
        }
    }
}
