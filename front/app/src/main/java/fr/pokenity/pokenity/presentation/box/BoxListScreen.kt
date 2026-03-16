package fr.pokenity.pokenity.presentation.box

import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.pokenity.pokenity.R

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun BoxListScreen(
    uiState: BoxListUiState,
    onRetry: () -> Unit,
    onBoxClick: (String) -> Unit,
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
                    Text(fr.pokenity.pokenity.ui.i18n.uiText("Reessayer"))
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.boxes, key = { it.id }) { box ->
                    val pokemonEntries = box.entries.filter { entry ->
                        entry.resourceType.equals("pokemon", ignoreCase = true)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBoxClick(box.id) }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.boxes_card),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = box.pokeballImage,
                                contentDescription = box.name,
                                modifier = Modifier.size(68.dp),
                                contentScale = ContentScale.Fit,
                                filterQuality = FilterQuality.None
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = box.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                if (pokemonEntries.isNotEmpty()) {
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        pokemonEntries.forEach { entry ->
                                            AsyncImage(
                                                model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${entry.resourceId}.png",
                                                contentDescription = entry.resourceName,
                                                modifier = Modifier.size(26.dp),
                                                contentScale = ContentScale.Fit,
                                                filterQuality = FilterQuality.None
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
