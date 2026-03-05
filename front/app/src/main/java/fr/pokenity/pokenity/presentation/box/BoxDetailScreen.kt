package fr.pokenity.pokenity.presentation.box

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun BoxDetailScreen(
    uiState: BoxDetailUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.errorMessage)
                OutlinedButton(onClick = onRetry, modifier = Modifier.padding(top = 10.dp)) {
                    Text("Reessayer")
                }
            }
        }

        uiState.box == null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Box introuvable.")
            }
        }

        else -> {
            val box = uiState.box
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = box.pokeballImage,
                                contentDescription = box.name,
                                modifier = Modifier.size(92.dp)
                            )
                            Text(
                                text = box.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total drop rate: ${"%.2f".format(box.totalDropRate)}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Contenu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(box.entries, key = { it.id }) { entry ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${entry.resourceType} #${entry.resourceId}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = entry.resourceName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Drop: ${"%.2f".format(entry.dropRate)}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
