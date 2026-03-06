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
import androidx.compose.foundation.shape.RoundedCornerShape
import fr.pokenity.pokenity.ui.components.PrimaryButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
fun MyTradesScreen(
    uiState: SocialUiState,
    onConfirmTrade: (tradeId: String) -> Unit,
    onCancelTrade: (tradeId: String) -> Unit,
    onDeclineTrade: (tradeId: String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.isLoading && uiState.myTrades.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }



    if (uiState.myTrades.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Aucun echange en cours",
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

    val currentUserId = uiState.currentUserId

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        items(uiState.myTrades, key = { it.id }) { trade ->
            TradeCard(trade = trade) {
                if (currentUserId != null) {
                    TradeActions(
                        trade = trade,
                        currentUserId = currentUserId,
                        onConfirm = { onConfirmTrade(trade.id) },
                        onCancel = { onCancelTrade(trade.id) },
                        onDecline = { onDeclineTrade(trade.id) }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(4.dp)) }
    }
}

@Composable
private fun TradeActions(
    trade: Trade,
    currentUserId: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDecline: () -> Unit
) {
    val isProposer = trade.proposerId == currentUserId

    when (trade.status) {
        TradeStatus.WAITING_CONFIRMATION -> {
            if (isProposer) {
                // Recap pour le proposer : qui a accepté et avec quoi
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${trade.recipient?.username ?: "Quelqu'un"} veut accepter votre echange.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (trade.requestedPokemons.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Il donnera :",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(trade.requestedPokemons, key = { "${it.resourceId}-${it.isShiny}" }) { pokemon ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        AsyncImage(
                                            model = pokemon.imageUrl,
                                            contentDescription = pokemon.resourceName,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = buildString {
                                                append(pokemon.resourceName)
                                                if (pokemon.isShiny) append(" ✨")
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "x${pokemon.quantity}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Refuser")
                    }
                    PrimaryButton(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Confirmer")
                    }
                }
            }
        }

        TradeStatus.PENDING -> {
            if (isProposer) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Annuler")
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Refuser")
                }
            }
        }

        TradeStatus.COMPLETED,
        TradeStatus.CANCELED,
        TradeStatus.DECLINED -> { }
    }
}
