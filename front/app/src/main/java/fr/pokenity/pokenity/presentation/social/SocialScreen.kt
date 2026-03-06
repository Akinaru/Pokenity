package fr.pokenity.pokenity.presentation.social

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.PokemonFilterOption
import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.pokenity.presentation.pokedex.PokedexSection
import fr.pokenity.pokenity.presentation.pokedex.PokedexUiState

@Composable
fun SocialScreen(
    uiState: SocialUiState,
    pokedexUiState: PokedexUiState,
    onSelectTab: (SocialTab) -> Unit,
    onAcceptTrade: (tradeId: String) -> Unit,
    onConfirmAccept: (tradeId: String) -> Unit,
    onDismissAcceptDialog: () -> Unit,
    onToggleOfferedSelection: (key: String) -> Unit,
    onToggleGivenItem: (inventoryItemId: String, suggestedQty: Int) -> Unit,
    onUpdateGivenQuantity: (inventoryItemId: String, quantity: Int) -> Unit,
    onConfirmTrade: (tradeId: String) -> Unit,
    onCancelTrade: (tradeId: String) -> Unit,
    onDeclineTrade: (tradeId: String) -> Unit,
    onRefreshMyTrades: () -> Unit,
    onSelectInventoryItem: (InventoryItem?) -> Unit,
    onUpdateOfferedQuantity: (itemId: String, quantity: Int) -> Unit,
    onAddRequestedPokemon: (PokemonSummary) -> Unit,
    onUpdateRequestedQuantity: (resourceId: Int, quantity: Int) -> Unit,
    onRemoveRequestedPokemonAt: (Int) -> Unit,
    onOpenInventorySelector: () -> Unit,
    onCloseInventorySelector: () -> Unit,
    onOpenPokedexSelector: () -> Unit,
    onClosePokedexSelector: () -> Unit,
    onCreateTrade: () -> Unit,
    onRefreshOpenTrades: () -> Unit,
    onClearMessages: () -> Unit,
    // Pokedex filter callbacks
    onFilterCategorySelected: (PokedexSection) -> Unit,
    onTypeClicked: (PokemonFilterOption) -> Unit,
    onGenerationClicked: (PokemonFilterOption) -> Unit,
    onAbilityClicked: (PokemonFilterOption) -> Unit,
    onHabitatClicked: (PokemonFilterOption) -> Unit,
    onRegionClicked: (PokemonFilterOption) -> Unit,
    onShapeClicked: (PokemonFilterOption) -> Unit,
    onClearTypeFilter: () -> Unit,
    onClearGenerationFilter: () -> Unit,
    onClearAbilityFilter: () -> Unit,
    onClearHabitatFilter: () -> Unit,
    onClearRegionFilter: () -> Unit,
    onClearShapeFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Social",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                TabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
                ) {
                    SocialTab.entries.forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { onSelectTab(tab) },
                            text = {
                                Text(
                                    text = when (tab) {
                                        SocialTab.OPEN_TRADES -> "Echanges"
                                        SocialTab.MY_TRADES -> "Mes echanges"
                                        SocialTab.PROPOSE_TRADE -> "Proposer"
                                        SocialTab.ACCOUNTS -> "Comptes"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState.selectedTab) {
                SocialTab.OPEN_TRADES -> {
                    OpenTradesScreen(
                        uiState = uiState,
                        onAcceptTrade = onAcceptTrade,
                        onConfirmAccept = onConfirmAccept,
                        onDismissAcceptDialog = onDismissAcceptDialog,
                        onToggleOfferedSelection = onToggleOfferedSelection,
                        onToggleGivenItem = onToggleGivenItem,
                        onUpdateGivenQuantity = onUpdateGivenQuantity,
                        onRefresh = onRefreshOpenTrades,
                        modifier = Modifier.weight(1f)
                    )
                }

                SocialTab.MY_TRADES -> {
                    MyTradesScreen(
                        uiState = uiState,
                        onConfirmTrade = onConfirmTrade,
                        onCancelTrade = onCancelTrade,
                        onDeclineTrade = onDeclineTrade,
                        onRefresh = onRefreshMyTrades,
                        modifier = Modifier.weight(1f)
                    )
                }

                SocialTab.PROPOSE_TRADE -> {
                    ProposeTradeScreen(
                        uiState = uiState,
                        pokedexUiState = pokedexUiState,
                        onSelectInventoryItem = onSelectInventoryItem,
                        onUpdateOfferedQuantity = onUpdateOfferedQuantity,
                        onAddRequestedPokemon = onAddRequestedPokemon,
                        onUpdateRequestedQuantity = onUpdateRequestedQuantity,
                        onRemoveRequestedPokemonAt = onRemoveRequestedPokemonAt,
                        onOpenInventorySelector = onOpenInventorySelector,
                        onCloseInventorySelector = onCloseInventorySelector,
                        onOpenPokedexSelector = onOpenPokedexSelector,
                        onClosePokedexSelector = onClosePokedexSelector,
                        onCreateTrade = onCreateTrade,
                        onFilterCategorySelected = onFilterCategorySelected,
                        onTypeClicked = onTypeClicked,
                        onGenerationClicked = onGenerationClicked,
                        onAbilityClicked = onAbilityClicked,
                        onHabitatClicked = onHabitatClicked,
                        onRegionClicked = onRegionClicked,
                        onShapeClicked = onShapeClicked,
                        onClearTypeFilter = onClearTypeFilter,
                        onClearGenerationFilter = onClearGenerationFilter,
                        onClearAbilityFilter = onClearAbilityFilter,
                        onClearHabitatFilter = onClearHabitatFilter,
                        onClearRegionFilter = onClearRegionFilter,
                        onClearShapeFilter = onClearShapeFilter,
                        modifier = Modifier.weight(1f)
                    )
                }

                SocialTab.ACCOUNTS -> {
                    UsersListScreen(uiState = uiState, modifier = Modifier.weight(1f))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
