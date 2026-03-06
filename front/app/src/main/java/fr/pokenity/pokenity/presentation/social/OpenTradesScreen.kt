package fr.pokenity.pokenity.presentation.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import fr.pokenity.data.model.InventoryItem
import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradePokemon
import fr.pokenity.data.model.TradeStatus

@Composable
fun OpenTradesScreen(
    uiState: SocialUiState,
    onAcceptTrade: (tradeId: String) -> Unit,
    onConfirmAccept: (tradeId: String) -> Unit,
    onDismissAcceptDialog: () -> Unit,
    onToggleOfferedSelection: (key: String) -> Unit,
    onToggleGivenItem: (inventoryItemId: String, suggestedQty: Int) -> Unit,
    onUpdateGivenQuantity: (inventoryItemId: String, quantity: Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val acceptingTradeId = uiState.acceptingTradeId
    val acceptingTrade = if (acceptingTradeId != null) {
        uiState.openTrades.find { it.id == acceptingTradeId }
    } else null

    if (acceptingTradeId != null && acceptingTrade != null) {
        AcceptTradeDialog(
            trade = acceptingTrade,
            myInventory = uiState.myInventory,
            selectedOfferedKeys = uiState.acceptDialogSelectedOffered,
            givenItems = uiState.acceptDialogGivenItems,
            onToggleOfferedSelection = onToggleOfferedSelection,
            onToggleGivenItem = onToggleGivenItem,
            onUpdateGivenQuantity = onUpdateGivenQuantity,
            onConfirm = { onConfirmAccept(acceptingTradeId) },
            onDismiss = onDismissAcceptDialog
        )
    }

    if (uiState.isLoading && uiState.openTrades.isEmpty()) {
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
                val offeredList = if (trade.offeredPokemons.isNotEmpty()) trade.offeredPokemons else listOfNotNull(trade.offeredPokemon)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (offeredList.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(offeredList, key = { "${it.resourceId}-${it.isShiny}" }) { offered ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(
                                        model = offered.imageUrl,
                                        contentDescription = offered.resourceName,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Text(
                                        text = buildString {
                                            append(offered.resourceName)
                                            if (offered.isShiny) append(" ✨")
                                            append(" x")
                                            append(offered.quantity)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (trade.requestedPokemons.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(trade.requestedPokemons, key = { "${it.resourceId}-${it.isShiny}" }) { requested ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(
                                        model = requested.imageUrl,
                                        contentDescription = requested.resourceName,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Text(
                                        text = buildString {
                                            append(requested.resourceName)
                                            if (requested.isShiny) append(" ✨")
                                            append(" x")
                                            append(requested.quantity)
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        Text("?", style = MaterialTheme.typography.headlineMedium)
                    }
                    Text(
                        text = "Demande",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
    trade: Trade,
    myInventory: List<InventoryItem>,
    selectedOfferedKeys: Set<String>,
    givenItems: Map<String, Int>,
    onToggleOfferedSelection: (key: String) -> Unit,
    onToggleGivenItem: (inventoryItemId: String, suggestedQty: Int) -> Unit,
    onUpdateGivenQuantity: (inventoryItemId: String, quantity: Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val offeredList = if (trade.offeredPokemons.isNotEmpty()) trade.offeredPokemons
                      else listOfNotNull(trade.offeredPokemon)
    val canConfirm = selectedOfferedKeys.isNotEmpty() && givenItems.isNotEmpty()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Confirmer l'echange",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Propose par ${trade.proposer?.username ?: "Inconnu"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Vous allez recevoir :",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                offeredList.forEach { pokemon ->
                    val key = "${pokemon.resourceId}:${pokemon.isShiny}"
                    val isSelected = selectedOfferedKeys.contains(key)
                    OfferedPokemonRow(
                        pokemon = pokemon,
                        isSelected = isSelected,
                        onToggle = { onToggleOfferedSelection(key) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Vous allez donner :",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (trade.requestedPokemons.isEmpty()) {
                    Text(
                        text = "Aucun pokemon demande.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    trade.requestedPokemons.forEach { requested ->
                        val inventoryItem = myInventory.find {
                            it.resourceId == requested.resourceId && it.isShiny == requested.isShiny
                        }
                        val isSelected = inventoryItem != null && givenItems.containsKey(inventoryItem.id)
                        val givenQty = if (inventoryItem != null) givenItems[inventoryItem.id] ?: 0 else 0
                        RequestedPokemonRow(
                            requested = requested,
                            inventoryItem = inventoryItem,
                            isSelected = isSelected,
                            givenQty = givenQty,
                            onToggle = {
                                if (inventoryItem != null) {
                                    val suggested = requested.quantity.coerceAtMost(inventoryItem.quantity)
                                    onToggleGivenItem(inventoryItem.id, suggested)
                                }
                            },
                            onUpdateQty = { qty ->
                                if (inventoryItem != null) onUpdateGivenQuantity(inventoryItem.id, qty)
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler", style = MaterialTheme.typography.bodyMedium)
                    }
                    PrimaryButton(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = canConfirm
                    ) {
                        Text("Confirmer", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun OfferedPokemonRow(
    pokemon: TradePokemon,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
        Spacer(modifier = Modifier.width(8.dp))
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.resourceName,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildString {
                    append(pokemon.resourceName)
                    if (pokemon.isShiny) append(" ✨")
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "x${pokemon.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RequestedPokemonRow(
    requested: TradePokemon,
    inventoryItem: InventoryItem?,
    isSelected: Boolean,
    givenQty: Int,
    onToggle: () -> Unit,
    onUpdateQty: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            enabled = inventoryItem != null
        )
        Spacer(modifier = Modifier.width(8.dp))
        AsyncImage(
            model = requested.imageUrl,
            contentDescription = requested.resourceName,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildString {
                    append(requested.resourceName)
                    if (requested.isShiny) append(" ✨")
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (inventoryItem != null) {
                Text(
                    text = "Vous avez : x${inventoryItem.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Non disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (isSelected && inventoryItem != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onUpdateQty(givenQty - 1) }) {
                    Text("-", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = "$givenQty",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onUpdateQty(givenQty + 1) }) {
                    Text("+", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
