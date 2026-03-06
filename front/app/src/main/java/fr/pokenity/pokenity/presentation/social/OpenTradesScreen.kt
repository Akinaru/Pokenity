package fr.pokenity.pokenity.presentation.social

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradeStatus

@Composable
fun OpenTradesScreen(
    uiState: SocialUiState,
    onAcceptTrade: (tradeId: String) -> Unit,
    onSelectInventoryItemForAccept: (tradeId: String, inventoryItem: InventoryItem) -> Unit,
    onDismissAcceptDialog: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Inventory picker dialog for accepting a trade
    val acceptingTradeId = uiState.acceptingTradeId
    if (acceptingTradeId != null) {
        // Find the trade being accepted to filter inventory by requestedPokemons
        val acceptingTrade = uiState.openTrades.find { it.id == acceptingTradeId }
        val requestedIds = acceptingTrade?.requestedPokemons?.map { it.resourceId }?.toSet()

        // Filter inventory: only show items whose resourceId matches a requested Pokemon
        val filteredInventory = if (requestedIds != null && requestedIds.isNotEmpty()) {
            uiState.myInventory.filter { it.resourceId in requestedIds }
        } else {
            uiState.myInventory
        }

        AcceptTradeDialog(
            inventory = filteredInventory,
            isLoading = uiState.isLoading && uiState.myInventory.isEmpty(),
            hasNoMatchingPokemon = requestedIds != null && requestedIds.isNotEmpty() && filteredInventory.isEmpty() && uiState.myInventory.isNotEmpty(),
            onItemSelected = { item -> onSelectInventoryItemForAccept(acceptingTradeId, item) },
            onDismiss = onDismissAcceptDialog
        )
    }

    if (uiState.isLoading && uiState.openTrades.isEmpty() && acceptingTradeId == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.openTrades.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Aucun echange disponible",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                PrimaryButton(onClick = onRefresh) {
                    Text("Rafraichir")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        items(uiState.openTrades, key = { it.id }) { trade ->
            TradeCard(
                trade = trade,
                actionLabel = "Accepter",
                onAction = { onAcceptTrade(trade.id) }
            )
        }
        item { Spacer(modifier = Modifier.height(4.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TradeCard(
    trade: Trade,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trade.proposer?.username ?: "Inconnu",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TradeStatusBadge(status = trade.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Offered pokemon
                val offered = trade.offeredPokemon
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (offered != null) {
                        AsyncImage(
                            model = offered.imageUrl,
                            contentDescription = offered.resourceName,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = offered.resourceName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text("?", style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        text = "Propose",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = " ↔ ",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Received pokemon if accepted, otherwise show requested pokemons
                val received = trade.receivedPokemon
                if (received != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = received.imageUrl,
                            contentDescription = received.resourceName,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = received.resourceName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Recu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (trade.requestedPokemons.isNotEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            trade.requestedPokemons.forEach { pokemon ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(
                                        model = pokemon.imageUrl,
                                        contentDescription = pokemon.resourceName,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = pokemon.resourceName,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Souhaite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("?", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "Souhaite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                PrimaryButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(actionLabel)
                }
            }

            actions?.invoke()
        }
    }
}

@Composable
fun TradeStatusBadge(status: TradeStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        TradeStatus.PENDING -> "En attente" to MaterialTheme.colorScheme.tertiary
        TradeStatus.WAITING_CONFIRMATION -> "A confirmer" to MaterialTheme.colorScheme.secondary
        TradeStatus.COMPLETED -> "Termine" to MaterialTheme.colorScheme.primary
        TradeStatus.CANCELED -> "Annule" to MaterialTheme.colorScheme.error
        TradeStatus.DECLINED -> "Refuse" to MaterialTheme.colorScheme.error
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}

@Composable
private fun AcceptTradeDialog(
    inventory: List<InventoryItem>,
    isLoading: Boolean,
    hasNoMatchingPokemon: Boolean,
    onItemSelected: (InventoryItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir un Pokemon a offrir") },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (hasNoMatchingPokemon) {
                Text(
                    text = "Vous ne possedez aucun des Pokemon souhaites.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (inventory.isEmpty()) {
                Text(
                    text = "Votre inventaire est vide.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(inventory, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemSelected(item) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.resourceName,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.resourceName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "x${item.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
