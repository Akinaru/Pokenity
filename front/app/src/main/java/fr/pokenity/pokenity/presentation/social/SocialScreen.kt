package fr.pokenity.pokenity.presentation.social

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.PokemonSummary
import fr.pokenity.data.model.TradePokemon

@Composable
fun SocialScreen(
    uiState: SocialUiState,
    onSelectTab: (SocialTab) -> Unit,
    onAcceptTrade: (tradeId: String) -> Unit,
    onAcceptTradeWithItem: (tradeId: String, inventoryItemId: String) -> Unit,
    onDismissAcceptDialog: () -> Unit,
    onConfirmTrade: (tradeId: String) -> Unit,
    onCancelTrade: (tradeId: String) -> Unit,
    onDeclineTrade: (tradeId: String) -> Unit,
    onRefreshMyTrades: () -> Unit,
    onSelectInventoryItem: (InventoryItem?) -> Unit,
    onPokemonSearchQueryChange: (String) -> Unit,
    onAddRequestedPokemon: (PokemonSummary) -> Unit,
    onRemoveRequestedPokemon: (TradePokemon) -> Unit,
    onCreateTrade: () -> Unit,
    onRefreshOpenTrades: () -> Unit,
    onClearMessages: () -> Unit,
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

    Column(modifier = modifier.fillMaxSize()) {
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
                    onSelectInventoryItemForAccept = { tradeId, item ->
                        onAcceptTradeWithItem(tradeId, item.id)
                    },
                    onDismissAcceptDialog = onDismissAcceptDialog,
                    onRefresh = onRefreshOpenTrades
                )
            }

            SocialTab.MY_TRADES -> {
                MyTradesScreen(
                    uiState = uiState,
                    onConfirmTrade = onConfirmTrade,
                    onCancelTrade = onCancelTrade,
                    onDeclineTrade = onDeclineTrade,
                    onRefresh = onRefreshMyTrades
                )
            }

            SocialTab.PROPOSE_TRADE -> {
                ProposeTradeScreen(
                    uiState = uiState,
                    onSelectInventoryItem = onSelectInventoryItem,
                    onPokemonSearchQueryChange = onPokemonSearchQueryChange,
                    onAddRequestedPokemon = onAddRequestedPokemon,
                    onRemoveRequestedPokemon = onRemoveRequestedPokemon,
                    onCreateTrade = onCreateTrade
                )
            }

            SocialTab.ACCOUNTS -> {
                UsersListScreen(uiState = uiState)
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
