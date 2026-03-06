package fr.pokenity.pokenity.presentation.social

import androidx.compose.foundation.clickable
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
import fr.pokenity.data.model.Trade
import fr.pokenity.data.model.TradeStatus

@Composable
fun OpenTradesScreen(
    uiState: SocialUiState,
    onAcceptTrade: (tradeId: String) -> Unit,
    onDismissAcceptDialog: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.acceptingTradeId != null) {
        onDismissAcceptDialog()
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
